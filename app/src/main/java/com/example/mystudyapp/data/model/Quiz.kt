package com.example.mystudyapp.data.model

data class Quiz(
    val id: String,
    val lessonId: String = "",
    val type: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val xpReward: Int = 0
)
