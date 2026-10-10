package com.example.mystudyapp.data.model

data class Pronunciation(
    val id: String,
    val lessonId: String = "",
    val word: String = "",
    val phonetic: String = "",
    val audioUrl: String? = null,
    val targetScore: Int = 0,
    val order: Int = 0
)
