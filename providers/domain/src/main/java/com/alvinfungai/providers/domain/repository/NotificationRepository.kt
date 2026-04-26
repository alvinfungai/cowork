package com.alvinfungai.providers.domain.repository

import com.alvinfungai.providers.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Result<List<AppNotification>>>
    fun markAsRead(notificationId: String): Flow<Result<Unit>>
    fun saveNotification(notification: AppNotification): Flow<Result<Unit>>
}
