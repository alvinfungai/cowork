package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow

class GetServiceProviderByUserIdUseCase(
    private val repository: ServiceProvidersRepository
) {
    operator fun invoke(userId: String): Flow<Result<ServiceProvider?>> {
        return repository.getServiceProviderByUserId(userId)
    }
}
