package com.alvinfungai.providers.data.repository

import android.util.Log
import com.alvinfungai.providers.domain.model.AppNotification
import com.alvinfungai.providers.domain.model.NotificationType
import com.alvinfungai.providers.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Result<List<AppNotification>>> = callbackFlow {
        Log.d("NotificationRepo", "Fetching notifications for user: $userId")
        
        val subscription = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error fetching notifications", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("NotificationRepo", "Error parsing notification", e)
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(Result.success(notifications))
            }

        awaitClose { subscription.remove() }
    }

    override fun markAsRead(notificationId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        close()
        awaitClose { }
    }

    override fun saveNotification(notification: AppNotification): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection("notifications")
                .add(notification)
                .await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        close()
        awaitClose { }
    }
}
