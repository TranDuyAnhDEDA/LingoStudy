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
import com.example.mystudyapp.viewmodel.HomeViewModel
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
class HomeViewModelTest {

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
        val viewModel = HomeViewModel(
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
    fun testAuthenticatedUserDataFlowsToHomeUiState() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProfile = UserProfile(
            uid = "test_uid_123",
            displayName = "Test Learner",
            email = "test@example.com",
            levelId = "level_3",
            levelName = "Explorer",
            xp = 350,
            streak = 7,
            longestStreak = 10,
            totalLessonsCompleted = 5,
            avatarId = "girl_glasses",
            customAvatarPath = "/data/user/0/com.example.mystudyapp/files/avatars/avatar_test.jpg"
        )

        val testProgress = UserProgress(
            userId = "test_uid_123",
            vocabularyProgress = 80,
            grammarProgress = 70,
            listeningProgress = 60,
            pronunciationProgress = 50,
            currentLessonId = "lesson_3_1_1",
            completedLessons = 1,
            totalLessons = 3
        )

        val testLevel = Level(id = "level_3", name = "Explorer", xpRequired = 600)
        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", estimatedMinutes = 8, xpReward = 30)

        val viewModel = HomeViewModel(
            authRepository = fakeAuthRepo,
            userRepository = FakeUserRepository(profile = testProfile),
            progressRepository = FakeProgressRepository(progress = testProgress),
            learningRepository = FakeLearningRepository(level = testLevel, lesson = testLesson)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals("Test Learner", state.displayName)
        assertEquals(7, state.streak)
        assertEquals("Cấp 2", state.levelName)
        assertEquals(350, state.currentXp)
        assertEquals(150, state.currentLevelXp)
        assertEquals(250, state.requiredXp)
        assertEquals("lesson_3_1_1", state.currentLessonId)
        assertEquals("At the Airport", state.currentLessonTitle)
        assertEquals(8, state.currentLessonEstimatedMinutes)
        assertEquals(80, state.vocabularyProgress)
        assertEquals(70, state.grammarProgress)
        assertEquals(60, state.listeningProgress)
        assertEquals(50, state.pronunciationProgress)
        assertEquals(1, state.completedLessons)
        assertEquals(3, state.totalLessons)
        assertEquals("girl_glasses", state.avatarId)
        assertEquals("/data/user/0/com.example.mystudyapp/files/avatars/avatar_test.jpg", state.customAvatarPath)
        assertEquals("unit_3_1", state.currentUnitId)
        assertEquals("unit/unit_3_1", state.resumeRoute)
    }

    @Test
    fun testCompletedLessonAdvancesToNextUnfinishedLessonInUnit() {
        val testUser = AuthUser(uid = "test_uid_456", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProfile = UserProfile(
            uid = "test_uid_456",
            displayName = "Test Learner",
            xp = 100
        )

        // Stale progress document pointing to lesson_b1_1_1 even though it's already completed
        val testProgress = UserProgress(
            userId = "test_uid_456",
            currentLessonId = "lesson_b1_1_1",
            completedLessons = 1,
            totalLessons = 3
        )

        val lesson1 = Lesson(id = "lesson_b1_1_1", unitId = "unit_b1_1", title = "Saying Hello", order = 1)
        val lesson2 = Lesson(id = "lesson_b1_1_2", unitId = "unit_b1_1", title = "Introducing Yourself", order = 2)
        val unit1 = Unit(id = "unit_b1_1", title = "Unit 1: Greetings", order = 1)

        val progressRepo = object : FakeProgressRepository(progress = testProgress) {
            override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> {
                return if (lessonId == "lesson_b1_1_1") {
                    flowOf(Result.success(LessonProgress(lessonId = lessonId, completed = true, progress = 100, xpEarned = 30)))
                } else {
                    flowOf(Result.success(null))
                }
            }
        }

        val learningRepo = object : FakeLearningRepository(lesson = lesson1) {
            override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(unit1))
            override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(listOf(lesson1, lesson2)))
        }

        val viewModel = HomeViewModel(
            authRepository = fakeAuthRepo,
            userRepository = FakeUserRepository(profile = testProfile),
            progressRepository = progressRepo,
            learningRepository = learningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("unit_b1_1", state.currentUnitId)
        assertEquals("unit/unit_b1_1", state.resumeRoute)
        assertEquals("lesson_b1_1_2", state.currentLessonId)
        assertEquals("Introducing Yourself", state.currentLessonTitle)
        assertEquals(0, state.unfinishedLessonProgressPct)
        assertEquals(1, state.unitCompletedLessons)
        assertEquals(2, state.unitTotalLessons)
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

    open class FakeProgressRepository(private val progress: UserProgress? = null) : ProgressRepository {
        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(progress))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> = flowOf(Result.success(null))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(false)
    }

    open class FakeLearningRepository(
        private val level: Level? = null,
        private val lesson: Lesson? = null
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(if (level != null) listOf(level) else emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(level))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lesson))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
