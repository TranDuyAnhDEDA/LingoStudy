package com.example.mystudyapp.data.model

data class UserLevelInfo(
    val levelOrder: Int,
    val levelName: String,
    val currentLevelXp: Int,       // XP progress within this level (resets to 0 upon reaching new level!)
    val levelRequiredXp: Int,      // Additional XP required within this level (Level 1: 200, Level 2: 250, Level 3: 300, ...)
    val cumulativeBaseXp: Int,     // Cumulative XP at the start of this level (Level 1: 0, Level 2: 200, Level 3: 450, ...)
    val nextLevelThresholdXp: Int  // Cumulative XP needed to reach next level (Level 1: 200, Level 2: 450, Level 3: 750, ...)
) {
    // Backwards compatibility for requiredXp
    val requiredXp: Int
        get() = levelRequiredXp
}

object UserLevelCalculator {
    const val BASE_LEVEL_1_XP = 200
    const val INCREMENT_STEP = 50

    /**
     * Determines the user's level, level progress, and cumulative thresholds:
     * - Level 1: 0..199 XP. Needs 200 XP to reach Level 2 (progress: 0..199 / 200).
     * - Level 2: 200..449 XP. Reaching 200 XP RESETS the level bar!
     *            Needs an additional 250 XP to reach Level 3 (threshold = 450 XP, progress: 0..249 / 250).
     * - Level 3: 450..749 XP. Reaching 450 XP RESETS the level bar!
     *            Needs an additional 300 XP to reach Level 4 (threshold = 750 XP, progress: 0..299 / 300).
     * - Level 4: 750..1099 XP. Needs an additional 350 XP (threshold = 1100 XP).
     * - Level 5: 1100..1499 XP. Needs an additional 400 XP (threshold = 1500 XP).
     *
     * In general, for Level k >= 1:
     *   XP needed in Level k = 200 + (k - 1) * 50
     *   Cumulative threshold to reach Level L = sum_{k=1}^{L-1} [200 + (k - 1) * 50]
     */
    fun calculate(userXp: Int, customLevelName: String? = null): UserLevelInfo {
        val safeXp = userXp.coerceAtLeast(0)

        var currentLevel = 1
        var cumulativeBase = 0
        var neededInLevel = BASE_LEVEL_1_XP

        while (true) {
            val nextThreshold = cumulativeBase + neededInLevel
            if (safeXp < nextThreshold) {
                val currentLevelXp = safeXp - cumulativeBase
                val resolvedName = if (customLevelName.isNullOrBlank()) {
                    if (currentLevel == 1) "Cấp 1: Người mới bắt đầu" else "Cấp $currentLevel"
                } else if (currentLevel == 1 || customLevelName.contains("$currentLevel")) {
                    customLevelName
                } else {
                    "Cấp $currentLevel"
                }

                return UserLevelInfo(
                    levelOrder = currentLevel,
                    levelName = resolvedName,
                    currentLevelXp = currentLevelXp,
                    levelRequiredXp = neededInLevel,
                    cumulativeBaseXp = cumulativeBase,
                    nextLevelThresholdXp = nextThreshold
                )
            }

            cumulativeBase = nextThreshold
            currentLevel++
            neededInLevel = BASE_LEVEL_1_XP + (currentLevel - 1) * INCREMENT_STEP
        }
    }

    /**
     * Returns the cumulative XP threshold required to reach a specific level order.
     * Level 1 -> 0 XP
     * Level 2 -> 200 XP
     * Level 3 -> 450 XP (200 + 250)
     * Level 4 -> 750 XP (450 + 300)
     * Level 5 -> 1100 XP (750 + 350)
     */
    fun xpThresholdForLevel(levelOrder: Int): Int {
        if (levelOrder <= 1) return 0
        var threshold = 0
        for (k in 1 until levelOrder) {
            threshold += BASE_LEVEL_1_XP + (k - 1) * INCREMENT_STEP
        }
        return threshold
    }

    /**
     * Returns the additional XP required in a specific level to reach the next level.
     * Level 1 -> 200 XP
     * Level 2 -> 250 XP
     * Level 3 -> 300 XP
     * Level 4 -> 350 XP
     */
    fun xpRequiredInLevel(levelOrder: Int): Int {
        val safeOrder = levelOrder.coerceAtLeast(1)
        return BASE_LEVEL_1_XP + (safeOrder - 1) * INCREMENT_STEP
    }
}
