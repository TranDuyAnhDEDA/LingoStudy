package com.example.mystudyapp

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.viewmodel.ProfileViewModel
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
class ProfileViewModelTest {

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
    fun testProfileViewModelLoadsProfileAndProgress() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepo(testUser)

        val testProfile = UserProfile(
            uid = "test_uid",
            displayName = "Test Learner",
            email = "test@example.com",
            levelName = "Explorer",
            xp = 350,
            streak = 5
        )

        val testProgress = UserProgress(
            userId = "test_uid",
            vocabularyProgress = 80,
            grammarProgress = 60,
            completedLessons = 2
        )

        val viewModel = ProfileViewModel(
            authRepository = fakeAuthRepo,
            userRepository = FakeUserRepo(profile = testProfile),
            progressRepository = FakeProgressRepo(progress = testProgress),
            learningRepository = FakeLearningRepo()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertNotNull(state.profile)
        assertEquals("Test Learner", state.profile?.displayName)
        assertEquals(350, state.profile?.xp)
        assertNotNull(state.progress)
        assertEquals(80, state.progress?.vocabularyProgress)
    }

    @Test
    fun testUpdateDisplayName() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val fakeUserRepo = FakeUserRepo()

        val viewModel = ProfileViewModel(
            authRepository = fakeAuthRepo,
            userRepository = fakeUserRepo,
            progressRepository = FakeProgressRepo(),
            learningRepository = FakeLearningRepo()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDisplayName("Updated Name")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Updated Name", fakeUserRepo.lastUpdatedName)
    }

    class FakeAuthRepo(private val user: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String) = Result.failure<AuthUser>(Exception())
        override suspend fun login(email: String, password: String) = Result.failure<AuthUser>(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = user
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(user)
    }

    class FakeUserRepo(private val profile: UserProfile? = null) : UserRepository {
        var lastUpdatedName: String? = null

        override fun getUserProfile(uid: String) = flowOf(Result.success(profile))
        override suspend fun createUserProfile(user: UserProfile) = Result.success(Unit)
        override suspend fun updateUserProfile(user: UserProfile) = Result.success(Unit)
        override suspend fun updateDisplayName(uid: String, displayName: String): Result<Unit> {
            lastUpdatedName = displayName
            return Result.success(Unit)
        }
        override fun getCurrentUserProfile() = flowOf(Result.success(profile))
    }

    class FakeLearningRepo : LearningRepository {
        override fun getPrograms(): Flow<Result<List<com.example.mystudyapp.data.model.LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<com.example.mystudyapp.data.model.LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<com.example.mystudyapp.data.model.Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<com.example.mystudyapp.data.model.Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<com.example.mystudyapp.data.model.Level?>> = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<com.example.mystudyapp.data.model.Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<com.example.mystudyapp.data.model.Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<com.example.mystudyapp.data.model.Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<com.example.mystudyapp.data.model.Lesson?>> = flowOf(Result.success(null))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<com.example.mystudyapp.data.model.Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<com.example.mystudyapp.data.model.Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<com.example.mystudyapp.data.model.Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<com.example.mystudyapp.data.model.Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<com.example.mystudyapp.data.model.Quiz>>> = flowOf(Result.success(emptyList()))
    }

    class FakeProgressRepo(private val progress: UserProgress? = null) : ProgressRepository {
        override fun getUserProgress(uid: String) = flowOf(Result.success(progress))
        override suspend fun createUserProgress(progress: UserProgress) = Result.success(Unit)
        override suspend fun updateUserProgress(progress: UserProgress) = Result.success(Unit)
        override fun getLessonProgress(uid: String, lessonId: String) = flowOf(Result.success(null as LessonProgress?))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress) = Result.success(Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String) = Result.success(Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String) = Result.success(false)
    }
}
