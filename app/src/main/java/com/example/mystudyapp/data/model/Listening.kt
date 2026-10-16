package com.example.mystudyapp.data.model

data class Listening(
    val id: String,
    val lessonId: String = "",
    val title: String = "",
    val titleVi: String? = null,
    val audioUrl: String? = null,
    val transcript: String = "",
    val transcriptVi: String? = null,
    val difficulty: String = "",
    val order: Int = 0
)
