package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.AppNotification
import com.alvinfungai.providers.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<AppNotification>>> {
        return repository.getNotifications(userId)
    }
}
