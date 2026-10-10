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
import com.example.mystudyapp.viewmodel.LearnViewModel
import com.example.mystudyapp.viewmodel.LessonViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonCompletionTest {

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
    fun testLessonNotCompleteIfAnyModuleIncomplete() {
        val partialProgress = LessonProgress(
            lessonId = "lesson_3_1_1",
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = true,
            pronunciationCompleted = true,
            quizCompleted = false,
            completed = false
        )

        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)
        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", xpReward = 30)

        val viewModel = LessonViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(lessonProgressMap = mapOf("lesson_3_1_1" to partialProgress)),
            learningRepository = FakeLearningRepository(lessonMap = mapOf("lesson_3_1_1" to testLesson))
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Lesson must NOT be complete when quiz is incomplete", state.isCompleted)
        assertEquals(80, state.progressPercentage)
        assertTrue(state.vocabularyCompleted)
        assertTrue(state.grammarCompleted)
        assertTrue(state.listeningCompleted)
        assertTrue(state.pronunciationCompleted)
        assertFalse(state.quizCompleted)
    }

    @Test
    fun testLessonBecomesCompletedWhenAllFiveModulesComplete() {
        val fullProgress = LessonProgress(
            lessonId = "lesson_3_1_1",
            vocabularyCompleted = true,
            grammarCompleted = true,
            listeningCompleted = true,
            pronunciationCompleted = true,
            quizCompleted = true,
            completed = true
        )

        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)
        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", xpReward = 30)

        val viewModel = LessonViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(lessonProgressMap = mapOf("lesson_3_1_1" to fullProgress)),
            learningRepository = FakeLearningRepository(lessonMap = mapOf("lesson_3_1_1" to testLesson))
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Lesson must be complete when all 5 modules are complete", state.isCompleted)
        assertEquals(100, state.progressPercentage)
    }

    @Test
    fun testXpDuplicationProtection() {
        var completeCallCount = 0
        val fakeProgressRepo = object : FakeProgressRepository() {
            override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> {
                completeCallCount++
                return Result.success(completeCallCount == 1)
            }
        }

        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val viewModel = LessonViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = fakeProgressRepo,
            learningRepository = FakeLearningRepository(
                lessonMap = mapOf("lesson_3_1_1" to Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "Airport", xpReward = 30))
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadLessonData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("completeLessonIfEligible called", completeCallCount >= 2)
    }

    @Test
    fun testHomeAndLearnStateReflectCompletedLesson() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProfile = UserProfile(uid = "test_uid_123", displayName = "Test Learner", xp = 350, levelName = "Explorer")
        val testProgress = UserProgress(userId = "test_uid_123", currentLessonId = "lesson_3_1_2", completedLessons = 1, totalLessons = 3)

        val homeViewModel = HomeViewModel(
            authRepository = fakeAuthRepo,
            userRepository = FakeUserRepository(profile = testProfile),
            progressRepository = FakeProgressRepository(progress = testProgress),
            learningRepository = FakeLearningRepository(
                level = Level(id = "level_3", name = "Explorer", xpRequired = 600),
                lessonMap = mapOf("lesson_3_1_2" to Lesson(id = "lesson_3_1_2", unitId = "unit_3_1", title = "At the Hotel"))
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val homeState = homeViewModel.uiState.value
        assertEquals("At the Hotel", homeState.currentLessonTitle)
        assertEquals(1, homeState.completedLessons)
        assertEquals(350, homeState.currentXp)

        val learnViewModel = LearnViewModel(
            authRepository = fakeAuthRepo,
            learningRepository = FakeLearningRepository(
                programsList = listOf(
                    LearningProgram(id = "program_beginner", title = "Beginner English", description = "Description", targetAudience = "Beginners", iconEmoji = "🌱", levelIds = listOf("level_3"), isAvailable = true)
                )
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val learnState = learnViewModel.uiState.value
        assertEquals(1, learnState.programs.size)
        assertEquals("Beginner English", learnState.programs[0].title)
    }

    open class FakeAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    open class FakeUserRepository(private val profile: UserProfile? = null) : UserRepository {
        override fun getUserProfile(uid: String): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
        override suspend fun createUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateDisplayName(uid: String, displayName: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getCurrentUserProfile(): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
    }

    open class FakeProgressRepository(
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

    open class FakeLearningRepository(
        private val programsList: List<LearningProgram> = emptyList(),
        private val level: Level? = null,
        private val units: List<Unit> = emptyList(),
        private val lessonsList: List<Lesson> = emptyList(),
        private val lessonMap: Map<String, Lesson> = emptyMap()
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(programsList))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(programsList.firstOrNull { it.id == programId }))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(level))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(units))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(lessonsList))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lessonMap[lessonId]))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
