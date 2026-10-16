package com.example.mystudyapp.data.model

data class Lesson(
    val id: String,
    val unitId: String = "",
    val programId: String = "",
    val title: String = "",
    val titleVi: String? = null,
    val description: String = "",
    val descriptionVi: String? = null,
    val order: Int = 0,
    val estimatedMinutes: Int = 0,
    val xpReward: Int = 0,
    val learningObjectives: List<String> = emptyList(),
    val learningObjectivesVi: List<String>? = null,
    val isPublished: Boolean = true
)
