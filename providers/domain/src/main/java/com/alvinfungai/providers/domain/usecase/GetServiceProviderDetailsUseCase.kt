package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow


class GetServiceProviderDetailsUseCase(
    private val serviceProvidersRepository: ServiceProvidersRepository
) {
    operator fun invoke(providerId: String): Flow<Result<ServiceProvider>> {
        return serviceProvidersRepository.getProviderDetails(providerId)
    }
}