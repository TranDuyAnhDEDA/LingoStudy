package com.example.mystudyapp.data.model

import com.google.firebase.Timestamp

data class DailyStudyTime(
    val date: String = "",
    val totalSeconds: Long = 0L,
    val updatedAt: Timestamp? = null
)
