package com.example.mystudyapp.data.model

data class Level(
    val id: String,
    val programId: String = "",
    val name: String = "",
    val nameVi: String? = null,
    val description: String = "",
    val descriptionVi: String? = null,
    val order: Int = 0,
    val xpRequired: Int = 0
)
