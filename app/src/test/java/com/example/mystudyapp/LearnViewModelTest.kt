package com.example.mystudyapp

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.Grammar
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
            userRepository = FakeUserRepository(),
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
    fun testAuthenticatedUserLoadsUnitsAndLessonsSortedWithProgress() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProfile = UserProfile(
            uid = "test_uid_123",
            displayName = "Test Learner",
            email = "test@example.com",
            levelId = "level_3",
            levelName = "Explorer",
            xp = 320,
            streak = 5
        )

        val testProgress = UserProgress(
            userId = "test_uid_123",
            currentLessonId = "lesson_3_1_1",
            vocabularyProgress = 72,
            grammarProgress = 64,
            listeningProgress = 58,
            pronunciationProgress = 45
        )

        val testLevel = Level(id = "level_3", name = "Explorer", xpRequired = 600)

        val unit1 = Unit(id = "unit_3_1", levelId = "level_3", title = "Travel and Tourism", order = 1)
        val unit2 = Unit(id = "unit_3_2", levelId = "level_3", title = "Food and Dining", order = 2)

        val lesson1 = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", order = 1)
        val lesson2 = Lesson(id = "lesson_3_1_2", unitId = "unit_3_1", title = "At the Hotel", order = 2)

        val lessonProgress1 = LessonProgress(lessonId = "lesson_3_1_1", progress = 100, completed = true)

        val fakeLearningRepo = FakeLearningRepository(
            level = testLevel,
            units = listOf(unit2, unit1),
            lessons = listOf(lesson2, lesson1)
        )

        val fakeProgressRepo = FakeProgressRepository(
            progress = testProgress,
            lessonProgressMap = mapOf("lesson_3_1_1" to lessonProgress1)
        )

        val viewModel = LearnViewModel(
            authRepository = fakeAuthRepo,
            userRepository = FakeUserRepository(profile = testProfile),
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals("Explorer", state.levelName)
        assertEquals(320, state.currentXp)
        assertEquals(600, state.requiredXp)
        assertEquals(5, state.streak)
        assertEquals(72, state.vocabularyProgress)

        assertEquals(2, state.units.size)
        assertEquals("unit_3_1", state.units[0].unitId)
        assertEquals("unit_3_2", state.units[1].unitId)

        val unit1Lessons = state.units[0].lessons
        assertEquals(2, unit1Lessons.size)
        assertEquals("lesson_3_1_1", unit1Lessons[0].lessonId)
        assertEquals("lesson_3_1_2", unit1Lessons[1].lessonId)

        assertTrue(unit1Lessons[0].isCurrentLesson)
        assertFalse(unit1Lessons[1].isCurrentLesson)

        assertEquals(100, unit1Lessons[0].progress)
        assertTrue(unit1Lessons[0].completed)

        assertEquals(0, unit1Lessons[1].progress)
        assertFalse(unit1Lessons[1].completed)
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
        private val progress: UserProgress? = null,
        private val lessonProgressMap: Map<String, LessonProgress> = emptyMap()
    ) : ProgressRepository {
        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(progress))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> =
            flowOf(Result.success(lessonProgressMap[lessonId]))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(false)
    }

    class FakeLearningRepository(
        private val level: Level? = null,
        private val units: List<Unit> = emptyList(),
        private val lessons: List<Lesson> = emptyList()
    ) : LearningRepository {
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(level))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(units))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(lessons))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(null))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
