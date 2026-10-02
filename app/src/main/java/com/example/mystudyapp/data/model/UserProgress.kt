package com.example.mystudyapp.data.model

data class UserProgress(
    val userId: String,
    val vocabularyProgress: Int = 0,
    val grammarProgress: Int = 0,
    val listeningProgress: Int = 0,
    val pronunciationProgress: Int = 0,
    val currentLessonId: String? = "lesson_3_1_1",
    val completedLessons: Int = 0,
    val totalLessons: Int = 3
)
