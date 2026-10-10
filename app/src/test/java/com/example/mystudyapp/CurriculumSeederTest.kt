package com.example.mystudyapp

import com.example.mystudyapp.data.seed.BeginnerCurriculumData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumSeederTest {

    @Test
    fun testBeginnerCurriculumDataStructure() {
        val program = BeginnerCurriculumData.program
        assertEquals("program_beginner", program.id)
        assertEquals("Beginner English", program.title)
        assertTrue(program.isAvailable)

        val level = BeginnerCurriculumData.level
        assertEquals("level_beginner_1", level.id)
        assertEquals("program_beginner", level.programId)

        val units = BeginnerCurriculumData.units
        assertEquals("10 Units required for Beginner English curriculum", 10, units.size)

        val lessons = BeginnerCurriculumData.lessons
        assertEquals("30 Lessons required across 10 Units", 30, lessons.size)

        // Verify every lesson belongs to a valid unit
        val unitIds = units.map { it.id }.toSet()
        lessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} must belong to a valid unit", unitIds.contains(lesson.unitId))
            assertEquals("program_beginner", lesson.programId)
        }
    }

    @Test
    fun testSampleSkillContentLinkedToLessons() {
        val vocabularies = BeginnerCurriculumData.sampleVocabularies
        val grammars = BeginnerCurriculumData.sampleGrammars
        val listenings = BeginnerCurriculumData.sampleListenings
        val pronunciations = BeginnerCurriculumData.samplePronunciations
        val quizzes = BeginnerCurriculumData.sampleQuizzes

        val lessonIds = BeginnerCurriculumData.lessons.map { it.id }.toSet()

        vocabularies.forEach { vocab ->
            assertTrue("Vocabulary ${vocab.id} must belong to a valid lesson", lessonIds.contains(vocab.lessonId))
            assertNotNull(vocab.word)
            assertNotNull(vocab.meaning)
        }

        grammars.forEach { grammar ->
            assertTrue("Grammar ${grammar.id} must belong to a valid lesson", lessonIds.contains(grammar.lessonId))
            assertNotNull(grammar.title)
            assertNotNull(grammar.explanation)
        }

        listenings.forEach { listening ->
            assertTrue("Listening ${listening.id} must belong to a valid lesson", lessonIds.contains(listening.lessonId))
            assertNotNull(listening.transcript)
        }

        pronunciations.forEach { pronun ->
            assertTrue("Pronunciation ${pronun.id} must belong to a valid lesson", lessonIds.contains(pronun.lessonId))
            assertNotNull(pronun.word)
            assertNotNull(pronun.phonetic)
        }

        quizzes.forEach { quiz ->
            assertTrue("Quiz ${quiz.id} must belong to a valid lesson", lessonIds.contains(quiz.lessonId))
            assertNotNull(quiz.question)
            assertTrue(quiz.options.contains(quiz.correctAnswer))
        }
    }
}
