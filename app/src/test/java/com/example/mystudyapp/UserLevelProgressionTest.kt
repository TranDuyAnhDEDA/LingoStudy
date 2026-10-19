package com.example.mystudyapp

import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserLevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLevelProgressionTest {

    @Test
    fun testLevel1Boundaries() {
        // User with 0 XP is Level 1, needs 200 XP to reach Level 2
        val zeroXp = UserLevelCalculator.calculate(0)
        assertEquals(1, zeroXp.levelOrder)
        assertEquals(0, zeroXp.currentLevelXp)
        assertEquals(200, zeroXp.levelRequiredXp)
        assertEquals(200, zeroXp.nextLevelThresholdXp)
        assertEquals("Cấp 1: Người mới bắt đầu", zeroXp.levelName)

        // User with 150 XP is Level 1, progress 150 / 200 XP
        val partialXp = UserLevelCalculator.calculate(150)
        assertEquals(1, partialXp.levelOrder)
        assertEquals(150, partialXp.currentLevelXp)
        assertEquals(200, partialXp.levelRequiredXp)
        assertEquals(200, partialXp.nextLevelThresholdXp)

        // User with 199 XP is still Level 1, needs 200 XP
        val justBelow200 = UserLevelCalculator.calculate(199)
        assertEquals(1, justBelow200.levelOrder)
        assertEquals(199, justBelow200.currentLevelXp)
        assertEquals(200, justBelow200.levelRequiredXp)
        assertEquals(200, justBelow200.nextLevelThresholdXp)

        // Negative XP handled safely
        val negativeXp = UserLevelCalculator.calculate(-50)
        assertEquals(1, negativeXp.levelOrder)
        assertEquals(0, negativeXp.currentLevelXp)
        assertEquals(200, negativeXp.levelRequiredXp)
    }

    @Test
    fun testLevel2BoundariesAndBarReset() {
        // Reaching 200 XP promotes user to Level 2.
        // The level progress bar RESETS to 0, and user must earn an additional 250 XP (threshold = 450 XP).
        val exact200 = UserLevelCalculator.calculate(200)
        assertEquals(2, exact200.levelOrder)
        assertEquals(0, exact200.currentLevelXp) // Bar reset to 0!
        assertEquals(250, exact200.levelRequiredXp)
        assertEquals(200, exact200.cumulativeBaseXp)
        assertEquals(450, exact200.nextLevelThresholdXp)
        assertEquals("Cấp 2", exact200.levelName)

        // At 300 XP, user has earned 100 XP within Level 2 (100 / 250 XP)
        val midLevel2 = UserLevelCalculator.calculate(300)
        assertEquals(2, midLevel2.levelOrder)
        assertEquals(100, midLevel2.currentLevelXp)
        assertEquals(250, midLevel2.levelRequiredXp)
        assertEquals(450, midLevel2.nextLevelThresholdXp)

        // 449 XP is still Level 2
        val justBelow450 = UserLevelCalculator.calculate(449)
        assertEquals(2, justBelow450.levelOrder)
        assertEquals(249, justBelow450.currentLevelXp)
        assertEquals(250, justBelow450.levelRequiredXp)
        assertEquals(450, justBelow450.nextLevelThresholdXp)
    }

    @Test
    fun testLevel3AndSubsequentLevelsCumulativeProgression() {
        // Reaching 450 XP promotes user to Level 3.
        // Bar RESETS to 0, needs an additional 300 XP (threshold = 750 XP).
        val level3Start = UserLevelCalculator.calculate(450)
        assertEquals(3, level3Start.levelOrder)
        assertEquals(0, level3Start.currentLevelXp) // Bar reset to 0!
        assertEquals(300, level3Start.levelRequiredXp)
        assertEquals(450, level3Start.cumulativeBaseXp)
        assertEquals(750, level3Start.nextLevelThresholdXp)
        assertEquals("Cấp 3", level3Start.levelName)

        // At 550 XP, user has earned 100 XP within Level 3 (100 / 300 XP)
        val level3Mid = UserLevelCalculator.calculate(550)
        assertEquals(3, level3Mid.levelOrder)
        assertEquals(100, level3Mid.currentLevelXp)
        assertEquals(300, level3Mid.levelRequiredXp)
        assertEquals(750, level3Mid.nextLevelThresholdXp)

        // Level 4: Reaching 750 XP promotes user to Level 4. Needs +350 XP (threshold = 1100 XP).
        val level4Start = UserLevelCalculator.calculate(750)
        assertEquals(4, level4Start.levelOrder)
        assertEquals(0, level4Start.currentLevelXp) // Bar reset to 0!
        assertEquals(350, level4Start.levelRequiredXp)
        assertEquals(750, level4Start.cumulativeBaseXp)
        assertEquals(1100, level4Start.nextLevelThresholdXp)
        assertEquals("Cấp 4", level4Start.levelName)

        // Level 5: Reaching 1100 XP promotes user to Level 5. Needs +400 XP (threshold = 1500 XP).
        val level5Start = UserLevelCalculator.calculate(1100)
        assertEquals(5, level5Start.levelOrder)
        assertEquals(0, level5Start.currentLevelXp) // Bar reset to 0!
        assertEquals(400, level5Start.levelRequiredXp)
        assertEquals(1100, level5Start.cumulativeBaseXp)
        assertEquals(1500, level5Start.nextLevelThresholdXp)
    }

    @Test
    fun testXpThresholdForLevelHelper() {
        assertEquals(0, UserLevelCalculator.xpThresholdForLevel(1))
        assertEquals(200, UserLevelCalculator.xpThresholdForLevel(2))
        assertEquals(450, UserLevelCalculator.xpThresholdForLevel(3)) // 200 + 250
        assertEquals(750, UserLevelCalculator.xpThresholdForLevel(4)) // 450 + 300
        assertEquals(1100, UserLevelCalculator.xpThresholdForLevel(5)) // 750 + 350
        assertEquals(1500, UserLevelCalculator.xpThresholdForLevel(6)) // 1100 + 400
    }

    @Test
    fun testXpRequiredInLevelHelper() {
        assertEquals(200, UserLevelCalculator.xpRequiredInLevel(1))
        assertEquals(250, UserLevelCalculator.xpRequiredInLevel(2))
        assertEquals(300, UserLevelCalculator.xpRequiredInLevel(3))
        assertEquals(350, UserLevelCalculator.xpRequiredInLevel(4))
        assertEquals(400, UserLevelCalculator.xpRequiredInLevel(5))
    }

    @Test
    fun testLessonProgressWithFiveModulesAtTwentyPercentEach() {
        val progress0 = LessonProgress("lesson_1")
        assertEquals(0, progress0.progress)
        assertFalse(progress0.completed)
        assertFalse(progress0.gameCompleted)

        // 1 module = 20%
        val progress1 = LessonProgress("lesson_1", vocabularyCompleted = true, progress = 20)
        assertEquals(20, progress1.progress)

        // 2 modules = 40%
        val progress2 = LessonProgress("lesson_1", vocabularyCompleted = true, grammarCompleted = true, progress = 40)
        assertEquals(40, progress2.progress)

        // 3 modules = 60%
        val progress3 = LessonProgress("lesson_1", vocabularyCompleted = true, grammarCompleted = true, listeningCompleted = true, progress = 60)
        assertEquals(60, progress3.progress)

        // 4 modules (vocab, grammar, listening, game) = 80%
        val progress4 = LessonProgress("lesson_1", vocabularyCompleted = true, grammarCompleted = true, listeningCompleted = true, pronunciationCompleted = true, progress = 80)
        assertEquals(80, progress4.progress)
        assertTrue(progress4.gameCompleted)

        // 5 modules (all modules including quiz) = 100%
        val progress5 = LessonProgress(
            lessonId = "lesson_1",
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = true,
            pronunciationCompleted = true,
            quizCompleted = true,
            completed = true,
            progress = 100,
            xpEarned = 30
        )
        assertEquals(100, progress5.progress)
        assertTrue(progress5.completed)
        assertEquals(30, progress5.xpEarned)
    }
}
