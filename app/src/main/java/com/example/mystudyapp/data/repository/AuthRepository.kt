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

    // Xác thực địa chỉ Email qua Firebase
    suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)
    suspend fun reloadUser(): Result<AuthUser?> = Result.success(getCurrentUser())
    fun isEmailVerified(): Boolean = getCurrentUser()?.isEmailVerified ?: false
}
