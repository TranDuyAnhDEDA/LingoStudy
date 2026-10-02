package com.example.mystudyapp.data.model

data class Vocabulary(
    val id: String,
    val lessonId: String = "",
    val word: String = "",
    val meaning: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val translation: String = "",
    val audioUrl: String? = null,
    val imageUrl: String? = null
)
