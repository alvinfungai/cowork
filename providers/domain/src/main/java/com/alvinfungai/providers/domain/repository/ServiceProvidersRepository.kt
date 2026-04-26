package com.alvinfungai.providers.domain.repository

import com.alvinfungai.providers.domain.model.ServiceProvider
import kotlinx.coroutines.flow.Flow

interface ServiceProvidersRepository {
    fun getProviders(
        category: String,
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null
    ): Flow<Result<List<ServiceProvider>>>

    fun getProviderDetails(providerId: String): Flow<Result<ServiceProvider>>
    fun updateServiceProvider(provider: ServiceProvider): Flow<Result<Unit>>
    fun getServiceProviderByUserId(userId: String): Flow<Result<ServiceProvider?>>
    fun registerServiceProvider(provider: ServiceProvider): Flow<Result<Unit>>
    fun updateLastActive(userId: String): Flow<Result<Unit>>
    fun updateProviderRating(userId: String, ratingAvg: Double, ratingCount: Int): Flow<Result<Unit>>
}
