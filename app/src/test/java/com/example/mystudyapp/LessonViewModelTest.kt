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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

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
        val viewModel = LessonViewModel(
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
    fun testLessonDataAndContentCountsLoadedSuccessfully() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testLesson = Lesson(
            id = "lesson_3_1_1",
            unitId = "unit_3_1",
            title = "At the Airport",
            description = "Learn useful English expressions used at the airport.",
            order = 1,
            estimatedMinutes = 8,
            xpReward = 30
        )

        val vocabList = listOf(
            Vocabulary("v1", "lesson_3_1_1", "boarding pass", "thẻ lên máy bay", null, "noun", "", null, "", null, null, 1),
            Vocabulary("v2", "lesson_3_1_1", "passport", "hộ chiếu", null, "noun", "", null, "", null, null, 2),
            Vocabulary("v3", "lesson_3_1_1", "luggage", "hành lý", null, "noun", "", null, "", null, null, 3),
            Vocabulary("v4", "lesson_3_1_1", "departure", "sự khởi hành", null, "noun", "", null, "", null, null, 4),
            Vocabulary("v5", "lesson_3_1_1", "gate", "cổng", null, "noun", "", null, "", null, null, 5)
        )

        val grammarList = listOf(
            Grammar("g1", "lesson_3_1_1", "Present Perfect", null, "Explanation", null, listOf("Example 1"))
        )

        val listeningList = listOf(
            Listening("l1", "lesson_3_1_1", "At the Airport", null, "", "Transcript", null, "medium", 1)
        )

        val pronunList = listOf(
            Pronunciation("p1", "lesson_3_1_1", "airport", "/ˈeəpɔːt/", null, 80, null, 1),
            Pronunciation("p2", "lesson_3_1_1", "boarding", "/ˈbɔːdɪŋ/", null, 80, null, 2),
            Pronunciation("p3", "lesson_3_1_1", "passport", "/ˈpɑːspɔːt/", null, 80, null, 3)
        )

        val quizList = listOf(
            Quiz("q1", "lesson_3_1_1", "multiple_choice", "Question 1", null, listOf("Opt 1"), "Opt 1", null, 10),
            Quiz("q2", "lesson_3_1_1", "multiple_choice", "Question 2", null, listOf("Opt 1"), "Opt 1", null, 10),
            Quiz("q3", "lesson_3_1_1", "multiple_choice", "Question 3", null, listOf("Opt 1"), "Opt 1", null, 10),
            Quiz("q4", "lesson_3_1_1", "multiple_choice", "Question 4", null, listOf("Opt 1"), "Opt 1", null, 10),
            Quiz("q5", "lesson_3_1_1", "multiple_choice", "Question 5", null, listOf("Opt 1"), "Opt 1", null, 10)
        )

        val lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", progress = 100, completed = true)

        val fakeLearningRepo = FakeLearningRepository(
            lessonMap = mapOf("lesson_3_1_1" to testLesson),
            vocabList = vocabList,
            grammarList = grammarList,
            listeningList = listeningList,
            pronunList = pronunList,
            quizList = quizList
        )

        val fakeProgressRepo = FakeProgressRepository(
            lessonProgressMap = mapOf("lesson_3_1_1" to lessonProgress)
        )

        val viewModel = LessonViewModel(
            lessonId = "lesson_3_1_1",
            authRepository = fakeAuthRepo,
            progressRepository = fakeProgressRepo,
            learningRepository = fakeLearningRepo
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals("lesson_3_1_1", state.lessonId)
        assertEquals("At the Airport", state.title)
        assertEquals("Learn useful English expressions used at the airport.", state.description)
        assertEquals(8, state.estimatedMinutes)
        assertEquals(30, state.xpReward)
        assertEquals(100, state.progressPercentage)
        assertTrue(state.isCompleted)
        assertEquals(5, state.vocabularyCount)
        assertEquals(1, state.grammarCount)
        assertEquals(1, state.listeningCount)
        assertEquals(3, state.pronunciationCount)
        assertEquals(5, state.quizCount)
    }

    @Test
    fun testMissingLessonProgressDefaultsToZero() {
        val testUser = AuthUser(uid = "test_uid_123", email = "test@example.com", displayName = "Test Learner")
        val fakeAuthRepo = FakeAuthRepository(currentUser = testUser)

        val testLesson = Lesson(id = "lesson_3_1_2", unitId = "unit_3_1", title = "At the Hotel")

        val viewModel = LessonViewModel(
            lessonId = "lesson_3_1_2",
            authRepository = fakeAuthRepo,
            progressRepository = FakeProgressRepository(lessonProgressMap = emptyMap()),
            learningRepository = FakeLearningRepository(lessonMap = mapOf("lesson_3_1_2" to testLesson))
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(0, state.progressPercentage)
        assertFalse(state.isCompleted)
    }

    open class FakeAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
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
        private val vocabList: List<Vocabulary> = emptyList(),
        private val grammarList: List<Grammar> = emptyList(),
        private val listeningList: List<Listening> = emptyList(),
        private val pronunList: List<Pronunciation> = emptyList(),
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
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(vocabList))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(grammarList))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(listeningList))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(pronunList))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(quizList))
    }
}
