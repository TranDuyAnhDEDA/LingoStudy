package com.example.mystudyapp.data.model

data class Grammar(
    val id: String,
    val lessonId: String = "",
    val title: String = "",
    val explanation: String = "",
    val examples: List<String> = emptyList(),
    val order: Int = 0
)
