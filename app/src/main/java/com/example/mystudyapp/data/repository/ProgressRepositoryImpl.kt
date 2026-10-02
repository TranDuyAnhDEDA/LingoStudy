package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.mapper.asFlow
import com.example.mystudyapp.data.mapper.mapFirestoreException
import com.example.mystudyapp.data.mapper.toFirestoreMap
import com.example.mystudyapp.data.mapper.toLesson
import com.example.mystudyapp.data.mapper.toLessonProgress
import com.example.mystudyapp.data.mapper.toUnitModel
import com.example.mystudyapp.data.mapper.toUserProgress
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProgress
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class ProgressRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProgressRepository {

    override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> {
        if (uid.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("user_progress")
            .document(uid)
            .asFlow { it.toUserProgress() }
    }

    override suspend fun createUserProgress(progress: UserProgress): Result<Unit> {
        return try {
            firestore.collection("user_progress")
                .document(progress.userId)
                .set(progress.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun updateUserProgress(progress: UserProgress): Result<Unit> {
        return try {
            firestore.collection("user_progress")
                .document(progress.userId)
                .set(progress.toFirestoreMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> {
        if (uid.isBlank() || lessonId.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("user_progress")
            .document(uid)
            .collection("lessons")
            .document(lessonId)
            .asFlow { it.toLessonProgress() }
    }

    override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<Unit> {
        if (uid.isBlank() || progress.lessonId.isBlank()) {
            return Result.failure(Exception("Invalid user ID or lesson ID."))
        }
        return try {
            val batch = firestore.batch()

            val lessonRef = firestore.collection("user_progress")
                .document(uid)
                .collection("lessons")
                .document(progress.lessonId)

            batch.set(lessonRef, progress.toFirestoreMap(), SetOptions.merge())

            val parentUpdates = mutableMapOf<String, Any>()
            if (progress.vocabularyCompleted) parentUpdates["vocabularyProgress"] = 100
            if (progress.grammarCompleted) parentUpdates["grammarProgress"] = 100
            if (progress.listeningCompleted) parentUpdates["listeningProgress"] = 100
            if (progress.pronunciationCompleted) parentUpdates["pronunciationProgress"] = 100

            if (parentUpdates.isNotEmpty()) {
                val userProgressRef = firestore.collection("user_progress").document(uid)
                batch.set(userProgressRef, parentUpdates, SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<Unit> {
        if (uid.isBlank() || lessonId.isBlank()) {
            return Result.failure(Exception("Invalid user ID or lesson ID."))
        }
        return try {
            val lessonUpdates = mapOf(
                "completed" to true,
                "progress" to 100,
                "lastAccessedAt" to FieldValue.serverTimestamp()
            )
            val batch = firestore.batch()
            val lessonRef = firestore.collection("user_progress")
                .document(uid)
                .collection("lessons")
                .document(lessonId)
            val userProgressRef = firestore.collection("user_progress").document(uid)

            batch.set(lessonRef, lessonUpdates, SetOptions.merge())
            batch.update(userProgressRef, "completedLessons", FieldValue.increment(1))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> {
        if (uid.isBlank() || lessonId.isBlank()) {
            return Result.failure(Exception("Invalid user ID or lesson ID."))
        }
        return try {
            val lessonProgressDoc = firestore.collection("user_progress")
                .document(uid)
                .collection("lessons")
                .document(lessonId)
                .get()
                .await()

            val progress = lessonProgressDoc.toLessonProgress()
                ?: return Result.success(false)

            val isEligible = progress.vocabularyCompleted &&
                    progress.grammarCompleted &&
                    progress.listeningCompleted &&
                    progress.pronunciationCompleted &&
                    progress.quizCompleted

            if (!isEligible) {
                return Result.success(false)
            }

            if (progress.completed) {
                return Result.success(false)
            }

            val lessonDoc = firestore.collection("lessons").document(lessonId).get().await()
            val lesson = lessonDoc.toLesson()
            val xpReward = lesson?.xpReward ?: 30
            val unitId = lesson?.unitId ?: ""

            val nextLessonId = findNextLessonId(unitId, lessonId) ?: lessonId

            val batch = firestore.batch()

            val lessonRef = firestore.collection("user_progress")
                .document(uid)
                .collection("lessons")
                .document(lessonId)

            batch.set(
                lessonRef,
                mapOf(
                    "completed" to true,
                    "progress" to 100,
                    "xpEarned" to xpReward,
                    "lastAccessedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            val userProgressRef = firestore.collection("user_progress").document(uid)
            batch.set(
                userProgressRef,
                mapOf(
                    "completedLessons" to FieldValue.increment(1),
                    "currentLessonId" to nextLessonId,
                    "vocabularyProgress" to 100,
                    "grammarProgress" to 100,
                    "listeningProgress" to 100,
                    "pronunciationProgress" to 100
                ),
                SetOptions.merge()
            )

            val userProfileRef = firestore.collection("users").document(uid)
            batch.set(
                userProfileRef,
                mapOf(
                    "xp" to FieldValue.increment(xpReward.toLong()),
                    "totalLessonsCompleted" to FieldValue.increment(1),
                    "lastActiveAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    private suspend fun findNextLessonId(unitId: String, currentLessonId: String): String? {
        if (unitId.isBlank()) return null
        return try {
            val lessonsQuery = firestore.collection("lessons")
                .whereEqualTo("unitId", unitId)
                .get()
                .await()

            val sortedLessons = lessonsQuery.documents
                .mapNotNull { it.toLesson() }
                .sortedBy { it.order }

            val currentIndex = sortedLessons.indexOfFirst { it.id == currentLessonId }
            if (currentIndex in 0 until sortedLessons.size - 1) {
                return sortedLessons[currentIndex + 1].id
            }

            val unitDoc = firestore.collection("units").document(unitId).get().await()
            val levelId = unitDoc.getString("levelId") ?: "level_3"

            val unitsQuery = firestore.collection("units")
                .whereEqualTo("levelId", levelId)
                .get()
                .await()

            val sortedUnits = unitsQuery.documents
                .mapNotNull { it.toUnitModel() }
                .sortedBy { it.order }

            val currentUnitIndex = sortedUnits.indexOfFirst { it.id == unitId }
            if (currentUnitIndex in 0 until sortedUnits.size - 1) {
                val nextUnitId = sortedUnits[currentUnitIndex + 1].id
                val nextUnitLessonsQuery = firestore.collection("lessons")
                    .whereEqualTo("unitId", nextUnitId)
                    .get()
                    .await()

                val nextUnitSortedLessons = nextUnitLessonsQuery.documents
                    .mapNotNull { it.toLesson() }
                    .sortedBy { it.order }

                return nextUnitSortedLessons.firstOrNull()?.id
            }

            null
        } catch (e: Exception) {
            null
        }
    }
}
