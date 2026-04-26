package com.alvinfungai.providers.domain.model

data class Review(
    val id: String = "",
    val bookingId: String = "",
    val productId: String = "",
    val customerId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: String = ""
)
