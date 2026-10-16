package com.example.mystudyapp.data.model

data class Grammar(
    val id: String,
    val lessonId: String = "",
    val title: String = "",
    val titleVi: String? = null,
    val explanation: String = "",
    val explanationVi: String? = null,
    val examples: List<String> = emptyList(),
    val examplesVi: List<String>? = null,
    val order: Int = 0
)
