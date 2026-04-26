package com.alvinfungai.bookings.domain.model

data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val hourlyRate: Double = 0.0,
    val amountDue: Double = 0.0,
    val serviceId: String = "",
    val scheduledTime: Long = 0,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    STARTED,
    COMPLETED,
    CANCELLED
}
