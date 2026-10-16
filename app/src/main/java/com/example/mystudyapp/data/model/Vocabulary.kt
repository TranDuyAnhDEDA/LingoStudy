package com.example.mystudyapp.data.model

data class Vocabulary(
    val id: String,
    val lessonId: String = "",
    val word: String = "",
    val meaning: String = "",
    val meaningVi: String? = null,
    val partOfSpeech: String = "",
    val example: String = "",
    val exampleVi: String? = null,
    val translation: String = "", // Keeping for backwards compatibility
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val order: Int = 0
)
