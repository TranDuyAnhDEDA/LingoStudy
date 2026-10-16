package com.example.mystudyapp.data.model

data class LearningProgram(
    val id: String,
    val title: String = "",
    val titleVi: String? = null,
    val description: String = "",
    val descriptionVi: String? = null,
    val targetAudience: String = "",
    val targetAudienceVi: String? = null,
    val learningObjectives: List<String> = emptyList(),
    val learningObjectivesVi: List<String>? = null,
    val order: Int = 0,
    val isPublished: Boolean = true,
    val iconEmoji: String = "",
    val levelIds: List<String> = emptyList(),
    val isAvailable: Boolean = true
)
