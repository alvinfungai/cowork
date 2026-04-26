package com.alvinfungai.users.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val profileImage: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDarkTheme: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    CLIENT,
    PROVIDER,
    ADMIN
}