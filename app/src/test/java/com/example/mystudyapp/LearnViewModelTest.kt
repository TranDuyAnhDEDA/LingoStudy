package com.example.mystudyapp

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.viewmodel.LearnViewModel
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
class LearnViewModelTest {

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
        val viewModel = LearnViewModel(
            authRepository = fakeAuthRepo,
            learningRepository = FakeLearningRepository()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun testAuthenticatedUserLoadsLearningPrograms() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val programsList = listOf(
            LearningProgram(id = "program_kids", title = "English for Kids", description = "Description", targetAudience = "Kids", iconEmoji = "🧸", levelIds = emptyList(), isAvailable = false),
            LearningProgram(id = "program_beginner", title = "Beginner English", description = "Description", targetAudience = "Beginners", iconEmoji = "🌱", levelIds = listOf("level_3"), isAvailable = true),
            LearningProgram(id = "program_adults", title = "English for Adults", description = "Description", targetAudience = "Adults", iconEmoji = "💼", levelIds = emptyList(), isAvailable = false)
        )

        val fakeLearningRepo = FakeLearningRepository(programs = programsList)

        val viewModel = LearnViewModel(
            authRepository = fakeAuthRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals(3, state.programs.size)
        assertEquals("English for Kids", state.programs[0].title)
        assertEquals("Beginner English", state.programs[1].title)
        assertEquals("English for Adults", state.programs[2].title)
        assertTrue(state.programs[1].isAvailable)
        assertFalse(state.programs[0].isAvailable)
    }

    open class FakeAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    class FakeLearningRepository(
        private val programs: List<LearningProgram> = emptyList()
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(programs))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(programs.firstOrNull { it.id == programId }))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(null))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
