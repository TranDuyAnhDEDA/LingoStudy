package com.example.mystudyapp

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.viewmodel.UnitLessonsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoadmapStarsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFiveSkillStarsDerivedFromLessonProgressFlags() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)

        val testUnit = Unit(id = "unit_3_1", levelId = "level_3", title = "Travel and Tourism")
        val lesson1 = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", order = 1, estimatedMinutes = 8, xpReward = 30)
        val lesson2 = Lesson(id = "lesson_3_1_2", unitId = "unit_3_1", title = "At the Hotel", order = 2, estimatedMinutes = 8, xpReward = 30)

        // Lesson 1 has 2 skills complete (Vocab & Grammar)
        val progress1 = LessonProgress(
            lessonId = "lesson_3_1_1",
            progress = 40,
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = false,
            pronunciationCompleted = false,
            quizCompleted = false
        )

        // Lesson 2 has all 5 skills complete
        val progress2 = LessonProgress(
            lessonId = "lesson_3_1_2",
            progress = 100,
            completed = true,
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = true,
            pronunciationCompleted = true,
            quizCompleted = true
        )

        val fakeLearningRepo = FakeLearningRepo(
            unit = testUnit,
            lessonsMap = mapOf("unit_3_1" to listOf(lesson1, lesson2))
        )

        val fakeProgressRepo = FakeProgressRepo(
            lessonProgressMap = mapOf(
                "lesson_3_1_1" to progress1,
                "lesson_3_1_2" to progress2
            )
        )

        val viewModel = UnitLessonsViewModel(
            unitId = "unit_3_1",
            authRepository = fakeAuthRepo,
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.unit)
        assertEquals(2, state.lessons.size)

        // Lesson 1 Verification
        val lesson1Item = state.lessons[0]
        assertEquals("lesson_3_1_1", lesson1Item.lesson.id)
        val p1 = lesson1Item.progress
        assertNotNull(p1)
        assertTrue("Vocab star active", p1?.vocabularyCompleted == true)
        assertTrue("Grammar star active", p1?.grammarCompleted == true)
        assertFalse("Listening star inactive", p1?.listeningCompleted == true)
        assertFalse("Pronunciation star inactive", p1?.pronunciationCompleted == true)
        assertFalse("Quiz star inactive", p1?.quizCompleted == true)

        // Lesson 2 Verification
        val lesson2Item = state.lessons[1]
        assertEquals("lesson_3_1_2", lesson2Item.lesson.id)
        val p2 = lesson2Item.progress
        assertNotNull(p2)
        assertTrue("Lesson 2 Vocab star active", p2?.vocabularyCompleted == true)
        assertTrue("Lesson 2 Grammar star active", p2?.grammarCompleted == true)
        assertTrue("Lesson 2 Listening star active", p2?.listeningCompleted == true)
        assertTrue("Lesson 2 Pronunciation star active", p2?.pronunciationCompleted == true)
        assertTrue("Lesson 2 Quiz star active", p2?.quizCompleted == true)
        assertTrue("Lesson 2 overall complete", p2?.completed == true)
    }

    class FakeAuthRepo(private val user: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String) = Result.failure<AuthUser>(Exception())
        override suspend fun login(email: String, password: String) = Result.failure<AuthUser>(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = user
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(user)
    }

    class FakeProgressRepo(private val lessonProgressMap: Map<String, LessonProgress> = emptyMap()) : ProgressRepository {
        override fun getUserProgress(uid: String) = flowOf(Result.success(null as UserProgress?))
        override suspend fun createUserProgress(progress: UserProgress) = Result.success(Unit)
        override suspend fun updateUserProgress(progress: UserProgress) = Result.success(Unit)
        override fun getLessonProgress(uid: String, lessonId: String) = flowOf(Result.success(lessonProgressMap[lessonId]))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress) = Result.success(Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String) = Result.success(Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String) = Result.success(false)
    }

    class FakeLearningRepo(
        private val unit: Unit? = null,
        private val lessonsMap: Map<String, List<Lesson>> = emptyMap()
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(unit))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(lessonsMap[unitId] ?: emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(null))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
