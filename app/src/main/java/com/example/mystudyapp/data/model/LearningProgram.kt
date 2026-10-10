package com.example.mystudyapp.data.model

data class LearningProgram(
    val id: String,
    val title: String = "",
    val description: String = "",
    val targetAudience: String = "",
    val learningObjectives: List<String> = emptyList(),
    val order: Int = 0,
    val isPublished: Boolean = true,
    val iconEmoji: String = "",
    val levelIds: List<String> = emptyList(),
    val isAvailable: Boolean = true
)
