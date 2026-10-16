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
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.viewmodel.QuizViewModel
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
class QuizViewModelTest {

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
    fun testUnauthenticatedUserHandledSafely() {
        val fakeAuthRepo = object : FakeAuthRepository(currentUser = null) {}
        val viewModel = QuizViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(),
            learningRepository = FakeLearningRepository()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun testQuizSelectionCorrectAndIncorrectAnswersAndScoring() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport")

        val q1 = Quiz("q1", "lesson_3_1_1", "multiple_choice", "Question 1?", null, listOf("Opt A", "Opt B", "Opt C"), "Opt A", null, 10, 1)
        val q2 = Quiz("q2", "lesson_3_1_1", "multiple_choice", "Question 2?", null, listOf("Opt A", "Opt B", "Opt C"), "Opt B", null, 10, 2)

        val fakeLearningRepo = FakeLearningRepository(
            lessonMap = mapOf("lesson_3_1_1" to testLesson),
            quizList = listOf(q1, q2)
        )

        val fakeProgressRepo = FakeProgressRepository()

        val viewModel = QuizViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals(2, state.quizList.size)
        assertEquals(0, state.currentIndex)
        assertEquals(0, state.currentScore)

        viewModel.selectOption("Opt A")
        assertEquals("Opt A", viewModel.uiState.value.selectedOption)

        viewModel.submitAnswer()
        state = viewModel.uiState.value
        assertTrue(state.isSubmitted)
        assertTrue(state.isCorrect)
        assertEquals(1, state.currentScore)

        viewModel.submitAnswer()
        assertEquals(1, viewModel.uiState.value.currentScore)

        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        assertFalse(state.isSubmitted)
        assertEquals(1, state.currentScore)

        viewModel.selectOption("Opt A")
        viewModel.submitAnswer()

        state = viewModel.uiState.value
        assertTrue(state.isSubmitted)
        assertFalse(state.isCorrect)
        assertEquals(1, state.currentScore)

        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.currentScore)
    }

    @Test
    fun testResumeBehaviorStartsAtSavedLastQuizIndexAndScore() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport")

        val q1 = Quiz("q1", "lesson_3_1_1", "multiple_choice", "Question 1?", null, listOf("A", "B"), "A", null, 10, 1)
        val q2 = Quiz("q2", "lesson_3_1_1", "multiple_choice", "Question 2?", null, listOf("A", "B"), "B", null, 10, 2)

        val lessonProgress = LessonProgress(
            lessonId = "lesson_3_1_1",
            lastQuizIndex = 1,
            quizScore = 1,
            quizCompleted = false
        )

        val fakeLearningRepo = FakeLearningRepository(
            lessonMap = mapOf("lesson_3_1_1" to testLesson),
            quizList = listOf(q1, q2)
        )

        val fakeProgressRepo = FakeProgressRepository(
            lessonProgressMap = mapOf("lesson_3_1_1" to lessonProgress)
        )

        val viewModel = QuizViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.currentIndex)
        assertEquals(1, state.currentScore)
        assertEquals("Question 2?", state.quizList[state.currentIndex].question)
    }

    @Test
    fun testEmptyQuizHandledGracefully() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val viewModel = QuizViewModel(
            lessonId = "empty_lesson",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(),
            learningRepository = FakeLearningRepository()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.quizList.isEmpty())
        assertFalse(state.isFinished)
    }

    open class FakeAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    class FakeUserRepository(private val profile: UserProfile? = null) : UserRepository {
        override fun getUserProfile(uid: String): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
        override suspend fun createUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateDisplayName(uid: String, displayName: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getCurrentUserProfile(): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
    }

    class FakeProgressRepository(
        private val lessonProgressMap: Map<String, LessonProgress> = emptyMap()
    ) : ProgressRepository {
        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(null))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> =
            flowOf(Result.success(lessonProgressMap[lessonId]))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(false)
    }

    class FakeLearningRepository(
        private val lessonMap: Map<String, Lesson> = emptyMap(),
        private val quizList: List<Quiz> = emptyList()
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lessonMap[lessonId]))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(quizList))
    }
}
