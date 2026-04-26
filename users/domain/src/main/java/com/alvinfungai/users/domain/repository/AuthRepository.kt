package com.alvinfungai.users.domain.repository

import com.alvinfungai.users.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow


interface AuthRepository {

    fun login(email: String, password: String): Flow<Result<AuthUser>>

    fun register(email: String, password: String): Flow<Result<AuthUser>>

    suspend fun logout()

    suspend fun getCurrentUser(): AuthUser?
}