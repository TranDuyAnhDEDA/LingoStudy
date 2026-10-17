package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(uid: String): Flow<Result<UserProfile?>>
    suspend fun createUserProfile(user: UserProfile): Result<Unit>
    suspend fun updateUserProfile(user: UserProfile): Result<Unit>
    suspend fun updateDisplayName(uid: String, displayName: String): Result<Unit>
    suspend fun updateAvatar(uid: String, avatarId: String, customAvatarPath: String? = null): Result<Unit> = Result.success(Unit)
    suspend fun updateProfileDetails(
        uid: String,
        displayName: String,
        phoneNumber: String = "",
        dailyGoalMinutes: Int = 30,
        levelId: String = "",
        levelName: String = ""
    ): Result<Unit> = Result.success(Unit)
    fun getCurrentUserProfile(): Flow<Result<UserProfile?>>
}
