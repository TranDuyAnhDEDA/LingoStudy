package com.example.mystudyapp.data.model

data class Quiz(
    val id: String,
    val lessonId: String = "",
    val type: String = "", // e.g. "multiple_choice", "fill_in_blank"
    val question: String = "",
    val questionVi: String? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanationVi: String? = null,
    val xpReward: Int = 10,
    val order: Int = 0
)
