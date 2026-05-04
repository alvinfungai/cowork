package com.alvinfungai.providers.domain.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.BOOKING_UPDATE,
    val bookingId: String? = null,
    val workHistoryId: String? = null,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class NotificationType {
    BOOKING_REQUEST,
    BOOKING_UPDATE,
    REVIEW_RECEIVED,
    WORK_HISTORY_VERIFICATION
}
