package com.example.mystudyapp

import com.example.mystudyapp.data.local.ChatbotLocalStore
import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.DailyStudyTime
import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Listening
import com.example.mystudyapp.data.model.MessageSender
import com.example.mystudyapp.data.model.Pronunciation
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.model.Vocabulary
import com.example.mystudyapp.data.network.GeminiChatService
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.ChatbotRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.viewmodel.ChatbotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotFeatureTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Fake Repositories ---

    private class FakeUserRepository(
        private val profile: UserProfile? = null
    ) : UserRepository {
        override fun getUserProfile(uid: String): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
        override suspend fun createUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProfile(user: UserProfile): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateDisplayName(uid: String, displayName: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getCurrentUserProfile(): Flow<Result<UserProfile?>> = flowOf(Result.success(profile))
    }

    private class FakeProgressRepository(
        private val progress: UserProgress? = null,
        private val lessonProgress: LessonProgress? = null,
        private val dailyStudyTime: DailyStudyTime? = null
    ) : ProgressRepository {
        override fun getUserProgress(uid: String): Flow<Result<UserProgress?>> = flowOf(Result.success(progress))
        override suspend fun createUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun updateUserProgress(progress: UserProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override fun getLessonProgress(uid: String, lessonId: String): Flow<Result<LessonProgress?>> = flowOf(Result.success(lessonProgress))
        override suspend fun updateLessonProgress(uid: String, progress: LessonProgress): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun markLessonCompleted(uid: String, lessonId: String): Result<kotlin.Unit> = Result.success(kotlin.Unit)
        override suspend fun completeLessonIfEligible(uid: String, lessonId: String): Result<Boolean> = Result.success(false)
        override fun getDailyStudyTime(uid: String, dateString: String): Flow<Result<DailyStudyTime?>> = flowOf(Result.success(dailyStudyTime))
    }

    private class FakeLearningRepository(
        private val lesson: Lesson? = null,
        private val unit: Unit? = null
    ) : LearningRepository {
        override fun getPrograms(): Flow<Result<List<LearningProgram>>> = flowOf(Result.success(emptyList()))
        override fun getProgram(programId: String): Flow<Result<LearningProgram?>> = flowOf(Result.success(null))
        override fun getLevelsByProgram(programId: String): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevels(): Flow<Result<List<Level>>> = flowOf(Result.success(emptyList()))
        override fun getLevel(levelId: String): Flow<Result<Level?>> = flowOf(Result.success(null))
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

    private class FakeAuthRepository(
        private val currentUser: AuthUser? = null
    ) : AuthRepository {
        override val authState: Flow<AuthState> = flowOf(if (currentUser != null) AuthState.Authenticated(currentUser) else AuthState.Unauthenticated)
        override suspend fun register(email: String, password: String, displayName: String): Result<AuthUser> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthUser> = Result.failure(Exception())
        override fun logout() {}
        override fun getCurrentUser(): AuthUser? = currentUser
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
    }

    @Test
    fun testInitialGreetingIncludesDisplayName() {
        val store = ChatbotLocalStore()
        val repo = ChatbotRepositoryImpl(
            localStore = store,
            userRepository = FakeUserRepository(),
            progressRepository = FakeProgressRepository(),
            learningRepository = FakeLearningRepository()
        )
        val greetingWithUser = repo.getInitialGreeting("Minh Anh")
        assertTrue(greetingWithUser.contains("Minh Anh"))
        assertTrue(greetingWithUser.contains("🦜"))
        assertTrue(greetingWithUser.contains("Vẹt"))

        val greetingDefault = repo.getInitialGreeting(null)
        assertTrue(greetingDefault.contains("bạn"))
    }

    @Test
    fun testApiKeyPersistenceInLocalStore() {
        val store = ChatbotLocalStore()
        assertEquals(ChatbotLocalStore.DEFAULT_API_KEY, store.getApiKey())

        store.saveApiKey("AIzaSyTestApiKey123456")
        assertEquals("AIzaSyTestApiKey123456", store.getApiKey())

        store.clearApiKey()
        assertNull(store.getApiKey())
    }

    @Test
    fun testFallbackTutorGreetingResponse() {
        val service = GeminiChatService()
        val response = service.generateFallbackResponse(
            userMessage = "Xin chào Vẹt!",
            userContextSummary = "",
            hasApiKey = false
        )
        assertTrue(response.contains("Chào bạn nhé!"))
        assertTrue(response.contains("🦜"))
    }

    @Test
    fun testFallbackTutorPersonalizedAdviceWithUserContext() {
        val service = GeminiChatService()
        val context = """
            - Tên học viên: Duy Anh
            - Cấp độ hiện tại: Level 2
            - Tổng XP tích lũy: 320 XP
            - Các kỹ năng chưa hoàn thành trong bài: Ngữ pháp, Bài kiểm tra
        """.trimIndent()

        val response = service.generateFallbackResponse(
            userMessage = "Hôm nay tôi nên học gì?",
            userContextSummary = context,
            hasApiKey = false
        )

        assertTrue(response.contains("Duy Anh"))
        assertTrue(response.contains("Ngữ pháp"))
        assertTrue(response.contains("Lời khuyên của Vẹt"))
        assertTrue(response.contains("15 phút"))
    }

    @Test
    fun testFallbackTutorGrammarAndVocabularyGuidance() {
        val service = GeminiChatService()

        val grammarResponse = service.generateFallbackResponse(
            userMessage = "Giải thích ngữ pháp giúp mình với",
            userContextSummary = "",
            hasApiKey = false
        )
        assertTrue(grammarResponse.contains("ngữ pháp"))
        assertTrue(grammarResponse.contains("Hiện tại đơn"))

        val vocabResponse = service.generateFallbackResponse(
            userMessage = "Từ vựng này nghĩa là gì?",
            userContextSummary = "",
            hasApiKey = false
        )
        assertTrue(vocabResponse.contains("tra từ"))
        assertTrue(vocabResponse.contains("phiên âm"))
    }

    @Test
    fun testCleanChatTextRemovesAsterisksAndSlashes() {
        val raw = "### **1. BOOK (danh từ)**\n* **Phiên âm IPA**: /bʊk/\n* **Ví dụ**: We need to **book** a table.\n---"
        val cleaned = com.example.mystudyapp.data.model.cleanChatText(raw)
        assertFalse(cleaned.contains("*"))
        assertFalse(cleaned.contains("###"))
        assertFalse(cleaned.contains("---"))
        assertTrue(cleaned.contains("[bʊk]"))
        assertTrue(cleaned.contains("1. BOOK (danh từ)"))
        assertTrue(cleaned.contains("• Phiên âm IPA: [bʊk]"))
    }

    @Test
    fun testUserContextSummaryBuilder() = runTest(testDispatcher) {
        val testUser = UserProfile(
            uid = "user_123",
            displayName = "Thanh Thảo",
            email = "thanhthao@example.com",
            xp = 250,
            streak = 5
        )
        val testProgress = UserProgress(
            userId = "user_123",
            currentLessonId = "lesson_1",
            completedLessons = 2
        )
        val testLesson = Lesson(
            id = "lesson_1",
            unitId = "unit_1",
            title = "Greetings",
            titleVi = "Chào hỏi cơ bản"
        )
        val testUnit = Unit(
            id = "unit_1",
            levelId = "level_1",
            title = "Unit 1",
            titleVi = "Bài mở đầu"
        )
        val testLessonProgress = LessonProgress(
            lessonId = "lesson_1",
            vocabularyCompleted = true,
            grammarCompleted = false,
            listeningCompleted = false,
            pronunciationCompleted = true,
            quizCompleted = false
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)

        val userRepo = FakeUserRepository(testUser)
        val progressRepo = FakeProgressRepository(
            progress = testProgress,
            lessonProgress = testLessonProgress,
            dailyStudyTime = DailyStudyTime(date = todayStr, totalSeconds = 1200)
        )
        val learningRepo = FakeLearningRepository(
            lesson = testLesson,
            unit = testUnit
        )

        val chatbotRepo = ChatbotRepositoryImpl(
            localStore = ChatbotLocalStore(),
            geminiService = GeminiChatService(),
            userRepository = userRepo,
            progressRepository = progressRepo,
            learningRepository = learningRepo
        )

        val summary = chatbotRepo.buildUserContextSummary("user_123")
        assertTrue(summary.contains("Thanh Thảo"))
        assertTrue(summary.contains("250 XP"))
        assertTrue(summary.contains("5 ngày"))
        assertTrue(summary.contains("20 phút"))
        assertTrue(summary.contains("Chào hỏi cơ bản"))
        assertTrue(summary.contains("Ngữ pháp"))
        assertTrue(summary.contains("Luyện nghe"))
        assertTrue(summary.contains("Bài kiểm tra"))
        assertFalse(summary.contains("Từ vựng,")) // Vocabulary was already completed
    }

    @Test
    fun testChatbotViewModelFlow() = runTest(testDispatcher) {
        val authUser = AuthUser(
            uid = "user_test",
            email = "user@test.com",
            displayName = "Hoàng Nam"
        )
        val authRepo = FakeAuthRepository(authUser)
        val chatbotRepo = ChatbotRepositoryImpl(
            localStore = ChatbotLocalStore(defaultApiKey = null),
            geminiService = GeminiChatService(ioDispatcher = testDispatcher),
            userRepository = FakeUserRepository(),
            progressRepository = FakeProgressRepository(),
            learningRepository = FakeLearningRepository()
        )

        val viewModel = ChatbotViewModel(
            chatbotRepository = chatbotRepo,
            authRepository = authRepo
        )

        // 1. Initial State
        val initialState = viewModel.uiState.value
        assertEquals(1, initialState.messages.size)
        assertEquals(MessageSender.PARROT, initialState.messages.first().sender)
        assertTrue(initialState.messages.first().text.contains("Hoàng Nam"))
        assertFalse(initialState.isGenerating)

        // 2. Send Message
        viewModel.sendMessage("Xin chào Vẹt!")
        // Immediately after sending, user message and loading message are present
        val intermediateState = viewModel.uiState.value
        assertTrue(intermediateState.isGenerating)
        assertEquals(3, intermediateState.messages.size)
        assertEquals(MessageSender.USER, intermediateState.messages[1].sender)
        assertEquals("Xin chào Vẹt!", intermediateState.messages[1].text)
        assertTrue(intermediateState.messages[2].isLoading)

        // Advance coroutines
        advanceUntilIdle()

        // 3. Response Received
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isGenerating)
        assertEquals(3, finalState.messages.size)
        assertFalse(finalState.messages.any { it.isLoading })
        val parrotReply = finalState.messages.last()
        assertEquals(MessageSender.PARROT, parrotReply.sender)
        assertTrue(parrotReply.text.contains("Chào bạn nhé!"))

        // 4. API Key save
        viewModel.saveApiKey("AIzaSyMyNewApiKey")
        assertEquals("AIzaSyMyNewApiKey", viewModel.uiState.value.apiKey)
        assertFalse(viewModel.uiState.value.showApiKeyDialog)

        // 5. Clear Chat
        viewModel.clearChat()
        val clearedState = viewModel.uiState.value
        assertEquals(1, clearedState.messages.size)
        assertEquals(MessageSender.PARROT, clearedState.messages.first().sender)
    }
}
