package com.alvinfungai.providers.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkHistory(
    @SerialName("id") val id: String? = null,
    @SerialName("provider_id") val providerId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("date_completed") val dateCompleted: Long,
    @SerialName("category") val category: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("customer_id") val customerId: String? = null
)
