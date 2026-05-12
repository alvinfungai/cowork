package com.alvinfungai.providers.data.repository

import android.util.Log
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.model.WorkHistory
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SupabaseServiceProvidersRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ServiceProvidersRepository {

    override fun getProviders(
        category: String,
        query: String,
        lat: Double?,
        lng: Double?,
        radiusKm: Double?
    ): Flow<Result<List<ServiceProvider>>> = flow {
        Log.d("COWORK_DEBUG", "Repo: getProviders started. Category: $category, Query: $query")
        try {
            val providers = withTimeout(15000) {
                if (lat != null && lng != null && radiusKm != null) {
                    val rpcParams = buildJsonObject {
                        put("search_lat", lat)
                        put("search_lng", lng)
                        put("radius_km", radiusKm)
                        put("search_category", category)
                        put("search_name", query)
                    }
                    supabaseClient.postgrest.rpc("get_providers_nearby", rpcParams).decodeList<ServiceProvider>()
                } else {
                    supabaseClient.postgrest.from("service_providers").select(Columns.ALL) {
                        filter {
                            if (category.trim().equals("All", ignoreCase = true).not()) {
                                or {
                                    eq("category", category)
                                    filter("services", FilterOperator.CS, "{${category}}")
                                }
                            }
                            if (query.trim().isNotBlank()) {
                                or {
                                    ilike("name", "%$query%")
                                    ilike("description", "%$query%")
                                    ilike("profession", "%$query%")
                                }
                            }
                        }
                    }.decodeList<ServiceProvider>()
                }
            }
            emit(Result.success(providers))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("COWORK_DEBUG", "Repo: getProviders error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getProviderDetails(providerId: String): Flow<Result<ServiceProvider>> = flow {
        try {
            val provider = supabaseClient.postgrest.from("service_providers")
                .select(Columns.ALL) {
                    filter {
                        eq("id", providerId)
                    }
                }.decodeSingle<ServiceProvider>()
            emit(Result.success(provider))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateServiceProvider(provider: ServiceProvider): Flow<Result<Unit>> = flow {
        try {
            val providerId = provider.id ?: throw IllegalArgumentException("Provider ID cannot be null for update")
            supabaseClient.postgrest.from("service_providers").update(provider) {
                filter {
                    eq("id", providerId)
                }
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("COWORK_DEBUG", "Repo: updateServiceProvider error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getServiceProviderByUserId(userId: String): Flow<Result<ServiceProvider?>> = flow {
        try {
            val provider = supabaseClient.postgrest.from("service_providers")
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<ServiceProvider>()
            emit(Result.success(provider))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("COWORK_DEBUG", "Repo: getServiceProviderByUserId error: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun registerServiceProvider(provider: ServiceProvider): Flow<Result<Unit>> = flow {
        try {
            withTimeout(15000) {
                supabaseClient.postgrest.from("service_providers").insert(provider)
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateLastActive(userId: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("COWORK_DEBUG", "Repo: Updating last active for $userId")
            val updateData = buildJsonObject { 
                put("last_active_at", System.currentTimeMillis()) 
            }
            
            supabaseClient.postgrest.from("service_providers").update(updateData) {
                filter {
                    eq("user_id", userId)
                }
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("COWORK_DEBUG", "Repo: updateLastActive failed for $userId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateProviderRating(userId: String, ratingAvg: Double, ratingCount: Int): Flow<Result<Unit>> = flow {
        try {
            Log.d("COWORK_DEBUG", "Repo: Updating rating for user $userId to $ratingAvg ($ratingCount reviews)")
            val updateData = buildJsonObject {
                put("rating_avg", ratingAvg)
                put("rating_count", ratingCount)
            }
            
            supabaseClient.postgrest.from("service_providers").update(updateData) {
                filter {
                    eq("user_id", userId)
                }
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("COWORK_DEBUG", "Repo: updateProviderRating failed for $userId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getWorkHistory(providerId: String): Flow<Result<List<WorkHistory>>> = callbackFlow {
        Log.d("COWORK_DEBUG", "Repo: getWorkHistory ENTERED with ID: $providerId")
        
        val query = firestore.collection("proof_of_work")
            .whereEqualTo("providerId", providerId)
            
        Log.d("COWORK_DEBUG", "Repo: getWorkHistory - setting up snapshot listener")
        
        val subscription = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("COWORK_DEBUG", "Repo: getWorkHistory LISTENER ERROR: ${error.message}")
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("COWORK_DEBUG", "Repo: getWorkHistory - snapshot is null")
                    trySend(Result.success(emptyList()))
                    return@addSnapshotListener
                }

                Log.d("COWORK_DEBUG", "Repo: getWorkHistory - Received snapshot with ${snapshot.size()} documents")

                val historyList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val imageUrls = doc.get("imageUrls") as? List<*>
                        val firstImageUrl = imageUrls?.firstOrNull() as? String

                        WorkHistory(
                            id = doc.id,
                            providerId = doc.getString("providerId") ?: "",
                            title = doc.getString("title") ?: "Job Completion",
                            description = doc.getString("description") ?: "",
                            dateCompleted = doc.getLong("submittedAt") ?: 0L,
                            category = doc.getString("category") ?: "Service",
                            imageUrl = firstImageUrl,
                            bookingId = doc.getString("bookingId"),
                            tags = (doc.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            isVerified = doc.getString("status") == "APPROVED",
                            customerId = doc.getString("customerId")
                        )
                    } catch (e: Exception) {
                        Log.e("COWORK_DEBUG", "Repo: Error mapping WorkHistory: ${e.message}")
                        null
                    }
                }
                
                Log.d("COWORK_DEBUG", "Repo: getWorkHistory SUCCESS - Sending ${historyList.size} items")
                trySend(Result.success(historyList))
            }

        awaitClose { 
            Log.d("COWORK_DEBUG", "Repo: getWorkHistory - Closing subscription")
            subscription.remove() 
        }
    }.flowOn(Dispatchers.IO)
}
