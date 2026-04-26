package com.alvinfungai.providers.data.repository

import android.util.Log
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SupabaseServiceProvidersRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val firebaseAuth: FirebaseAuth
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
}
