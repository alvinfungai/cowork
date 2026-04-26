package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.repository.BookingsRepository

class CreateBookingUseCase(
    private val bookingsRepository: BookingsRepository
) {
    operator fun invoke(booking: Booking) = bookingsRepository.createBooking(booking)
}
