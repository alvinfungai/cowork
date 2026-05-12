package com.alvinfungai.coworkapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TestSamplesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun createTestData(targetUserId: String) {
        // 1. Promote a user to MODERATOR
        firestore.collection("users").document(targetUserId)
            .update("role", "MODERATOR")
            .addOnSuccessListener { Log.d("TEST", "User promoted to MODERATOR") }

        // 2. Create a Dummy Booking
        val dummyBookingId = "test_booking_123"
        val bookingData = mapOf(
            "id" to dummyBookingId,
            "providerId" to "sample_provider_id",
            "providerName" to "John Doe Services",
            "customerName" to "Jane Smith",
            "status" to "COMPLETED",
            "amountDue" to 150.0,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("bookings").document(dummyBookingId).set(bookingData)

        // 3. Create a Dummy Proof of Work (PENDING Audit)
        val proofId = "sample_proof_999"
        val proofData = mapOf(
            "id" to proofId,
            "bookingId" to dummyBookingId,
            "providerId" to "sample_provider_id",
            "description" to "Fixed the leaking roof. Replaced 5 tiles and applied waterproof sealant. See attached job card and final photo.",
            "imageUrls" to listOf(
                "https://images.pexels.com/photos/159358/construction-site-build-construction-working-159358.jpeg",
                "https://images.pexels.com/photos/585418/pexels-photo-585418.jpeg"
            ),
            "status" to "PENDING",
            "submittedAt" to System.currentTimeMillis()
        )

        firestore.collection("proof_of_work").document(proofId).set(proofData)
            .addOnSuccessListener {
                // Link the proof back to the booking
                firestore.collection("bookings").document(dummyBookingId)
                    .update("proofOfWorkId", proofId)
                Log.d("TEST", "Sample Proof of Work created successfully")
            }
    }
}
