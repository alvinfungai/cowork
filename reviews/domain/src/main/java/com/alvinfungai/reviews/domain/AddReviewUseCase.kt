package com.alvinfungai.reviews.domain

class AddReviewUseCase(
    private val repository: ReviewRepository
) {
    operator fun invoke(review: Review) = repository.addReview(review)
}
