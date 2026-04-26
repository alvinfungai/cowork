package com.alvinfungai.bookings.domain.repository

import com.alvinfungai.bookings.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingsRepository {
    fun createBooking(booking: Booking): Flow<Result<Unit>>
    fun getBookingsForUser(userId: String): Flow<Result<List<Booking>>>
    fun getBookingsForProvider(providerId: String): Flow<Result<List<Booking>>>
    fun updateBookingStatus(bookingId: String, status: String): Flow<Result<Unit>>
}
