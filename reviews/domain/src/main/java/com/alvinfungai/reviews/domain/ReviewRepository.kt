package com.alvinfungai.reviews.domain

import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun addReview(review: Review): Flow<Result<Unit>>
    fun getReviewsForProvider(providerId: String): Flow<Result<List<Review>>>
}
