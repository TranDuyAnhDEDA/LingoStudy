package com.example.mystudyapp.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PronunciationScorerTest {

    @Test
    fun testExactMatchScores100() {
        val result = PronunciationScorer.evaluate("apple", listOf("apple"))
        assertEquals(100, result.score)
        assertEquals(PronunciationGrade.EXCELLENT, result.grade)
        assertEquals(3, result.stars)
        assertTrue(result.characterMatches.all { it.isMatch })
    }

    @Test
    fun testExactMatchCaseInsensitive() {
        val result = PronunciationScorer.evaluate("Hello", listOf("hello"))
        assertEquals(100, result.score)
        assertEquals(PronunciationGrade.EXCELLENT, result.grade)
    }

    @Test
    fun testCandidateWithArticleScoresHigh() {
        val result = PronunciationScorer.evaluate("cat", listOf("a cat", "the cat"))
        assertTrue("Score should be >= 90 when target word is present", result.score >= 90)
    }

    @Test
    fun testClosePronunciationScoresGood() {
        val result = PronunciationScorer.evaluate("elephant", listOf("elefant"))
        assertTrue("Score for elefant should be >= 75", result.score >= 75)
    }

    @Test
    fun testWrongWordScoresLow() {
        val result = PronunciationScorer.evaluate("banana", listOf("dog"))
        assertTrue("Score for completely wrong word should be < 50", result.score < 50)
        assertEquals(PronunciationGrade.NEEDS_PRACTICE, result.grade)
    }

    @Test
    fun testEmptyInputHandledGracefully() {
        val result = PronunciationScorer.evaluate("", emptyList())
        assertEquals(0, result.score)
        assertEquals(PronunciationGrade.NEEDS_PRACTICE, result.grade)
    }
}
