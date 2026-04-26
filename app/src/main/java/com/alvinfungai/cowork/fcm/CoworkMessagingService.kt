package com.alvinfungai.cowork.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alvinfungai.cowork.MainActivity
import com.alvinfungai.providers.domain.model.AppNotification
import com.alvinfungai.providers.domain.model.NotificationType
import com.alvinfungai.providers.domain.repository.NotificationRepository
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.UpdateFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CoworkMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        scope.launch {
            val user = getCurrentUserUseCase()
            if (user != null) {
                updateFcmTokenUseCase(user.uid, token).collect { result ->
                    result.onSuccess {
                        Log.d("FCM", "Token updated successfully")
                    }.onFailure {
                        Log.e("FCM", "Failed to update token", it)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: message.data["title"] ?: "New Notification"
        val body = message.notification?.body ?: message.data["message"] ?: ""
        val typeStr = message.data["type"]
        val bookingId = message.data["bookingId"]

        showNotification(title, body)

        scope.launch {
            val user = getCurrentUserUseCase()
            if (user != null) {
                val notification = AppNotification(
                    userId = user.uid,
                    title = title,
                    message = body,
                    type = try {
                        NotificationType.valueOf(typeStr ?: NotificationType.BOOKING_UPDATE.name)
                    } catch (e: Exception) {
                        NotificationType.BOOKING_UPDATE
                    },
                    bookingId = bookingId,
                    read = false,
                    createdAt = System.currentTimeMillis()
                )
                
                notificationRepository.saveNotification(notification).collect { result ->
                    result.onSuccess {
                        Log.d("FCM", "Notification saved to Firestore")
                    }.onFailure {
                        Log.e("FCM", "Failed to save notification to Firestore", it)
                    }
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "cowork_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cowork Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
