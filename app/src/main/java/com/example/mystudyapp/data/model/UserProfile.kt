package com.example.mystudyapp.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val levelId: String = "level_3",
    val levelName: String = "Explorer",
    val xp: Int = 0,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessonsCompleted: Int = 0,
    val createdAt: Timestamp? = null,
    val lastActiveAt: Timestamp? = null
)
