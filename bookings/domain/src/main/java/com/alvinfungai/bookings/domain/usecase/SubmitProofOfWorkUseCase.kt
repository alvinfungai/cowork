package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.model.ProofOfWork
import com.alvinfungai.bookings.domain.repository.BookingsRepository

class SubmitProofOfWorkUseCase(
    private val repository: BookingsRepository
) {
    operator fun invoke(proof: ProofOfWork) = repository.submitProofOfWork(proof)
}
