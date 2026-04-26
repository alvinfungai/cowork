package com.alvinfungai.users.domain.repository

import com.alvinfungai.users.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    fun createProfile(profile: UserProfile): Flow<Result<Unit>>
    fun updateProfile(profile: UserProfile): Flow<Result<Unit>>
    fun getProfile(uid: String): Flow<Result<UserProfile?>>
    fun deleteProfile(uid: String): Flow<Result<Unit>>
    fun updateFcmToken(uid: String, token: String): Flow<Result<Unit>>
    fun uploadProfileImage(uid: String, imageBytes: ByteArray): Flow<Result<String>>
    fun updateLastActive(uid: String): Flow<Result<Unit>>
}