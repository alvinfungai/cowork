package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UpdateLastActiveUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(uid: String): Flow<Result<Unit>> {
        return userRepository.updateLastActive(uid)
    }
}
