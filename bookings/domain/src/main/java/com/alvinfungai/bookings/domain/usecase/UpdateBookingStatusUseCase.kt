package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class UpdateBookingStatusUseCase(
    private val bookingsRepository: BookingsRepository
) {
    operator fun invoke(bookingId: String, status: String) = bookingsRepository.updateBookingStatus(bookingId, status)
}
