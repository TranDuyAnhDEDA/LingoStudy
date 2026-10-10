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
import com.example.mystudyapp.viewmodel.GrammarViewModel
import com.example.mystudyapp.viewmodel.LessonViewModel
import com.example.mystudyapp.viewmodel.ListeningViewModel
import com.example.mystudyapp.viewmodel.PronunciationViewModel
import com.example.mystudyapp.viewmodel.QuizViewModel
import com.example.mystudyapp.viewmodel.VocabularyViewModel
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
class ActivityCompletionFlowTest {

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
    fun testVocabularyCompletionShowsVocabularySpecificState() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val vocab = Vocabulary("v1", "lesson_3_1_1", "airport", "sân bay", "noun", "At the airport.", "Tại sân bay.", null, null, 1)

        val fakeLearningRepo = FakeLearningRepo(vocabularies = listOf(vocab))
        val fakeProgressRepo = FakeProgressRepo(lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", vocabularyCompleted = true))

        val viewModel = VocabularyViewModel("lesson_3_1_1", fakeAuthRepo, fakeProgressRepo, fakeLearningRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.completedCount)
        assertEquals(1, state.vocabularyList.size)
    }

    @Test
    fun testGrammarCompletionShowsGrammarSpecificState() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val grammar = Grammar("g1", "lesson_3_1_1", "Present Perfect", "Explanation", listOf("Example 1"), 1)

        val fakeLearningRepo = FakeLearningRepo(grammars = listOf(grammar))
        val fakeProgressRepo = FakeProgressRepo(lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", grammarCompleted = true))

        val viewModel = GrammarViewModel("lesson_3_1_1", fakeAuthRepo, fakeProgressRepo, fakeLearningRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.completedCount)
        assertEquals(1, state.grammarList.size)
    }

    @Test
    fun testListeningCompletionShowsListeningSpecificState() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val listening = Listening("l1", "lesson_3_1_1", "Airport Listening", null, "Transcript text", "beginner", 1)

        val fakeLearningRepo = FakeLearningRepo(listenings = listOf(listening))
        val fakeProgressRepo = FakeProgressRepo(lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", listeningCompleted = true))

        val viewModel = ListeningViewModel("lesson_3_1_1", fakeAuthRepo, fakeProgressRepo, fakeLearningRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.completedCount)
        assertEquals(1, state.listeningList.size)
    }

    @Test
    fun testPronunciationCompletionShowsPronunciationSpecificState() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val pronun = Pronunciation("p1", "lesson_3_1_1", "airport", "/ˈeəpɔːt/", null, 80, 1)

        val fakeLearningRepo = FakeLearningRepo(pronunciations = listOf(pronun))
        val fakeProgressRepo = FakeProgressRepo(lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", pronunciationCompleted = true))

        val viewModel = PronunciationViewModel("lesson_3_1_1", fakeAuthRepo, fakeProgressRepo, fakeLearningRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.completedCount)
        assertEquals(1, state.pronunciationList.size)
    }

    @Test
    fun testQuizCompletionShowsQuizResultsState() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val quiz = Quiz("q1", "lesson_3_1_1", "multiple_choice", "Question 1?", listOf("A", "B"), "A", 10, 1)

        val fakeLearningRepo = FakeLearningRepo(quizzes = listOf(quiz))
        val fakeProgressRepo = FakeProgressRepo(lessonProgress = LessonProgress(lessonId = "lesson_3_1_1", quizCompleted = true, quizScore = 1))

        val viewModel = QuizViewModel("lesson_3_1_1", fakeAuthRepo, fakeProgressRepo, fakeLearningRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.currentScore)
    }

    @Test
    fun testLessonProgressPercentageCalculation() {
        val testUser = AuthUser(uid = "test_uid", email = "test@example.com", displayName = "Test User")
        val fakeAuthRepo = FakeAuthRepo(testUser)
        val testLesson = Lesson("lesson_3_1_1", "unit_3_1", "program_beginner", "Airport", "Desc", 1, 8, 30)

        // 1 of 5 = 20%
        val progress1 = LessonProgress("lesson_3_1_1", vocabularyCompleted = true, progress = 20)
        val viewModel1 = LessonViewModel("lesson_3_1_1", fakeAuthRepo, FakeProgressRepo(lessonProgress = progress1), FakeLearningRepo(lesson = testLesson))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(20, viewModel1.uiState.value.progressPercentage)

        // 2 of 5 = 40%
        val progress2 = LessonProgress("lesson_3_1_1", vocabularyCompleted = true, grammarCompleted = true, progress = 40)
        val viewModel2 = LessonViewModel("lesson_3_1_1", fakeAuthRepo, FakeProgressRepo(lessonProgress = progress2), FakeLearningRepo(lesson = testLesson))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(40, viewModel2.uiState.value.progressPercentage)

        // 5 of 5 = 100%
        val progress5 = LessonProgress("lesson_3_1_1", vocabularyCompleted = true, grammarCompleted = true, listeningCompleted = true, pronunciationCompleted = true, quizCompleted = true, completed = true, progress = 100)
        val viewModel5 = LessonViewModel("lesson_3_1_1", fakeAuthRepo, FakeProgressRepo(lessonProgress = progress5), FakeLearningRepo(lesson = testLesson))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(100, viewModel5.uiState.value.progressPercentage)
        assertTrue(viewModel5.uiState.value.isCompleted)
    }

    class FakeAuthRepo(private val user: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String) = Result.failure<AuthUser>(Exception())
        override suspend fun login(email: String, password: String) = Result.failure<AuthUser>(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = user
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(user)
    }

    class FakeProgressRepo(private val lessonProgress: LessonProgress? = null) : ProgressRepository {
        override fun getUserProgress(uid: String) = flowOf(Result.success(null as UserProgress?))
        override suspend fun createUserProgress(progress: UserProgress) = Result.success(Unit)
        override suspend fun updateUserProgress(progress: UserProgress) = Result.success(Unit)
        override fun getLessonProgress(uid: String, lessonId: String) = flowOf(Result.success(lessonProgress))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress) = Result.success(Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String) = Result.success(Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String) = Result.success(false)
    }

    class FakeLearningRepo(
        private val lesson: Lesson? = null,
        private val vocabularies: List<Vocabulary> = emptyList(),
        private val grammars: List<Grammar> = emptyList(),
        private val listenings: List<Listening> = emptyList(),
        private val pronunciations: List<Pronunciation> = emptyList(),
        private val quizzes: List<Quiz> = emptyList()
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String): Flow<Result<List<Unit>>> = flowOf(Result.success(emptyList()))
        override fun getUnit(unitId: String): Flow<Result<Unit?>> = flowOf(Result.success(null))
        override fun getLessonsByUnit(unitId: String): Flow<Result<List<Lesson>>> = flowOf(Result.success(emptyList()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lesson))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(vocabularies))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(grammars))
        override fun getListeningByLesson(lessonId: String): Flow<Result<List<Listening>>> = flowOf(Result.success(listenings))
        override fun getPronunciationByLesson(lessonId: String): Flow<Result<List<Pronunciation>>> = flowOf(Result.success(pronunciations))
        override fun getQuizzesByLesson(lessonId: String): Flow<Result<List<Quiz>>> = flowOf(Result.success(quizzes))
    }
}
