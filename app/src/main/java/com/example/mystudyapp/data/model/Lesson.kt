package com.example.mystudyapp.data.model

data class Lesson(
    val id: String,
    val unitId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val estimatedMinutes: Int = 0,
    val xpReward: Int = 0
)
