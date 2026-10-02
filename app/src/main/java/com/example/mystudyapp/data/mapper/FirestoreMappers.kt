package com.example.mystudyapp.data.mapper

import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.model.Vocabulary
import com.google.firebase.firestore.FieldValue
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun mapFirestoreException(e: Throwable): Exception {
    return when (e) {
        is FirebaseFirestoreException -> {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    Exception("Access denied. You do not have permission to perform this action.")
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    Exception("Requested document was not found.")
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    Exception("Database service is temporarily unavailable. Please try again later.")
                FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                    Exception("User is not authenticated. Please log in.")
                else -> Exception("Database error: ${e.localizedMessage ?: "An unexpected error occurred."}")
            }
        }
        is FirebaseNetworkException -> Exception("Network error. Please check your internet connection.")
        else -> Exception(e.localizedMessage ?: "An unexpected error occurred while accessing the database.")
    }
}

fun <T> DocumentReference.asFlow(transform: (DocumentSnapshot) -> T?): Flow<Result<T?>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(Result.failure(mapFirestoreException(error)))
            return@addSnapshotListener
        }
        if (snapshot != null) {
            trySend(Result.success(transform(snapshot)))
        } else {
            trySend(Result.success(null))
        }
    }
    awaitClose { registration.remove() }
}

fun <T> Query.asFlow(transform: (DocumentSnapshot) -> T?): Flow<Result<List<T>>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(Result.failure(mapFirestoreException(error)))
            return@addSnapshotListener
        }
        if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { transform(it) }
            trySend(Result.success(list))
        } else {
            trySend(Result.success(emptyList()))
        }
    }
    awaitClose { registration.remove() }
}

fun DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    return UserProfile(
        uid = id,
        displayName = getString("displayName") ?: "",
        email = getString("email") ?: "",
        levelId = getString("levelId") ?: "",
        levelName = getString("levelName") ?: "",
        xp = getLong("xp")?.toInt() ?: 0,
        streak = getLong("streak")?.toInt() ?: 0,
        longestStreak = getLong("longestStreak")?.toInt() ?: 0,
        totalLessonsCompleted = getLong("totalLessonsCompleted")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt"),
        lastActiveAt = getTimestamp("lastActiveAt")
    )
}

fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
    "displayName" to displayName,
    "email" to email,
    "levelId" to levelId,
    "levelName" to levelName,
    "xp" to xp,
    "streak" to streak,
    "longestStreak" to longestStreak,
    "totalLessonsCompleted" to totalLessonsCompleted,
    "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
    "lastActiveAt" to FieldValue.serverTimestamp()
)

fun DocumentSnapshot.toLevel(): Level? {
    if (!exists()) return null
    return Level(
        id = id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        order = getLong("order")?.toInt() ?: 0,
        xpRequired = getLong("xpRequired")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toUnitModel(): Unit? {
    if (!exists()) return null
    return Unit(
        id = id,
        levelId = getString("levelId") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        order = getLong("order")?.toInt() ?: 0,
        imageUrl = getString("imageUrl")
    )
}

fun DocumentSnapshot.toLesson(): Lesson? {
    if (!exists()) return null
    return Lesson(
        id = id,
        unitId = getString("unitId") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        order = getLong("order")?.toInt() ?: 0,
        estimatedMinutes = getLong("estimatedMinutes")?.toInt() ?: 0,
        xpReward = getLong("xpReward")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toVocabulary(): Vocabulary? {
    if (!exists()) return null
    return Vocabulary(
        id = id,
        lessonId = getString("lessonId") ?: "",
        word = getString("word") ?: "",
        meaning = getString("meaning") ?: "",
        partOfSpeech = getString("partOfSpeech") ?: "",
        example = getString("example") ?: "",
        translation = getString("translation") ?: "",
        audioUrl = getString("audioUrl"),
        imageUrl = getString("imageUrl")
    )
}

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toGrammar(): Grammar? {
    if (!exists()) return null
    return Grammar(
        id = id,
        lessonId = getString("lessonId") ?: "",
        title = getString("title") ?: "",
        explanation = getString("explanation") ?: "",
        examples = (get("examples") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
        order = getLong("order")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toListening(): Listening? {
    if (!exists()) return null
    return Listening(
        id = id,
        lessonId = getString("lessonId") ?: "",
        title = getString("title") ?: "",
        audioUrl = getString("audioUrl"),
        transcript = getString("transcript") ?: "",
        difficulty = getString("difficulty") ?: "",
        order = getLong("order")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toPronunciation(): Pronunciation? {
    if (!exists()) return null
    return Pronunciation(
        id = id,
        lessonId = getString("lessonId") ?: "",
        word = getString("word") ?: "",
        phonetic = getString("phonetic") ?: "",
        audioUrl = getString("audioUrl"),
        targetScore = getLong("targetScore")?.toInt() ?: 0
    )
}

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toQuiz(): Quiz? {
    if (!exists()) return null
    return Quiz(
        id = id,
        lessonId = getString("lessonId") ?: "",
        type = getString("type") ?: "",
        question = getString("question") ?: "",
        options = (get("options") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
        correctAnswer = getString("correctAnswer") ?: "",
        xpReward = getLong("xpReward")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toUserProgress(): UserProgress? {
    if (!exists()) return null
    return UserProgress(
        userId = id,
        vocabularyProgress = getLong("vocabularyProgress")?.toInt() ?: 0,
        grammarProgress = getLong("grammarProgress")?.toInt() ?: 0,
        listeningProgress = getLong("listeningProgress")?.toInt() ?: 0,
        pronunciationProgress = getLong("pronunciationProgress")?.toInt() ?: 0,
        currentLessonId = getString("currentLessonId"),
        completedLessons = getLong("completedLessons")?.toInt() ?: 0,
        totalLessons = getLong("totalLessons")?.toInt() ?: 0
    )
}

fun UserProgress.toFirestoreMap(): Map<String, Any?> = mapOf(
    "vocabularyProgress" to vocabularyProgress,
    "grammarProgress" to grammarProgress,
    "listeningProgress" to listeningProgress,
    "pronunciationProgress" to pronunciationProgress,
    "currentLessonId" to currentLessonId,
    "completedLessons" to completedLessons,
    "totalLessons" to totalLessons
)

fun DocumentSnapshot.toLessonProgress(): LessonProgress? {
    if (!exists()) return null
    return LessonProgress(
        lessonId = id,
        progress = getLong("progress")?.toInt() ?: 0,
        completed = getBoolean("completed") ?: false,
        xpEarned = getLong("xpEarned")?.toInt() ?: 0,
        lastAccessedAt = getTimestamp("lastAccessedAt"),
        vocabularyCompleted = getBoolean("vocabularyCompleted") ?: false,
        grammarCompleted = getBoolean("grammarCompleted") ?: false,
        listeningCompleted = getBoolean("listeningCompleted") ?: false,
        pronunciationCompleted = getBoolean("pronunciationCompleted") ?: false,
        quizCompleted = getBoolean("quizCompleted") ?: false,
        lastVocabularyIndex = getLong("lastVocabularyIndex")?.toInt() ?: 0,
        lastGrammarIndex = getLong("lastGrammarIndex")?.toInt() ?: 0,
        lastListeningIndex = getLong("lastListeningIndex")?.toInt() ?: 0,
        lastPronunciationIndex = getLong("lastPronunciationIndex")?.toInt() ?: 0,
        lastQuizIndex = getLong("lastQuizIndex")?.toInt() ?: 0,
        quizScore = getLong("quizScore")?.toInt() ?: 0
    )
}

fun LessonProgress.toFirestoreMap(): Map<String, Any?> = mapOf(
    "progress" to progress,
    "completed" to completed,
    "xpEarned" to xpEarned,
    "lastAccessedAt" to (lastAccessedAt ?: FieldValue.serverTimestamp()),
    "vocabularyCompleted" to vocabularyCompleted,
    "grammarCompleted" to grammarCompleted,
    "listeningCompleted" to listeningCompleted,
    "pronunciationCompleted" to pronunciationCompleted,
    "quizCompleted" to quizCompleted,
    "lastVocabularyIndex" to lastVocabularyIndex,
    "lastGrammarIndex" to lastGrammarIndex,
    "lastListeningIndex" to lastListeningIndex,
    "lastPronunciationIndex" to lastPronunciationIndex,
    "lastQuizIndex" to lastQuizIndex,
    "quizScore" to quizScore
)
