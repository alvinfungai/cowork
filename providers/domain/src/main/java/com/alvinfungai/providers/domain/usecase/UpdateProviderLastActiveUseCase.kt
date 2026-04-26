package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow

class UpdateProviderLastActiveUseCase(
    private val repository: ServiceProvidersRepository
) {
    operator fun invoke(userId: String): Flow<Result<Unit>> {
        return repository.updateLastActive(userId)
    }
}
