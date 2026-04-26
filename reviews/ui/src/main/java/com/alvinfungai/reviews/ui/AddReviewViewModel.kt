package com.alvinfungai.reviews.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.usecase.UpdateProviderRatingUseCase
import com.alvinfungai.reviews.domain.AddReviewUseCase
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.Review
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddReviewViewModel @Inject constructor(
    private val addReviewUseCase: AddReviewUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getReviewsForProviderUseCase: GetReviewsForProviderUseCase,
    private val updateProviderRatingUseCase: UpdateProviderRatingUseCase
) : ViewModel() {

    private val _reviewStatus = MutableStateFlow<Result<Unit>?>(null)
    val reviewStatus = _reviewStatus.asStateFlow()

    fun submitReview(bookingId: String, providerId: String, rating: Double, comment: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _reviewStatus.update { Result.failure(Exception("User not authenticated")) }
                return@launch
            }
            
            val profileResult = getUserProfileUseCase(user.uid).first()
            val customerName = profileResult.getOrNull()?.displayName ?: "User"

            val review = Review(
                id = "",
                bookingId = bookingId,
                customerId = user.uid,
                customerName = customerName,
                providerId = providerId,
                rating = rating,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )

            addReviewUseCase(review).collect { result ->
                if (result.isSuccess) {
                    Log.d("AddReviewViewModel", "Review saved to Firestore. Syncing stats for provider: $providerId")
                    updateProviderRating(providerId)
                }
                _reviewStatus.update { result }
            }
        }
    }

    private suspend fun updateProviderRating(providerId: String) {
        try {
            // Fetch all reviews from Firestore to calculate the new average
            val reviewsResult = getReviewsForProviderUseCase(providerId).first()
            val reviews = reviewsResult.getOrNull() ?: emptyList<Review>()
            
            if (reviews.isNotEmpty()) {
                val count = reviews.size
                val avg = reviews.map { it.rating }.average()

                Log.d("AddReviewViewModel", "Calculated stats: avg=$avg, count=$count")
                
                // Use the specialized partial update Use Case to update ONLY the rating fields in Supabase
                withContext(NonCancellable) {
                    Log.d("AddReviewViewModel", "Sending partial update to Supabase for Firebase UID: $providerId")
                    val updateResult = updateProviderRatingUseCase(providerId, avg, count).first()
                    
                    if (updateResult.isSuccess) {
                        Log.d("AddReviewViewModel", "Supabase rating sync SUCCESS")
                    } else {
                        Log.e("AddReviewViewModel", "Supabase rating sync FAILED: ${updateResult.exceptionOrNull()?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AddReviewViewModel", "Critical failure during rating sync: ${e.message}")
        }
    }
}
