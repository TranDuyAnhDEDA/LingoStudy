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
import com.example.mystudyapp.data.model.Unit as CurriculumUnit
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.ui.screen.game.GameQuestionGenerator
import com.example.mystudyapp.ui.screen.game.GameType
import com.example.mystudyapp.viewmodel.GameViewModel
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
class GameFeatureTest {

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
    fun testOddUnitGeneratesSentenceBuilderGame() {
        val unit1 = CurriculumUnit(id = "unit_kids_1", title = "Unit 1: Greetings", order = 1)
        val lesson = Lesson(id = "lesson_kids_1_1", unitId = "unit_kids_1", title = "Hello & Welcome")
        val grammars = listOf(
            Grammar(
                id = "g1",
                lessonId = lesson.id,
                title = "Chào hỏi",
                explanation = "Cách chào",
                examples = listOf("Good morning, teacher!", "How are you today?"),
                examplesVi = listOf("Chào buổi sáng cô giáo!", "Hôm nay bạn thế nào?")
            )
        )
        val vocabs = listOf(
            Vocabulary(id = "v1", lessonId = lesson.id, word = "hello", example = "Hello, nice to meet you.", exampleVi = "Xin chào, rất vui được gặp bạn.")
        )

        val questions = GameQuestionGenerator.generateQuestions(lesson, unit1, vocabs, grammars)

        assertEquals(5, questions.size)
        questions.forEach { q ->
            assertEquals("Odd unit 1 must use SENTENCE_BUILDER game", GameType.SENTENCE_BUILDER, q.type)
            assertTrue("Target tokens must not be empty", q.targetTokens.isNotEmpty())
            assertTrue("Scrambled tokens must not be empty", q.scrambledTokens.isNotEmpty())
            assertEquals(q.targetTokens.size, q.scrambledTokens.size)
            assertTrue("Vietnamese prompt must be present", q.promptVi.isNotBlank())
        }
    }

    @Test
    fun testEvenUnitGeneratesFillInBlankGame() {
        val unit2 = CurriculumUnit(id = "unit_kids_2", title = "Unit 2: Alphabet", order = 2)
        val lesson = Lesson(id = "lesson_kids_2_1", unitId = "unit_kids_2", title = "Letter A")
        val grammars = listOf(
            Grammar(
                id = "g2",
                lessonId = lesson.id,
                title = "To be",
                explanation = "Động từ to be",
                examples = listOf("I am happy.", "She is tall."),
                examplesVi = listOf("Tôi rất vui vẻ.", "Cô ấy cao.")
            )
        )
        val vocabs = listOf(
            Vocabulary(id = "v2", lessonId = lesson.id, word = "apple", example = "She has an apple.", exampleVi = "Cô ấy có một quả táo.")
        )

        val questions = GameQuestionGenerator.generateQuestions(lesson, unit2, vocabs, grammars)

        assertEquals(5, questions.size)
        questions.forEach { q ->
            assertEquals("Even unit 2 must use FILL_IN_BLANK game", GameType.FILL_IN_BLANK, q.type)
            assertTrue("Sentence with blank must contain _____ indicator", q.sentenceWithBlank.contains("_____"))
            assertTrue("Target word must be non-blank", q.targetWord.isNotBlank())
            assertEquals("Must have 4 options", 4, q.options.size)
            assertTrue("Options must contain target word", q.options.any { it.equals(q.targetWord, ignoreCase = true) })
        }
    }

    @Test
    fun testUnitOrderResolutionPattern() {
        assertEquals(1, GameQuestionGenerator.resolveUnitOrder("unit_kids_1", null))
        assertEquals(2, GameQuestionGenerator.resolveUnitOrder("unit_kids_2", null))
        assertEquals(3, GameQuestionGenerator.resolveUnitOrder("unit_b1_3", null))
        assertEquals(4, GameQuestionGenerator.resolveUnitOrder("unit_b1_4", null))
        assertEquals(5, GameQuestionGenerator.resolveUnitOrder("unit_5", null))

        assertTrue("Unit 1 is odd", GameQuestionGenerator.isUnitOdd("unit_1", null))
        assertFalse("Unit 2 is even", GameQuestionGenerator.isUnitOdd("unit_2", null))
        assertTrue("Unit 3 is odd", GameQuestionGenerator.isUnitOdd("unit_3", null))
        assertFalse("Unit 4 is even", GameQuestionGenerator.isUnitOdd("unit_4", null))
    }

    @Test
    fun testSentenceBuilderGameplayFlow() {
        val testUser = AuthUser(uid = "user_game_1", email = "game@test.com", displayName = "Player")
        val fakeAuth = FakeGameAuthRepository(currentUser = testUser)

        val unit = CurriculumUnit(id = "unit_1", title = "Unit 1", order = 1)
        val lesson = Lesson(id = "lesson_1", unitId = "unit_1", title = "Lesson 1")
        val grammar = Grammar(
            id = "g1",
            lessonId = "lesson_1",
            title = "Chào hỏi",
            explanation = "Chào hỏi",
            examples = listOf("Good morning teacher"),
            examplesVi = listOf("Chào buổi sáng cô giáo")
        )

        val fakeLearning = FakeGameLearningRepository(
            lessonMap = mapOf("lesson_1" to lesson),
            unitMap = mapOf("unit_1" to unit),
            grammarList = listOf(grammar)
        )
        val fakeProgress = FakeGameProgressRepository()

        val viewModel = GameViewModel(
            lessonId = "lesson_1",
            authRepository = fakeAuth,
            progressRepository = fakeProgress,
            learningRepository = fakeLearning
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(GameType.SENTENCE_BUILDER, state.gameType)
        assertEquals(5, state.questions.size)

        // Select tokens in order
        val currentQ = state.questions[0]
        currentQ.targetTokens.forEach { targetWord ->
            val token = viewModel.uiState.value.availableTokens.first { it.text == targetWord }
            viewModel.selectToken(token)
        }

        viewModel.checkAnswer()
        assertTrue("Sentence should be marked correct", viewModel.uiState.value.isChecked)
        assertTrue(viewModel.uiState.value.isCorrect)
        assertEquals(1, viewModel.uiState.value.score)

        // Advance to next question
        viewModel.nextQuestion()
        assertEquals(1, viewModel.uiState.value.currentIndex)
        assertFalse(viewModel.uiState.value.isChecked)
    }

    @Test
    fun testFillInBlankGameplayAndProgressCompletion() {
        val testUser = AuthUser(uid = "user_game_2", email = "game2@test.com", displayName = "Player 2")
        val fakeAuth = FakeGameAuthRepository(currentUser = testUser)

        val unit = CurriculumUnit(id = "unit_2", title = "Unit 2", order = 2)
        val lesson = Lesson(id = "lesson_2", unitId = "unit_2", title = "Lesson 2")

        val fakeLearning = FakeGameLearningRepository(
            lessonMap = mapOf("lesson_2" to lesson),
            unitMap = mapOf("unit_2" to unit)
        )
        val fakeProgress = FakeGameProgressRepository()

        val viewModel = GameViewModel(
            lessonId = "lesson_2",
            authRepository = fakeAuth,
            progressRepository = fakeProgress,
            learningRepository = fakeLearning
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(GameType.FILL_IN_BLANK, state.gameType)

        // Complete all 5 questions
        for (i in 0 until state.questions.size) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.targetWord)
            viewModel.checkAnswer()
            assertTrue(viewModel.uiState.value.isCorrect)
            viewModel.nextQuestion()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue("Game should be finished after 5 questions", finalState.isFinished)
        assertEquals(5, finalState.score)
        assertTrue("Progress repository must record pronunciationCompleted", fakeProgress.lastUpdatedProgress?.pronunciationCompleted == true)
    }

    // FAKES
    class FakeGameAuthRepository(private val currentUser: AuthUser?) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    class FakeGameProgressRepository : ProgressRepository {
        var lastUpdatedProgress: LessonProgress? = null

        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> =
            flowOf(Result.success(lastUpdatedProgress))

        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> {
            lastUpdatedProgress = progress
            return Result.success(kotlin.Unit)
        }

        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(null))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(true)
    }

    class FakeGameLearningRepository(
        private val lessonMap: Map<String, Lesson> = emptyMap(),
        private val unitMap: Map<String, CurriculumUnit> = emptyMap(),
        private val vocabList: List<Vocabulary> = emptyList(),
        private val grammarList: List<Grammar> = emptyList()
    ) : LearningRepository {
        override fun getPrograms() = flowOf(Result.success(emptyList<LearningProgram>()))
        override fun getProgram(programId: String) = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String) = flowOf(Result.success(emptyList<Level>()))
        override fun getLevels() = flowOf(Result.success(emptyList<Level>()))
        override fun getLevel(levelId: String) = flowOf(Result.success(null))
        override fun getUnitsByLevel(levelId: String) = flowOf(Result.success(emptyList<CurriculumUnit>()))
        override fun getUnit(unitId: String): Flow<Result<CurriculumUnit?>> = flowOf(Result.success(unitMap[unitId]))
        override fun getLessonsByUnit(unitId: String) = flowOf(Result.success(emptyList<Lesson>()))
        override fun getLesson(lessonId: String): Flow<Result<Lesson?>> = flowOf(Result.success(lessonMap[lessonId]))
        override fun getVocabularyByLesson(lessonId: String): Flow<Result<List<Vocabulary>>> = flowOf(Result.success(vocabList))
        override fun getGrammarByLesson(lessonId: String): Flow<Result<List<Grammar>>> = flowOf(Result.success(grammarList))
        override fun getListeningByLesson(lessonId: String) = flowOf(Result.success(emptyList<Listening>()))
        override fun getPronunciationByLesson(lessonId: String) = flowOf(Result.success(emptyList<Pronunciation>()))
        override fun getQuizzesByLesson(lessonId: String) = flowOf(Result.success(emptyList<Quiz>()))
    }
}
