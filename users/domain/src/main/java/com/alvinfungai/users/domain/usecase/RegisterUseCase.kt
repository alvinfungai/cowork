package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.AuthRepository


class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String) = authRepository.register(email, password)
}