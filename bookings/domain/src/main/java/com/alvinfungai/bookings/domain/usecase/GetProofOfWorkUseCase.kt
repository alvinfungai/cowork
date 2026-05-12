package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class GetProofOfWorkUseCase(
    private val repository: BookingsRepository
) {
    operator fun invoke(bookingId: String) = repository.getProofOfWork(bookingId)
}
