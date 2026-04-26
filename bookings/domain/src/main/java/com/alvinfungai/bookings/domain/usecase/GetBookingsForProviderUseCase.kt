package com.alvinfungai.bookings.domain.usecase

import com.alvinfungai.bookings.domain.repository.BookingsRepository

class GetBookingsForProviderUseCase(
    private val bookingsRepository: BookingsRepository
) {
    operator fun invoke(providerId: String) = bookingsRepository.getBookingsForProvider(providerId)
}
