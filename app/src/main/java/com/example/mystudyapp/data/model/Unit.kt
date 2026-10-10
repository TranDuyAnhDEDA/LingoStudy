package com.example.mystudyapp.data.model

data class Unit(
    val id: String,
    val levelId: String = "",
    val programId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val learningObjectives: List<String> = emptyList(),
    val estimatedMinutes: Int = 0,
    val imageUrl: String? = null,
    val isPublished: Boolean = true
)
