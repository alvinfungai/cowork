package com.alvinfungai.bookings.domain.repository

import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.model.ProofOfWork
import kotlinx.coroutines.flow.Flow

interface BookingsRepository {
    fun createBooking(booking: Booking): Flow<Result<Unit>>
    fun getBookingsForUser(userId: String): Flow<Result<List<Booking>>>
    fun getBookingsForProvider(providerId: String): Flow<Result<List<Booking>>>
    fun updateBookingStatus(bookingId: String, status: String): Flow<Result<Unit>>
    
    // Proof of Work
    fun submitProofOfWork(proof: ProofOfWork): Flow<Result<Unit>>
    fun getProofOfWork(bookingId: String): Flow<Result<ProofOfWork?>>
    fun getPendingProofOfWorks(): Flow<Result<List<ProofOfWork>>>
    fun verifyProofOfWork(proofId: String, verifierId: String, status: String, notes: String?): Flow<Result<Unit>>
}
