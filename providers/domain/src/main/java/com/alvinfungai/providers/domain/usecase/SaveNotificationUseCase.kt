package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.AppNotification
import com.alvinfungai.providers.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class SaveNotificationUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(notification: AppNotification): Flow<Result<Unit>> {
        return repository.saveNotification(notification)
    }
}
