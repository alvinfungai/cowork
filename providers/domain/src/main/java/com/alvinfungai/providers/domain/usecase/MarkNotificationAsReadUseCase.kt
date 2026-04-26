package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class MarkNotificationAsReadUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(notificationId: String): Flow<Result<Unit>> {
        return repository.markAsRead(notificationId)
    }
}
