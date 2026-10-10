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
import com.example.mystudyapp.viewmodel.LearnViewModel
import com.example.mystudyapp.viewmodel.LevelUnitsViewModel
import com.example.mystudyapp.viewmodel.ProgramOverviewViewModel
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
class HierarchyNavigationTest {

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
    fun testProgramSelectionAndRoutingInLearnViewModel() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)
        val fakeLearningRepo = FakeLearningRepository()

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
    }

    @Test
    fun testLevelFilteringByProgramInProgramOverviewViewModel() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProgram = LearningProgram(
            id = "program_beginner",
            title = "Beginner English",
            description = "Basic English",
            targetAudience = "Beginners",
            iconEmoji = "🌱",
            levelIds = listOf("level_3"),
            isAvailable = true
        )
        val testLevel = Level(id = "level_3", programId = "program_beginner", name = "Explorer", description = "Basic English", order = 3, xpRequired = 600)

        val fakeLearningRepo = FakeLearningRepository(
            programs = listOf(testProgram),
            levelsMap = mapOf("program_beginner" to listOf(testLevel))
        )

        val viewModel = ProgramOverviewViewModel(
            programId = "program_beginner",
            authRepository = fakeAuthRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.program)
        assertEquals("Beginner English", state.program?.title)
        assertEquals(1, state.levels.size)
        assertEquals("Explorer", state.levels[0].name)
        assertEquals("program_beginner", state.levels[0].programId)
    }

    @Test
    fun testUnrelatedLevelsFilteredOutForOtherPrograms() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testProgramKids = LearningProgram(
            id = "program_kids",
            title = "English for Kids",
            description = "Description",
            targetAudience = "Kids",
            iconEmoji = "🧸",
            levelIds = emptyList(),
            isAvailable = false
        )
        val testLevelBeginner = Level(id = "level_3", programId = "program_beginner", name = "Explorer")

        val fakeLearningRepo = FakeLearningRepository(
            programs = listOf(testProgramKids),
            levelsMap = mapOf("program_beginner" to listOf(testLevelBeginner))
        )

        val viewModel = ProgramOverviewViewModel(
            programId = "program_kids",
            authRepository = fakeAuthRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.program)
        assertEquals("English for Kids", state.program?.title)
        assertTrue("English for Kids must NOT show Beginner levels", state.levels.isEmpty())
    }

    @Test
    fun testUnitFilteringByLevelInLevelUnitsViewModel() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testLevel = Level(id = "level_3", name = "Explorer")
        val testUnit = Unit(id = "unit_3_1", levelId = "level_3", title = "Travel and Tourism", order = 1)

        val fakeLearningRepo = FakeLearningRepository(
            level = testLevel,
            unitsMap = mapOf("level_3" to listOf(testUnit))
        )

        val viewModel = LevelUnitsViewModel(
            levelId = "level_3",
            authRepository = fakeAuthRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.level)
        assertEquals("Explorer", state.level?.name)
        assertEquals(1, state.units.size)
        assertEquals("Travel and Tourism", state.units[0].title)
    }

    @Test
    fun testLessonFilteringByUnitInUnitLessonsViewModel() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testUnit = Unit(id = "unit_3_1", levelId = "level_3", title = "Travel and Tourism")
        val testLesson = Lesson(id = "lesson_3_1_1", unitId = "unit_3_1", title = "At the Airport", order = 1, estimatedMinutes = 8, xpReward = 30)

        val fakeLearningRepo = FakeLearningRepository(
            unit = testUnit,
            lessonsMap = mapOf("unit_3_1" to listOf(testLesson))
        )

        val viewModel = UnitLessonsViewModel(
            unitId = "unit_3_1",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(),
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.unit)
        assertEquals("Travel and Tourism", state.unit?.title)
        assertEquals(1, state.lessons.size)
        assertEquals("lesson_3_1_1", state.lessons[0].lesson.id)
        assertEquals("At the Airport", state.lessons[0].lesson.title)
    }

    open class FakeProgressRepository : ProgressRepository {
        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(null))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> = flowOf(Result.success(null))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(false)
    }

    open class FakeAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    open class FakeLearningRepository(
        private val programs: List<LearningProgram> = listOf(
            LearningProgram(id = "program_kids", title = "English for Kids", description = "Description", targetAudience = "Audience", iconEmoji = "🧸", levelIds = emptyList(), isAvailable = false),
            LearningProgram(id = "program_beginner", title = "Beginner English", description = "Description", targetAudience = "Audience", iconEmoji = "🌱", levelIds = listOf("level_3"), isAvailable = true),
            LearningProgram(id = "program_adults", title = "English for Adults", description = "Description", targetAudience = "Audience", iconEmoji = "💼", levelIds = emptyList(), isAvailable = false)
        ),
        private val levelsMap: Map<String, List<Level>> = emptyMap(),
        private val unitsMap: Map<String, List<Unit>> = emptyMap(),
        private val lessonsMap: Map<String, List<Lesson>> = emptyMap(),
        private val level: Level? = null,
        private val unit: Unit? = null
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(programs))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(programs.firstOrNull { it.id == programId }))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(levelsMap[programId] ?: emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(level))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(unitsMap[levelId] ?: emptyList()))
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
