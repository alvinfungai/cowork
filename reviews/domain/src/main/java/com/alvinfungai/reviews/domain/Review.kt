package com.alvinfungai.reviews.domain

data class Review(
    val id: String,
    val bookingId: String,
    val customerId: String,
    val customerName: String = "",
    val providerId: String,
    val rating: Double,
    val comment: String,
    val createdAt: Long
)
