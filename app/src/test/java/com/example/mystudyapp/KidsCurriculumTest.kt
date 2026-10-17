package com.example.mystudyapp

import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import com.example.mystudyapp.data.seed.KidsCurriculumData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KidsCurriculumTest {

    @Test
    fun testKidsProgramAndLevelHierarchy() {
        val program = KidsCurriculumData.program
        assertEquals("program_kids", program.id)
        assertEquals("English for Kids", program.title)
        assertEquals("Tiếng Anh cho trẻ em", program.titleVi)
        assertTrue("Kids program must be available", program.isAvailable)
        assertTrue("Kids program must be published", program.isPublished)
        assertEquals(listOf("level_kids_1"), program.levelIds)

        val level = KidsCurriculumData.level
        assertEquals("level_kids_1", level.id)
        assertEquals("program_kids", level.programId)
        assertNotNull(level.name)
        assertNotNull(level.nameVi)
        assertEquals(900, level.xpRequired)
    }

    @Test
    fun testKidsUnitsAndLessonsStructure() {
        val units = KidsCurriculumData.units
        assertEquals("Exactly 10 Units required for Kids English curriculum", 10, units.size)

        // Check unit IDs and order
        for (i in 1..10) {
            val unit = units.find { it.id == "unit_kids_$i" }
            assertNotNull("Unit unit_kids_$i must exist", unit)
            assertEquals(i, unit!!.order)
            assertEquals("level_kids_1", unit.levelId)
            assertEquals("program_kids", unit.programId)
            assertTrue("Unit $i title must not be blank", unit.title.isNotBlank())
            assertTrue("Unit $i titleVi must not be blank", (unit.titleVi ?: "").isNotBlank())
            assertTrue("Unit $i must have learning objectives", unit.learningObjectives.isNotEmpty())
            assertTrue("Unit $i must have Vietnamese learning objectives", (unit.learningObjectivesVi ?: emptyList()).isNotEmpty())
        }

        val lessons = KidsCurriculumData.lessons
        assertEquals("Exactly 30 lessons required across 10 Units (3 per Unit)", 30, lessons.size)

        val unitIds = units.map { it.id }.toSet()
        // Check 3 lessons per unit
        units.forEach { unit ->
            val unitLessons = lessons.filter { it.unitId == unit.id }
            assertEquals("Unit ${unit.id} must have exactly 3 lessons", 3, unitLessons.size)
            assertEquals(listOf(1, 2, 3), unitLessons.map { it.order }.sorted())
            unitLessons.forEach { lesson ->
                assertTrue(unitIds.contains(lesson.unitId))
                assertEquals("program_kids", lesson.programId)
                assertEquals(30, lesson.xpReward)
                assertTrue(lesson.title.isNotBlank())
                assertTrue((lesson.titleVi ?: "").isNotBlank())
            }
        }
    }

    @Test
    fun testKidsVocabularyTenPerLesson() {
        val vocabularies = KidsCurriculumData.vocabularies
        assertEquals("Exactly 300 vocabulary items across 30 lessons (10 per lesson)", 300, vocabularies.size)

        val lessons = KidsCurriculumData.lessons
        lessons.forEach { lesson ->
            val lessonVocabs = vocabularies.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have exactly 10 vocabulary items", 10, lessonVocabs.size)
            assertEquals("Vocabulary orders must be 1 to 10", (1..10).toList(), lessonVocabs.map { it.order }.sorted())

            lessonVocabs.forEach { vocab ->
                assertTrue("Word cannot be blank in ${vocab.id}", vocab.word.isNotBlank())
                assertTrue("MeaningVi cannot be blank in ${vocab.id}", (vocab.meaningVi ?: "").isNotBlank())
                assertTrue("Example cannot be blank in ${vocab.id}", vocab.example.isNotBlank())
                assertTrue("ExampleVi cannot be blank in ${vocab.id}", (vocab.exampleVi ?: "").isNotBlank())
            }

            // Check no duplicate words within the same lesson
            val distinctWords = lessonVocabs.map { it.word.lowercase() }.distinct()
            assertEquals("No duplicate words in lesson ${lesson.id}", 10, distinctWords.size)
        }
    }

    @Test
    fun testKidsGrammarAndQuickQuizzes() {
        val grammars = KidsCurriculumData.grammars
        assertEquals("Exactly 30 grammar modules (1 per lesson)", 30, grammars.size)

        val quickQuizzes = KidsCurriculumData.grammarQuickQuizzes
        assertEquals("Exactly 150 grammar quick quizzes (5 per lesson)", 150, quickQuizzes.size)

        val lessons = KidsCurriculumData.lessons
        lessons.forEach { lesson ->
            val lessonGrammars = grammars.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have 1 grammar explanation", 1, lessonGrammars.size)
            val grammar = lessonGrammars.first()
            assertTrue("Grammar title required", grammar.title.isNotBlank())
            assertTrue("Grammar explanationVi required", (grammar.explanationVi ?: "").isNotBlank())
            assertTrue("Grammar must have at least 3 examples", grammar.examples.size >= 3)
            assertTrue("Grammar must have at least 3 examplesVi", (grammar.examplesVi ?: emptyList()).size >= 3)

            val lessonQuickQuizzes = quickQuizzes.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have exactly 5 quick quizzes", 5, lessonQuickQuizzes.size)
            assertEquals(listOf(1, 2, 3, 4, 5), lessonQuickQuizzes.map { it.order }.sorted())

            lessonQuickQuizzes.forEach { quiz ->
                assertEquals("grammar_quick", quiz.type)
                assertTrue("Question must not be blank", quiz.question.isNotBlank())
                assertEquals("Quiz must have 4 options", 4, quiz.options.size)
                assertTrue("Correct answer must be among options for ${quiz.id}", quiz.options.contains(quiz.correctAnswer))
                assertTrue("ExplanationVi must not be blank for ${quiz.id}", (quiz.explanationVi ?: "").isNotBlank())
                assertEquals(2, quiz.xpReward)
            }
        }
    }

    @Test
    fun testKidsListeningMappedToVocabulary() {
        val listenings = KidsCurriculumData.listenings
        assertEquals("Exactly 300 listening exercises (10 per lesson)", 300, listenings.size)

        val lessons = KidsCurriculumData.lessons
        lessons.forEach { lesson ->
            val lessonListenings = listenings.filter { it.lessonId == lesson.id }
            val lessonVocabs = KidsCurriculumData.vocabularies.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have 10 listening items", 10, lessonListenings.size)

            val vocabWords = lessonVocabs.map { it.word }.toSet()
            lessonListenings.forEach { listening ->
                assertTrue(
                    "Listening title '${listening.title}' in lesson ${lesson.id} must match one of the lesson vocabulary words",
                    vocabWords.contains(listening.title)
                )
                assertTrue("Listening transcript cannot be blank", listening.transcript.isNotBlank())
                assertTrue("Listening transcriptVi cannot be blank", (listening.transcriptVi ?: "").isNotBlank())
            }
        }
    }

    @Test
    fun testKidsQuizzesFifteenPerLessonWithEightVocabAndSevenGrammar() {
        val quizzes = KidsCurriculumData.quizzes
        assertEquals("Exactly 450 end-of-lesson quizzes (15 per lesson)", 450, quizzes.size)

        val lessons = KidsCurriculumData.lessons
        lessons.forEach { lesson ->
            val lessonQuizzes = quizzes.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have exactly 15 end-of-lesson quiz questions", 15, lessonQuizzes.size)
            assertEquals("Order must be 1 to 15", (1..15).toList(), lessonQuizzes.map { it.order }.sorted())

            lessonQuizzes.forEach { quiz ->
                assertEquals("multiple_choice", quiz.type)
                assertTrue("Question must not be blank in ${quiz.id}", quiz.question.isNotBlank())
                assertEquals("Quiz must have 4 options in ${quiz.id}", 4, quiz.options.size)
                assertTrue("Correct answer must be among options in ${quiz.id}", quiz.options.contains(quiz.correctAnswer))
                assertTrue("ExplanationVi must not be blank in ${quiz.id}", (quiz.explanationVi ?: "").isNotBlank())
                assertEquals(2, quiz.xpReward)
            }

            // In our generator: odd order (1,3,5,7,9,11,13,15) = 8 vocab questions
            // even order (2,4,6,8,10,12,14) = 7 grammar questions
            val vocabQuizzes = lessonQuizzes.filter { it.order % 2 != 0 }
            val grammarQuizzes = lessonQuizzes.filter { it.order % 2 == 0 }
            assertEquals("Exactly 8 vocabulary questions per lesson", 8, vocabQuizzes.size)
            assertEquals("Exactly 7 grammar questions per lesson", 7, grammarQuizzes.size)
        }
    }

    @Test
    fun testKidsCurriculumHelperMethods() {
        // Verify all 30 lessons have quick quizzes accessible via helper
        KidsCurriculumData.lessons.forEach { lesson ->
            val qq = KidsCurriculumData.getQuickQuizzesByLesson(lesson.id)
            assertEquals("Lesson ${lesson.id} must have exactly 5 quick quizzes", 5, qq.size)
            assertEquals(listOf(1, 2, 3, 4, 5), qq.map { it.order }.sorted())
        }

        // Verify pronunciation coverage across lessons
        val pronunciations = KidsCurriculumData.pronunciations
        assertEquals(30, pronunciations.size)
        KidsCurriculumData.lessons.forEach { lesson ->
            val lessonPron = pronunciations.filter { it.lessonId == lesson.id }
            assertEquals("Lesson ${lesson.id} must have 1 pronunciation item", 1, lessonPron.size)
            assertTrue(lessonPron.first().word.isNotBlank())
            assertTrue(lessonPron.first().phonetic.isNotBlank())
        }
    }
}
