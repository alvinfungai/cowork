package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.UserRepository


class DeleteProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(uid: String) = userRepository.deleteProfile(uid)
}