package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.model.UserProfile
import com.alvinfungai.users.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UpdateProfileUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(profile: UserProfile): Flow<Result<Unit>> {
        return userRepository.updateProfile(profile)
    }
}
