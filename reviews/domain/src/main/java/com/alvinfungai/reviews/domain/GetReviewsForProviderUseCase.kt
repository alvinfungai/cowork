package com.alvinfungai.reviews.domain

class GetReviewsForProviderUseCase(
    private val repository: ReviewRepository
) {
    operator fun invoke(providerId: String) = repository.getReviewsForProvider(providerId)
}
