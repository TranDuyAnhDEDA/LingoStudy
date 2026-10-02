package com.example.mystudyapp.data.model

data class Listening(
    val id: String,
    val lessonId: String = "",
    val title: String = "",
    val audioUrl: String? = null,
    val transcript: String = "",
    val difficulty: String = "",
    val order: Int = 0
)
