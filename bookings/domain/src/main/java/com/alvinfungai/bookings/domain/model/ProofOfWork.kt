package com.alvinfungai.bookings.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProofOfWork(
    val id: String = "",
    val bookingId: String = "",
    val providerId: String = "",
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: VerificationStatus = VerificationStatus.PENDING,
    val verifierId: String? = null,
    val verifiedAt: Long? = null,
    val moderatorNotes: String? = null
)

enum class VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
