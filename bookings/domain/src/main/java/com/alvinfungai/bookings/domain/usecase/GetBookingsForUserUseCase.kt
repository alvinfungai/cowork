package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class GetBookingsForUserUseCase(
    private val bookingsRepository: BookingsRepository
) {
    operator fun invoke(userId: String) = bookingsRepository.getBookingsForUser(userId)
}
