package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.AuthRepository


class LoginUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String) = authRepository.login(email, password)
}