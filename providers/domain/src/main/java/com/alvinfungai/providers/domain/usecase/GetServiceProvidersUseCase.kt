package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow


class GetServiceProvidersUseCase(
    private val serviceProvidersRepository: ServiceProvidersRepository
) {
    operator fun invoke(
        category: String,
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null
    ): Flow<Result<List<ServiceProvider>>> {
        return serviceProvidersRepository.getProviders(
            category = category,
            query = query,
            lat = lat,
            lng = lng,
            radiusKm = radiusKm
        )
    }
}