package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UpdateFcmTokenUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(uid: String, token: String): Flow<Result<Unit>> {
        return userRepository.updateFcmToken(uid, token)
    }
}
