package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthUser>
    suspend fun login(email: String, password: String): Result<AuthUser>
    fun logout()
    fun getCurrentUser(): AuthUser?
    fun observeAuthState(): Flow<AuthUser?>
}
