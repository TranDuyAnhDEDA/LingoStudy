package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.mapper.asFlow
import com.example.mystudyapp.data.mapper.mapFirestoreException
import com.example.mystudyapp.data.mapper.toDailyStudyTime
import com.example.mystudyapp.data.mapper.toFirestoreMap
import com.example.mystudyapp.data.mapper.toLesson
import com.example.mystudyapp.data.mapper.toLessonProgress
import com.example.mystudyapp.data.mapper.toUnitModel
import com.example.mystudyapp.data.mapper.toUserProgress
import com.example.mystudyapp.data.mapper.toUserProfile
import com.example.mystudyapp.data.model.DailyStudyTime
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.seed.BeginnerCurriculumData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            val lessonDocRef = firestore.collection("user_progress")
                .document(uid)
                .collection("lessons")
                .document(progress.lessonId)

            val existingSnapshot = lessonDocRef.get().await()
            val existingProgress = existingSnapshot.toLessonProgress()

            val vocabDone = progress.vocabularyCompleted || (existingProgress?.vocabularyCompleted == true)
            val grammarDone = progress.grammarCompleted || (existingProgress?.grammarCompleted == true)
            val listeningDone = progress.listeningCompleted || (existingProgress?.listeningCompleted == true)
            val pronunDone = progress.pronunciationCompleted || (existingProgress?.pronunciationCompleted == true)
            val quizDone = progress.quizCompleted || (existingProgress?.quizCompleted == true)

            var completedModulesCount = 0
            if (vocabDone) completedModulesCount++
            if (grammarDone) completedModulesCount++
            if (listeningDone) completedModulesCount++
            if (quizDone) completedModulesCount++

            val calculatedProgressPct = (completedModulesCount * 25).coerceAtMost(100)
            val isOverallCompleted = (completedModulesCount == 4) || (existingProgress?.completed == true)

            val lastVocabIndex = if (progress.lastVocabularyIndex > 0) progress.lastVocabularyIndex else (existingProgress?.lastVocabularyIndex ?: 0)
            val lastGrammarIndex = if (progress.lastGrammarIndex > 0) progress.lastGrammarIndex else (existingProgress?.lastGrammarIndex ?: 0)
            val lastListeningIndex = if (progress.lastListeningIndex > 0) progress.lastListeningIndex else (existingProgress?.lastListeningIndex ?: 0)
            val lastPronunIndex = if (progress.lastPronunciationIndex > 0) progress.lastPronunciationIndex else (existingProgress?.lastPronunciationIndex ?: 0)
            val lastQuizIndex = if (progress.lastQuizIndex > 0) progress.lastQuizIndex else (existingProgress?.lastQuizIndex ?: 0)
            val quizScore = if (progress.quizScore > 0) progress.quizScore else (existingProgress?.quizScore ?: 0)
            val preservedXpEarned = if ((existingProgress?.xpEarned ?: 0) > 0) (existingProgress?.xpEarned ?: 0) else progress.xpEarned

            val mergedProgress = LessonProgress(
                lessonId = progress.lessonId,
                progress = calculatedProgressPct,
                completed = isOverallCompleted,
                vocabularyCompleted = vocabDone,
                grammarCompleted = grammarDone,
                listeningCompleted = listeningDone,
                pronunciationCompleted = pronunDone,
                quizCompleted = quizDone,
                lastVocabularyIndex = lastVocabIndex,
                lastGrammarIndex = lastGrammarIndex,
                lastListeningIndex = lastListeningIndex,
                lastPronunciationIndex = lastPronunIndex,
                lastQuizIndex = lastQuizIndex,
                quizScore = quizScore,
                xpEarned = preservedXpEarned
            )

            val batch = firestore.batch()
            batch.set(lessonDocRef, mergedProgress.toFirestoreMap(), SetOptions.merge())

            val parentUpdates = mutableMapOf<String, Any>()
            if (vocabDone) parentUpdates["vocabularyProgress"] = 100
            if (grammarDone) parentUpdates["grammarProgress"] = 100
            if (listeningDone) parentUpdates["listeningProgress"] = 100
            if (pronunDone) parentUpdates["pronunciationProgress"] = 100

            if (parentUpdates.isNotEmpty()) {
                val userProgressRef = firestore.collection("user_progress").document(uid)
                batch.set(userProgressRef, parentUpdates, SetOptions.merge())
            }

            batch.commit().await()
            if (isOverallCompleted && preservedXpEarned == 0) {
                completeLessonIfEligible(uid, progress.lessonId)
            }
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

            val isEligible = (progress.vocabularyCompleted &&
                    progress.grammarCompleted &&
                    progress.listeningCompleted &&
                    progress.quizCompleted) || progress.completed

            if (!isEligible) {
                return Result.success(false)
            }

            // Only skip if XP was ALREADY awarded for this lesson (prevent duplicate rewards)
            if (progress.xpEarned > 0) {
                return Result.success(false)
            }

            val lessonDoc = try {
                firestore.collection("lessons").document(lessonId).get().await()
            } catch (e: Exception) {
                null
            }
            val lesson = lessonDoc?.toLesson() ?: BeginnerCurriculumData.lessons.firstOrNull { it.id == lessonId }
            val xpReward = lesson?.xpReward ?: 30
            val unitId = lesson?.unitId ?: (BeginnerCurriculumData.lessons.firstOrNull { it.id == lessonId }?.unitId ?: "")

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
                    "currentActivity" to FieldValue.delete(),
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
            checkAndUpdateStreak(uid)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    private suspend fun findNextLessonId(unitId: String, currentLessonId: String): String? {
        if (unitId.isBlank()) return null
        return try {
            val firestoreLessons = try {
                val lessonsQuery = firestore.collection("lessons")
                    .whereEqualTo("unitId", unitId)
                    .get()
                    .await()
                lessonsQuery.documents.mapNotNull { it.toLesson() }
            } catch (e: Exception) {
                emptyList()
            }

            val seedLessons = BeginnerCurriculumData.lessons.filter { it.unitId == unitId }
            val sortedLessons = (firestoreLessons + seedLessons).distinctBy { it.id }.sortedBy { it.order }

            val currentIndex = sortedLessons.indexOfFirst { it.id == currentLessonId }
            if (currentIndex in 0 until sortedLessons.size - 1) {
                return sortedLessons[currentIndex + 1].id
            }

            val unitDoc = try { firestore.collection("units").document(unitId).get().await() } catch (e: Exception) { null }
            val levelId = unitDoc?.getString("levelId") ?: (BeginnerCurriculumData.units.firstOrNull { it.id == unitId }?.levelId ?: "level_beginner_1")

            val firestoreUnits = try {
                val unitsQuery = firestore.collection("units")
                    .whereEqualTo("levelId", levelId)
                    .get()
                    .await()
                unitsQuery.documents.mapNotNull { it.toUnitModel() }
            } catch (e: Exception) {
                emptyList()
            }

            val seedUnits = BeginnerCurriculumData.units.filter { it.levelId == levelId }
            val sortedUnits = (firestoreUnits + seedUnits).distinctBy { it.id }.sortedBy { it.order }

            val currentUnitIndex = sortedUnits.indexOfFirst { it.id == unitId }
            if (currentUnitIndex in 0 until sortedUnits.size - 1) {
                val nextUnitId = sortedUnits[currentUnitIndex + 1].id
                val nextUnitFirestoreLessons = try {
                    val nextUnitLessonsQuery = firestore.collection("lessons")
                        .whereEqualTo("unitId", nextUnitId)
                        .get()
                        .await()
                    nextUnitLessonsQuery.documents.mapNotNull { it.toLesson() }
                } catch (e: Exception) {
                    emptyList()
                }

                val nextUnitSeedLessons = BeginnerCurriculumData.lessons.filter { it.unitId == nextUnitId }
                val nextUnitSortedLessons = (nextUnitFirestoreLessons + nextUnitSeedLessons).distinctBy { it.id }.sortedBy { it.order }

                return nextUnitSortedLessons.firstOrNull()?.id
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateCurrentLearningPosition(
        uid: String,
        lessonId: String,
        activity: String?,
        index: Int
    ): Result<Unit> {
        if (uid.isBlank() || lessonId.isBlank()) {
            return Result.failure(Exception("Invalid user ID or lesson ID."))
        }
        return try {
            val userProgressRef = firestore.collection("user_progress").document(uid)
            val lessonProgressDoc = try {
                firestore.collection("user_progress")
                    .document(uid)
                    .collection("lessons")
                    .document(lessonId)
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }
            val isLessonCompleted = lessonProgressDoc?.getBoolean("completed") ?: false

            val updates = mutableMapOf<String, Any>(
                "lastActivityIndex" to index,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            // Do not regress currentLessonId if this lesson was already completed
            if (!isLessonCompleted) {
                updates["currentLessonId"] = lessonId
            }
            if (activity != null) {
                updates["currentActivity"] = activity
            }

            userProgressRef.set(updates, SetOptions.merge()).await()
            checkAndUpdateStreak(uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override suspend fun recordStudyTime(uid: String, seconds: Long, dateString: String): Result<Unit> {
        if (uid.isBlank() || seconds <= 0 || dateString.isBlank()) {
            return Result.success(Unit)
        }
        return try {
            val docRef = firestore.collection("user_study_time")
                .document(uid)
                .collection("daily")
                .document(dateString)

            val updates = mapOf(
                "date" to dateString,
                "totalSeconds" to FieldValue.increment(seconds),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(updates, SetOptions.merge()).await()
            checkAndUpdateStreak(uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirestoreException(e))
        }
    }

    override fun getDailyStudyTime(uid: String, dateString: String): Flow<Result<DailyStudyTime?>> {
        if (uid.isBlank() || dateString.isBlank()) return flowOf(Result.success(null))
        return firestore.collection("user_study_time")
            .document(uid)
            .collection("daily")
            .document(dateString)
            .asFlow { it.toDailyStudyTime() }
    }

    override fun getLast7DaysStudyTime(uid: String): Flow<Result<List<DailyStudyTime>>> {
        if (uid.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("user_study_time")
            .document(uid)
            .collection("daily")
            .asFlow { it.toDailyStudyTime() }
            .map { result ->
                result.map { list ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val last7Dates = (6 downTo 0).map { i ->
                        val cal = calendar.clone() as Calendar
                        cal.add(Calendar.DAY_OF_YEAR, -i)
                        sdf.format(cal.time)
                    }

                    val studyTimeMap = list.associateBy { it.date }
                    last7Dates.map { date ->
                        studyTimeMap[date] ?: DailyStudyTime(date = date, totalSeconds = 0L)
                    }
                }
            }
    }

    override fun getAllTimeStudyTime(uid: String): Flow<Result<Long>> {
        if (uid.isBlank()) return flowOf(Result.success(0L))
        return firestore.collection("user_study_time")
            .document(uid)
            .collection("daily")
            .asFlow { it.toDailyStudyTime() }
            .map { result ->
                result.map { list ->
                    list.sumOf { it.totalSeconds }
                }
            }
    }

    override fun getLearningHistory(uid: String): Flow<Result<List<LessonProgress>>> {
        if (uid.isBlank()) return flowOf(Result.success(emptyList()))
        return firestore.collection("user_progress")
            .document(uid)
            .collection("lessons")
            .asFlow { it.toLessonProgress() }
            .map { result ->
                result.map { list ->
                    list.sortedWith(
                        compareByDescending<LessonProgress> { it.completed }
                            .thenByDescending { it.lastAccessedAt?.toDate() }
                    )
                }
            }
    }

    private suspend fun checkAndUpdateStreak(uid: String) {
        if (uid.isBlank()) return
        try {
            val userRef = firestore.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val userProfile = userSnap.toUserProfile() ?: return

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val now = Calendar.getInstance()
            val todayStr = sdf.format(now.time)

            val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = sdf.format(calYesterday.time)

            val lastActiveDate = userSnap.getString("lastActiveDate")
            val currentStreak = userProfile.streak
            val longestStreak = userProfile.longestStreak

            if (lastActiveDate == todayStr) {
                if (currentStreak == 0) {
                    userRef.set(
                        mapOf(
                            "streak" to 1,
                            "longestStreak" to maxOf(longestStreak, 1),
                            "lastActiveDate" to todayStr
                        ),
                        SetOptions.merge()
                    ).await()
                }
            } else if (lastActiveDate == yesterdayStr) {
                val newStreak = currentStreak + 1
                val newLongest = maxOf(longestStreak, newStreak)
                userRef.set(
                    mapOf(
                        "streak" to newStreak,
                        "longestStreak" to newLongest,
                        "lastActiveDate" to todayStr
                    ),
                    SetOptions.merge()
                ).await()
            } else {
                val newStreak = 1
                val newLongest = maxOf(longestStreak, 1)
                userRef.set(
                    mapOf(
                        "streak" to newStreak,
                        "longestStreak" to newLongest,
                        "lastActiveDate" to todayStr
                    ),
                    SetOptions.merge()
                ).await()
            }
        } catch (e: Exception) {
            // Ignore streak update failure
        }
    }
}
