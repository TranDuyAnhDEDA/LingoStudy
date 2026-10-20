package com.example.mystudyapp

import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.DailyStudyTime
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
import com.example.mystudyapp.ui.screen.profile.AVAILABLE_AVATARS
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileFeatureTest {

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
    fun testProfileViewModelLoadsFullUserStatisticsAndChart() {
        val testUser = AuthUser(uid = "user_123", email = "alex@example.com", displayName = "Alex Learner")
        val fakeAuth = FakeAuthRepo(testUser)

        val testProfile = UserProfile(
            uid = "user_123",
            displayName = "Alex Learner",
            email = "alex@example.com",
            levelId = "level_3",
            levelName = "Beginner Explorer",
            xp = 450,
            streak = 7
        )

        val testProgress = UserProgress(
            userId = "user_123",
            vocabularyProgress = 80,
            grammarProgress = 60,
            listeningProgress = 40,
            pronunciationProgress = 20,
            currentLessonId = "lesson_3_1_2",
            currentActivity = "grammar",
            completedLessons = 3
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)

        val dailyList = listOf(
            DailyStudyTime(date = todayStr, totalSeconds = 1200L), // 20 mins
            DailyStudyTime(date = "2026-10-10", totalSeconds = 600L) // 10 mins
        )

        val fakeProgressRepo = FakeProgressRepo(
            progress = testProgress,
            dailyList = dailyList,
            totalSeconds = 1800L
        )

        val fakeLearningRepo = FakeLearningRepo(
            level = Level(id = "level_3", name = "Beginner Explorer", xpRequired = 900),
            lesson = Lesson(id = "lesson_3_1_2", unitId = "unit_3_1", title = "At the Hotel"),
            unit = Unit(id = "unit_3_1", levelId = "level_3", title = "Travel and Tourism")
        )

        val viewModel = ProfileViewModel(
            authRepository = fakeAuth,
            userRepository = FakeUserRepo(profile = testProfile),
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertNotNull(state.profile)
        assertEquals("Alex Learner", state.profile?.displayName)
        assertEquals(450, state.currentXp)
        assertEquals(300, state.requiredXp)
        assertEquals("Cấp 3", state.currentLevelName)

        // Skill Mastery
        assertEquals(80, state.vocabMasteryPct)
        assertEquals(60, state.grammarMasteryPct)
        assertEquals(40, state.listeningMasteryPct)
        assertEquals(20, state.pronunMasteryPct)

        // Study Time & Chart
        assertEquals(20, state.todayStudyMinutes)
        assertEquals(30, state.totalStudyMinutes)
        assertEquals(7, state.last7DaysChart.size)

        // Resume Learning
        assertEquals("lesson_3_1_2", state.unfinishedLessonId)
        assertEquals("At the Hotel", state.unfinishedLessonTitle)
        assertEquals("Travel and Tourism", state.unfinishedUnitTitle)
        assertEquals("grammar/lesson_3_1_2", state.resumeRoute)
    }

    @Test
    fun testUpdateDisplayNameInProfile() {
        val testUser = AuthUser(uid = "user_123", email = "alex@example.com", displayName = "Alex Learner")
        val fakeAuth = FakeAuthRepo(testUser)
        val fakeUserRepo = FakeUserRepo()

        val viewModel = ProfileViewModel(
            authRepository = fakeAuth,
            userRepository = fakeUserRepo,
            progressRepository = FakeProgressRepo(),
            learningRepository = FakeLearningRepo()
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDisplayName("Alex Pro")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Alex Pro", fakeUserRepo.lastUpdatedName)
    }

    @Test
    fun testUpdateAvatarInProfile() {
        val testUser = AuthUser(uid = "user_123", email = "alex@example.com", displayName = "Alex Learner")
        val fakeAuth = FakeAuthRepo(testUser)
        val fakeUserRepo = FakeUserRepo()

        val viewModel = ProfileViewModel(
            authRepository = fakeAuth,
            userRepository = fakeUserRepo,
            progressRepository = FakeProgressRepo(),
            learningRepository = FakeLearningRepo()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateAvatar("girl_glasses", "/data/user/0/com.example.mystudyapp/files/avatars/avatar_custom.jpg")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("girl_glasses", viewModel.uiState.value.profile?.avatarId)
        assertEquals("/data/user/0/com.example.mystudyapp/files/avatars/avatar_custom.jpg", viewModel.uiState.value.profile?.customAvatarPath)
        assertEquals("girl_glasses", fakeUserRepo.lastUpdatedAvatar)
        assertEquals("/data/user/0/com.example.mystudyapp/files/avatars/avatar_custom.jpg", fakeUserRepo.lastUpdatedCustomAvatar)
    }

    @Test
    fun testUpdateProfileDetails() {
        val testUser = AuthUser(uid = "user_123", email = "alex@example.com", displayName = "Alex Learner")
        val fakeAuth = FakeAuthRepo(testUser)
        val fakeUserRepo = FakeUserRepo()

        val viewModel = ProfileViewModel(
            authRepository = fakeAuth,
            userRepository = fakeUserRepo,
            progressRepository = FakeProgressRepo(),
            learningRepository = FakeLearningRepo()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateProfileDetails(
            newName = "Minh Anh Mới",
            newPhone = "0987654321",
            newGoalMinutes = 45,
            newLevelName = "Beginner 2: Mở rộng vốn từ"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Minh Anh Mới", state.profile?.displayName)
        assertEquals("0987654321", state.profile?.phoneNumber)
        assertEquals(45, state.profile?.dailyGoalMinutes)
        assertEquals("Beginner 2: Mở rộng vốn từ", state.currentLevelName)
        assertEquals("Minh Anh Mới", fakeUserRepo.lastUpdatedName)
    }

    @Test
    fun testLearningHistoryInProfile() {
        val testUser = AuthUser(uid = "user_123", email = "alex@example.com", displayName = "Alex Learner")
        val fakeAuth = FakeAuthRepo(testUser)
        val mockHistory = listOf(
            LessonProgress(
                lessonId = "lesson_b1_1_1",
                completed = true,
                progress = 100,
                vocabularyCompleted = true,
                grammarCompleted = true
            )
        )

        val viewModel = ProfileViewModel(
            authRepository = fakeAuth,
            userRepository = FakeUserRepo(),
            progressRepository = FakeProgressRepo(historyList = mockHistory),
            learningRepository = FakeLearningRepo()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val history = viewModel.uiState.value.learningHistory
        assertNotNull(history)
        assertTrue(history.isNotEmpty())
        assertTrue(history.any { it.isCompleted })
    }

    @Test
    fun testAvatarOptionsAvailable() {
        assertTrue(AVAILABLE_AVATARS.isNotEmpty())
        assertTrue(AVAILABLE_AVATARS.any { it.id == "boy_hoodie" })
        assertTrue(AVAILABLE_AVATARS.any { it.id == "girl_glasses" })
    }

    @Test
    fun testFormatStudyDurationUnderAndOver60Minutes() {
        assertEquals("0m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(0))
        assertEquals("15m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(15))
        assertEquals("45m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(45))
        assertEquals("1h", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(60))
        assertEquals("1h 15m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(75))
        assertEquals("1h 53m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(113))
        assertEquals("2h", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(120))
        assertEquals("2h 15m", com.example.mystudyapp.ui.screen.profile.formatStudyDuration(135))

        assertEquals("0", com.example.mystudyapp.ui.screen.profile.formatStudyDurationShort(0))
        assertEquals("15m", com.example.mystudyapp.ui.screen.profile.formatStudyDurationShort(15))
        assertEquals("1h", com.example.mystudyapp.ui.screen.profile.formatStudyDurationShort(60))
        assertEquals("1h15m", com.example.mystudyapp.ui.screen.profile.formatStudyDurationShort(75))
        assertEquals("1h53m", com.example.mystudyapp.ui.screen.profile.formatStudyDurationShort(113))
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
        var lastUpdatedAvatar: String? = null
        var lastUpdatedCustomAvatar: String? = null

        override fun getUserProfile(uid: String) = flowOf(Result.success(profile))
        override suspend fun createUserProfile(user: UserProfile) = Result.success(kotlin.Unit)
        override suspend fun updateUserProfile(user: UserProfile) = Result.success(kotlin.Unit)
        override suspend fun updateDisplayName(uid: String, displayName: String): Result<kotlin.Unit> {
            lastUpdatedName = displayName
            return Result.success(kotlin.Unit)
        }
        override suspend fun updateAvatar(uid: String, avatarId: String, customAvatarPath: String?): Result<kotlin.Unit> {
            lastUpdatedAvatar = avatarId
            lastUpdatedCustomAvatar = customAvatarPath
            return Result.success(kotlin.Unit)
        }
        override suspend fun updateProfileDetails(
            uid: String,
            displayName: String,
            phoneNumber: String,
            dailyGoalMinutes: Int,
            levelId: String,
            levelName: String
        ): Result<kotlin.Unit> {
            lastUpdatedName = displayName
            return Result.success(kotlin.Unit)
        }
        override fun getCurrentUserProfile() = flowOf(Result.success(profile))
    }

    class FakeProgressRepo(
        private val progress: UserProgress? = null,
        private val dailyList: List<DailyStudyTime> = emptyList(),
        private val totalSeconds: Long = 0L,
        private val historyList: List<LessonProgress> = emptyList()
    ) : ProgressRepository {
        override fun getUserProgress(uid: String) = flowOf(Result.success(progress))
        override suspend fun createUserProgress(progress: UserProgress) = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress) = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String) = flowOf(Result.success(null as LessonProgress?))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress) = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String) = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String) = Result.success(false)
        override suspend fun updateCurrentLearningPosition(uid: String, lessonId: String, activity: String?, index: Int) = Result.success(kotlin.Unit)
        override suspend fun recordStudyTime(uid: String, seconds: Long, dateString: String) = Result.success(kotlin.Unit)
        override fun getDailyStudyTime(uid: String, dateString: String) = flowOf(Result.success(null as DailyStudyTime?))
        override fun getLast7DaysStudyTime(uid: String): Flow<Result<List<DailyStudyTime>>> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val last7Dates = (6 downTo 0).map { i ->
                val cal = calendar.clone() as Calendar
                cal.add(Calendar.DAY_OF_YEAR, -i)
                sdf.format(cal.time)
            }

            val studyTimeMap = dailyList.associateBy { it.date }
            val full7Days = last7Dates.map { date ->
                studyTimeMap[date] ?: DailyStudyTime(date = date, totalSeconds = 0L)
            }
            return flowOf(Result.success(full7Days))
        }
        override fun getAllTimeStudyTime(uid: String) = flowOf(Result.success(totalSeconds))
        override fun getLearningHistory(uid: String) = flowOf(Result.success(historyList))
    }

    class FakeLearningRepo(
        private val level: Level? = null,
        private val lesson: Lesson? = null,
        private val unit: Unit? = null
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(level))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(unit))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lesson))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(emptyList()))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(emptyList()))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(emptyList()))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(emptyList()))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(emptyList()))
    }
}
