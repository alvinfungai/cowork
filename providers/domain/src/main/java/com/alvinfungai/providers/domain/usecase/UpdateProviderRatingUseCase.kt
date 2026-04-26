package com.alvinfungai.providers.domain.usecase

import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import kotlinx.coroutines.flow.Flow

class UpdateProviderRatingUseCase(
    private val repository: ServiceProvidersRepository
) {
    operator fun invoke(userId: String, ratingAvg: Double, ratingCount: Int): Flow<Result<Unit>> {
        return repository.updateProviderRating(userId, ratingAvg, ratingCount)
    }
}
