package com.alvinfungai.users

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TestSamplesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val TAG = "TEST_DATA"

    fun createTestData(targetUserId: String) {
        Log.d(TAG, "Starting creation sequence for UID: $targetUserId")

        // 1. Promote user to MODERATOR first
        firestore.collection("users").document(targetUserId)
            .update("role", "MODERATOR")
            .addOnSuccessListener {
                Log.d(TAG, "Step 1 Success: User promoted to MODERATOR")
                
                // 2. Create Dummy Booking using targetUserId as providerId
                // Using targetUserId is critical to satisfy Firestore security rules (ownership)
                val timestamp = System.currentTimeMillis()
                val dummyBookingId = "test_booking_$timestamp"
                val proofId = "sample_proof_$timestamp"

                val bookingData = mapOf(
                    "id" to dummyBookingId,
                    "customerId" to "sample_client_id",
                    "customerName" to "Jane Client",
                    "customerPhone" to "+263777111222",
                    "providerId" to targetUserId, 
                    "providerName" to "Test Provider Services",
                    "status" to "COMPLETED",
                    "amountDue" to 150.0,
                    "location" to GeoPoint(-17.82, 31.05), // Required for repository mapping
                    "createdAt" to timestamp
                )

                firestore.collection("bookings").document(dummyBookingId).set(bookingData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Step 2 Success: Dummy Booking created: $dummyBookingId")

                        // 3. Create Dummy Proof of Work (PENDING Audit)
                        val proofData = mapOf(
                            "id" to proofId,
                            "bookingId" to dummyBookingId,
                            "providerId" to targetUserId, // MUST match authenticated UID for rules
                            "description" to "Fixed the leaking roof. Replaced 5 tiles and applied waterproof sealant. See attached job card and final photo.",
                            "imageUrls" to listOf(
                                "https://images.pexels.com/photos/159358/construction-site-build-construction-working-159358.jpeg",
                                "https://images.pexels.com/photos/585418/pexels-photo-585418.jpeg"
                            ),
                            "status" to "PENDING",
                            "submittedAt" to timestamp
                        )

                        firestore.collection("proof_of_work").document(proofId).set(proofData)
                            .addOnSuccessListener {
                                Log.d(TAG, "Step 3 Success: Sample Proof of Work created: $proofId")

                                // 4. Link the proof back to the booking
                                firestore.collection("bookings").document(dummyBookingId)
                                    .update("proofOfWorkId", proofId)
                                    .addOnSuccessListener {
                                        Log.i(TAG, "SUCCESS: All test data created and linked. You can now see this in the Moderator Panel!")
                                    }
                                    .addOnFailureListener { e -> Log.e(TAG, "Step 4 Failed", e) }
                            }
                            .addOnFailureListener { e -> 
                                Log.e(TAG, "Step 3 Failed: PERMISSION_DENIED. Check Firestore rules for 'proof_of_work'. UID used: $targetUserId", e) 
                            }
                    }
                    .addOnFailureListener { e -> Log.e(TAG, "Step 2 Failed", e) }
            }
            .addOnFailureListener { e -> Log.e(TAG, "Step 1 Failed", e) }
    }
}
