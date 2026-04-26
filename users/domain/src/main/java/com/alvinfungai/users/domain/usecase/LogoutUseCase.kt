package com.alvinfungai.users.domain.usecase

import com.alvinfungai.users.domain.repository.AuthRepository


class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}