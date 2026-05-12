package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class GetPendingProofOfWorksUseCase(
    private val repository: BookingsRepository
) {
    operator fun invoke() = repository.getPendingProofOfWorks()
}
