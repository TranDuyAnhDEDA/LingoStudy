package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun getUserProgress(uid: String): Flow<Result<UserProgress?>>
    suspend fun createUserProgress(progress: UserProgress): Result<Unit>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
    fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>>
    suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<Unit>
    suspend fun markLessonCompleted(uid: String, lessonId: String): Result<Unit>
    suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean>
}
