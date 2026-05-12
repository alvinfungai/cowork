package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class VerifyProofOfWorkUseCase(
    private val repository: BookingsRepository
) {
    operator fun invoke(proofId: String, verifierId: String, status: String, notes: String?) =
        repository.verifyProofOfWork(proofId, verifierId, status, notes)
}
