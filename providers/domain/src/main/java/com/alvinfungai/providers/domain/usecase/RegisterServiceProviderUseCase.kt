package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow


class RegisterServiceProviderUseCase(
    private val repository: ServiceProvidersRepository
) {
    operator fun invoke(provider: ServiceProvider): Flow<Result<Unit>> {
        return repository.registerServiceProvider(provider)
    }
}