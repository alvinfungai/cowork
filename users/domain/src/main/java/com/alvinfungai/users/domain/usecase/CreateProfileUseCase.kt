package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.model.UserProfile
import com.alvinfungai.users.domain.repository.UserRepository


class CreateUserProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(profile: UserProfile) = userRepository.createProfile(profile)
}