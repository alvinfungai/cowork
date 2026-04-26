package com.alvinfungai.reviews.data

import com.alvinfungai.reviews.domain.Review
import com.alvinfungai.reviews.domain.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override fun addReview(review: Review): Flow<Result<Unit>> = callbackFlow {
        try {
            // Note: We are now storing providers in Supabase.
            // Rating updates should ideally happen via a Cloud Function or a direct Supabase update.
            // For now, we store the review in Firestore as usual.
            
            val reviewRef = firestore.collection("reviews").document()
            val reviewData = hashMapOf(
                "id" to reviewRef.id,
                "bookingId" to review.bookingId,
                "customerId" to review.customerId,
                "customerName" to review.customerName,
                "providerId" to review.providerId,
                "rating" to review.rating,
                "comment" to review.comment,
                "createdAt" to review.createdAt
            )
            
            reviewRef.set(reviewData).await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getReviewsForProvider(providerId: String): Flow<Result<List<Review>>> = callbackFlow {
        // providerId here is the Firebase UID (which matches 'providerId' in Firestore 'bookings')
        val subscription = firestore.collection("reviews")
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.documents.mapNotNull { doc ->
                        if (!doc.exists()) return@mapNotNull null
                        Review(
                            id = doc.id,
                            bookingId = doc.getString("bookingId") ?: "",
                            customerId = doc.getString("customerId") ?: "",
                            customerName = doc.getString("customerName") ?: "User",
                            providerId = doc.getString("providerId") ?: "",
                            rating = doc.getDouble("rating") ?: 0.0,
                            comment = doc.getString("comment") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    }
                    trySend(Result.success(reviews))
                }
            }
        awaitClose { subscription.remove() }
    }
}
