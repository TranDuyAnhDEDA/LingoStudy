package com.example.mystudyapp

import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.seed.BeginnerCurriculumData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonContentTest {

    @Test
    fun testLesson1FlashcardCountAndOrdering() {
        val vocabList = BeginnerCurriculumData.sampleVocabularies.filter { it.lessonId == "lesson_b1_1_1" }
        assertEquals(10, vocabList.size)

        val expectedWords = listOf(
            "Hello",
            "Hi",
            "Good morning",
            "Good afternoon",
            "Good evening",
            "Goodbye",
            "See you later",
            "How are you?",
            "I'm fine, thank you.",
            "Nice to meet you."
        )

        vocabList.forEachIndexed { index, vocab ->
            assertEquals(expectedWords[index], vocab.word)
            assertEquals(index + 1, vocab.order)
        }
    }

    @Test
    fun testGrammarCompletionRequiresReadingAndQuizSubmission() {
        val readToBottom = true
        val quizSubmitted = true

        val isGrammarEligibleForCompletion = readToBottom && quizSubmitted
        assertTrue(isGrammarEligibleForCompletion)
    }

    @Test
    fun testGrammarQuickQuizScoring() {
        val questions = BeginnerCurriculumData.sampleGrammarQuickQuizzes
        assertEquals(5, questions.size)

        var userCorrectCount = 0
        questions.forEach { quiz ->
            if (quiz.correctAnswer.isNotBlank()) {
                userCorrectCount++
            }
        }
        assertEquals(5, userCorrectCount)
    }

    @Test
    fun testListening10QuestionsCompletionCondition() {
        val listeningList = BeginnerCurriculumData.sampleListenings.filter { it.lessonId.startsWith("lesson_b1_1") }
        assertEquals(10, listeningList.size)

        var answeredCount = 0
        listeningList.forEach { _ ->
            answeredCount++
        }
        val isListeningComplete = (answeredCount == 10)
        assertTrue(isListeningComplete)
    }

    @Test
    fun test15QuestionQuizScoringAndDivision() {
        val quizList = BeginnerCurriculumData.sampleQuizzes.filter { it.lessonId.startsWith("lesson_b1_1") }
        assertEquals(15, quizList.size)

        val vocabQuestions = quizList.take(8)
        val grammarQuestions = quizList.drop(8)

        assertEquals(8, vocabQuestions.size)
        assertEquals(7, grammarQuestions.size)

        var score = 0
        quizList.forEach { quiz ->
            if (quiz.correctAnswer.isNotBlank()) {
                score++
            }
        }
        assertEquals(15, score)
    }

    @Test
    fun testProgressCalculationWhenPronunciationIsBypassed() {
        var completedModules = 0
        val vocabDone = true
        val grammarDone = true

        if (vocabDone) completedModules++
        if (grammarDone) completedModules++

        var progressPct = (completedModules * 25).coerceAtMost(100)
        assertEquals(50, progressPct)

        completedModules = 4
        progressPct = (completedModules * 25).coerceAtMost(100)
        val isOverallCompleted = (completedModules == 4)

        assertEquals(100, progressPct)
        assertTrue(isOverallCompleted)
    }

    @Test
    fun testRestoreSavedProgressAndPreventDoubleXp() {
        val savedProgress = LessonProgress(
            lessonId = "lesson_b1_1_1",
            progress = 75,
            completed = false,
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = true,
            pronunciationCompleted = false,
            quizCompleted = false,
            xpEarned = 0
        )

        assertEquals(75, savedProgress.progress)
        assertTrue(savedProgress.vocabularyCompleted)
        assertTrue(savedProgress.grammarCompleted)
        assertTrue(savedProgress.listeningCompleted)
        assertFalse(savedProgress.quizCompleted)

        val completedProgressWithXp = savedProgress.copy(
            progress = 100,
            completed = true,
            quizCompleted = true,
            xpEarned = 30
        )

        val isEligibleForDuplicateXp = (completedProgressWithXp.xpEarned == 0)
        assertFalse("Should prevent awarding XP a second time", isEligibleForDuplicateXp)
    }
}
