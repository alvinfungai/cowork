package com.alvinfungai.providers.domain.model

data class Availability(
    val providerId: String = "",
    val slots: List<TimeSlot> = emptyList()
)
