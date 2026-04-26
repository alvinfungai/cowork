package com.alvinfungai.users.data.repository

import com.alvinfungai.users.domain.model.UserProfile
import com.alvinfungai.users.domain.model.UserRole
import com.alvinfungai.users.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient
): UserRepository {

    companion object {
        private const val USERS = "users"
        private const val BUCKET_PROFILES = "profiles"
        private const val SUPABASE_URL = "https://zoxtuqzsntwqondsbiod.supabase.co"
    }

    private val usersCollection by lazy { firebaseFirestore.collection(USERS) }

    override fun createProfile(profile: UserProfile): Flow<Result<Unit>> = callbackFlow {
        if (profile.uid.isBlank()) {
            trySend(Result.failure(Exception("UID cannot be empty")))
            close()
            return@callbackFlow
        }
        try {
            usersCollection.document(profile.uid).set(profile).await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            trySend(Result.failure(e))
        } finally {
            close()
        }
        awaitClose { }
    }

    override fun updateProfile(profile: UserProfile): Flow<Result<Unit>> = callbackFlow {
        try {
            usersCollection.document(profile.uid).set(profile).await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            trySend(Result.failure(e))
        } finally {
            close()
        }
        awaitClose { }
    }

    override fun getProfile(uid: String): Flow<Result<UserProfile?>> = callbackFlow {
        val docRef = usersCollection.document(uid)

        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            
            try {
                val profile = snapshot?.let { doc ->
                    if (doc.exists()) {
                        // Manually map the role to handle legacy "CUSTOMER" value
                        val roleStr = doc.getString("role")
                        val role = when (roleStr) {
                            "CUSTOMER", "CLIENT" -> UserRole.CLIENT
                            "PROVIDER" -> UserRole.PROVIDER
                            "ADMIN" -> UserRole.ADMIN
                            else -> UserRole.CLIENT
                        }

                        // Try to get flat latitude/longitude first, then fallback to nested location object
                        val latitude = doc.getDouble("latitude") ?: (doc.get("location") as? Map<*, *>)?.get("lat") as? Double
                        val longitude = doc.getDouble("longitude") ?: (doc.get("location") as? Map<*, *>)?.get("lng") as? Double

                        UserProfile(
                            uid = doc.getString("uid") ?: "",
                            displayName = doc.getString("displayName") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            role = role,
                            profileImage = doc.getString("profileImage"),
                            latitude = latitude,
                            longitude = longitude,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            lastActiveAt = doc.getLong("lastActiveAt") ?: 0L
                        )
                    } else null
                }
                trySend(Result.success(profile))
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun deleteProfile(uid: String): Flow<Result<Unit>> = callbackFlow {
        try {
            usersCollection.document(uid).delete().await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            trySend(Result.failure(e))
        } finally {
            close()
        }
        awaitClose { }
    }

    override fun updateFcmToken(uid: String, token: String): Flow<Result<Unit>> = callbackFlow {
        try {
            usersCollection.document(uid).update("fcmToken", token).await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            trySend(Result.failure(e))
        } finally {
            close()
        }
        awaitClose { }
    }

    override fun uploadProfileImage(uid: String, imageBytes: ByteArray): Flow<Result<String>> = flow {
        try {
            val bucket = supabaseClient.storage.from(BUCKET_PROFILES)
            val fileName = "$uid.jpg"
            
            // Upload to Supabase Storage
            bucket.upload(path = fileName, data = imageBytes) {
                upsert = true
            }
            
            // Generate a permanent public URL
            val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$BUCKET_PROFILES/$fileName"
            
            emit(Result.success(publicUrl))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.failure(e))
        }
    }

    override fun updateLastActive(uid: String): Flow<Result<Unit>> = flow {
        try {
            usersCollection.document(uid).update("lastActiveAt", System.currentTimeMillis()).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Result.failure(e))
        }
    }
}
