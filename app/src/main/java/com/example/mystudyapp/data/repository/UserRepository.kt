package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(uid: String): Flow<Result<UserProfile?>>
    suspend fun createUserProfile(user: UserProfile): Result<Unit>
    suspend fun updateUserProfile(user: UserProfile): Result<Unit>
    suspend fun updateDisplayName(uid: String, displayName: String): Result<Unit>
    fun getCurrentUserProfile(): Flow<Result<UserProfile?>>
}
