package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.DailyStudyTime
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProgressRepository {
    fun getUserProgress(uid: String): Flow<Result<UserProgress?>>
    suspend fun createUserProgress(progress: UserProgress): Result<Unit>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
    fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>>
    suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<Unit>
    suspend fun markLessonCompleted(uid: String, lessonId: String): Result<Unit>
    suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean>
    suspend fun updateCurrentLearningPosition(uid: String, lessonId: String, activity: String? = null, index: Int = 0): Result<Unit> = Result.success(Unit)
    suspend fun recordStudyTime(uid: String, seconds: Long, dateString: String): Result<Unit> = Result.success(Unit)
    fun getDailyStudyTime(uid: String, dateString: String): Flow<Result<DailyStudyTime?>> = flowOf(Result.success(null))
    fun getLast7DaysStudyTime(uid: String): Flow<Result<List<DailyStudyTime>>> = flowOf(Result.success(emptyList()))
    fun getAllTimeStudyTime(uid: String): Flow<Result<Long>> = flowOf(Result.success(0L))
    fun getLearningHistory(uid: String): Flow<Result<List<LessonProgress>>> = flowOf(Result.success(emptyList()))
}
