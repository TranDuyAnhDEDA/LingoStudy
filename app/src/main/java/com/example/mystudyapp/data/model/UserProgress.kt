package com.example.mystudyapp.data.model

import com.google.firebase.Timestamp

data class UserProgress(
    val userId: String,
    val vocabularyProgress: Int = 0,
    val grammarProgress: Int = 0,
    val listeningProgress: Int = 0,
    val pronunciationProgress: Int = 0,
    val currentLessonId: String? = "lesson_b1_1_1",
    val completedLessons: Int = 0,
    val totalLessons: Int = 30,
    val currentActivity: String? = null,
    val lastActivityIndex: Int = 0,
    val lastActiveAt: Timestamp? = null
)
