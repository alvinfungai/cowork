package com.alvinfungai.bookings.data.repository

import android.util.Log
import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.model.BookingStatus
import com.alvinfungai.bookings.domain.repository.BookingsRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

class BookingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingsRepository {

    companion object {
        private const val BOOKINGS_COLLECTION = "bookings"
    }

    override fun createBooking(booking: Booking): Flow<Result<Unit>> = callbackFlow {
        try {
            val bookingMap = mutableMapOf(
                "id" to booking.id,
                "customerId" to booking.customerId,
                "customerName" to booking.customerName,
                "customerPhone" to booking.customerPhone,
                "providerId" to booking.providerId,
                "providerName" to booking.providerName,
                "hourlyRate" to booking.hourlyRate,
                "amountDue" to booking.amountDue,
                "serviceId" to booking.serviceId,
                "scheduledTime" to booking.scheduledTime,
                "startTime" to booking.startTime,
                "endTime" to booking.endTime,
                "location" to GeoPoint(booking.latitude ?: 0.0, booking.longitude ?: 0.0),
                "notes" to booking.notes,
                "status" to booking.status.name,
                "createdAt" to booking.createdAt
            )
            
            val docRef = if (booking.id.isEmpty()) {
                firestore.collection(BOOKINGS_COLLECTION).document()
            } else {
                firestore.collection(BOOKINGS_COLLECTION).document(booking.id)
            }
            
            bookingMap["id"] = docRef.id
            
            docRef.set(bookingMap).await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getBookingsForUser(userId: String): Flow<Result<List<Booking>>> = callbackFlow {
        val subscription = firestore.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("customerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        mapDocumentToBooking(doc)
                    }
                    trySend(Result.success(bookings))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getBookingsForProvider(providerId: String): Flow<Result<List<Booking>>> = callbackFlow {
        val subscription = firestore.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        mapDocumentToBooking(doc)
                    }
                    Log.d("BOOKINGS", "getBookingsForProvider: $bookings")
                    trySend(Result.success(bookings))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateBookingStatus(bookingId: String, status: String): Flow<Result<Unit>> = callbackFlow {
        try {
            val updateData = mutableMapOf<String, Any>("status" to status)
            
            // Automatically set startTime when status changes to STARTED
            if (status == BookingStatus.STARTED.name) {
                updateData["startTime"] = System.currentTimeMillis()
            }
            
            // Automatically set endTime and calculate amountDue when status changes to COMPLETED
            if (status == BookingStatus.COMPLETED.name) {
                val endTime = System.currentTimeMillis()
                updateData["endTime"] = endTime
                
                // Fetch the booking to get startTime and hourlyRate
                val bookingDoc = firestore.collection(BOOKINGS_COLLECTION).document(bookingId).get().await()
                val startTime = bookingDoc.getLong("startTime")
                val hourlyRate = bookingDoc.getDouble("hourlyRate") ?: 0.0
                
                if (startTime != null && hourlyRate > 0) {
                    val durationMillis = endTime - startTime
                    val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                    
                    // Round up to nearest 15 minutes
                    val roundedMinutes = ceil(durationMinutes / 15.0).toLong() * 15
                    val hours = roundedMinutes.toDouble() / 60.0
                    val amountDue = hours * hourlyRate
                    
                    updateData["amountDue"] = amountDue
                }
            }

            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updateData)
                .await()
            trySend(Result.success(Unit))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    private fun mapDocumentToBooking(doc: DocumentSnapshot): Booking? {
        val geoPoint = doc.getGeoPoint("location")
        val statusStr = doc.getString("status") ?: BookingStatus.PENDING.name
        val status = try {
            BookingStatus.valueOf(statusStr.uppercase())
        } catch (e: Exception) {
            BookingStatus.PENDING
        }

        return Booking(
            id = doc.id,
            customerId = doc.getString("customerId") ?: "",
            customerName = doc.getString("customerName") ?: doc.getString("customerDisplayName") ?: "",
            customerPhone = doc.getString("customerPhone") ?: "",
            providerId = doc.getString("providerId") ?: "",
            providerName = doc.getString("providerName") ?: "",
            hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
            amountDue = doc.getDouble("amountDue") ?: 0.0,
            serviceId = doc.getString("serviceId") ?: "",
            scheduledTime = doc.getLong("scheduledTime") ?: 0,
            startTime = doc.getLong("startTime"),
            endTime = doc.getLong("endTime"),
            latitude = geoPoint?.latitude,
            longitude = geoPoint?.longitude,
            notes = doc.getString("notes") ?: "",
            status = status,
            createdAt = doc.getLong("createdAt") ?: 0
        )
    }
}
