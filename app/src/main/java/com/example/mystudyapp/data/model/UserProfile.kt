package com.example.mystudyapp.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val levelId: String = "level_beginner_1",
    val levelName: String = "Beginner 1: Nền tảng & Giao tiếp",
    val xp: Int = 0,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessonsCompleted: Int = 0,
    val createdAt: Timestamp? = null,
    val lastActiveAt: Timestamp? = null,
    val avatarId: String = "boy_hoodie",
    val customAvatarPath: String? = null,
    val phoneNumber: String = "",
    val dailyGoalMinutes: Int = 30
)
