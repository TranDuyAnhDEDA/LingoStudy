package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.local.ChatbotLocalStore
import com.example.mystudyapp.data.model.ChatMessage
import com.example.mystudyapp.data.model.UserLevelCalculator
import com.example.mystudyapp.data.network.GeminiChatService
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface ChatbotRepository {
    fun getApiKey(): String?
    fun saveApiKey(apiKey: String)
    fun clearApiKey()
    suspend fun buildUserContextSummary(uid: String): String
    suspend fun askParrot(history: List<ChatMessage>, userMessage: String, uid: String): Result<String>
    fun getInitialGreeting(displayName: String?): String
}

class ChatbotRepositoryImpl(
    private val localStore: ChatbotLocalStore = ChatbotLocalStore(),
    private val geminiService: GeminiChatService = GeminiChatService(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ChatbotRepository {

    override fun getApiKey(): String? = localStore.getApiKey()

    override fun saveApiKey(apiKey: String) = localStore.saveApiKey(apiKey)

    override fun clearApiKey() = localStore.clearApiKey()

    override suspend fun buildUserContextSummary(uid: String): String {
        if (uid.isBlank()) return ""

        return try {
            val profile = userRepository.getUserProfile(uid).firstOrNull()?.getOrNull()
            val progress = progressRepository.getUserProgress(uid).firstOrNull()?.getOrNull()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Calendar.getInstance().time)
            val todayRecord = progressRepository.getDailyStudyTime(uid, todayStr).firstOrNull()?.getOrNull()
            val todayMinutes = (todayRecord?.totalSeconds ?: 0L) / 60

            val currentLessonId = progress?.currentLessonId ?: "lesson_b1_1_1"
            val lesson = learningRepository.getLesson(currentLessonId).firstOrNull()?.getOrNull()
            val unit = if (lesson?.unitId != null) learningRepository.getUnit(lesson.unitId).firstOrNull()?.getOrNull() else null
            val lessonProgress = progressRepository.getLessonProgress(uid, currentLessonId).firstOrNull()?.getOrNull()

            val xp = profile?.xp ?: 0
            val levelInfo = UserLevelCalculator.calculate(xp, profile?.levelName)

            val incompleteSkills = mutableListOf<String>()
            if (lessonProgress?.vocabularyCompleted != true) incompleteSkills.add("Từ vựng")
            if (lessonProgress?.grammarCompleted != true) incompleteSkills.add("Ngữ pháp")
            if (lessonProgress?.listeningCompleted != true) incompleteSkills.add("Luyện nghe")
            if (lessonProgress?.pronunciationCompleted != true) incompleteSkills.add("Trò chơi mini game")
            if (lessonProgress?.quizCompleted != true) incompleteSkills.add("Bài kiểm tra")

            buildString {
                appendLine("- Tên học viên: ${profile?.displayName ?: "Bạn"}")
                appendLine("- Cấp độ hiện tại: ${levelInfo.levelName} (Cấp ${levelInfo.levelOrder})")
                appendLine("- Tổng XP tích lũy: $xp XP")
                appendLine("- Tiến độ cấp hiện tại: ${levelInfo.currentLevelXp}/${levelInfo.levelRequiredXp} XP (cần thêm ${levelInfo.levelRequiredXp - levelInfo.currentLevelXp} XP để lên Cấp ${levelInfo.levelOrder + 1})")
                appendLine("- Chuỗi ngày học (Streak): ${profile?.streak ?: 0} ngày")
                appendLine("- Thời gian học hôm nay: $todayMinutes phút")
                if (lesson != null) {
                    appendLine("- Bài học đang học: ${lesson.titleVi ?: lesson.title} (${unit?.titleVi ?: unit?.title ?: ""})")
                    if (incompleteSkills.isNotEmpty()) {
                        appendLine("- Các kỹ năng chưa hoàn thành trong bài: ${incompleteSkills.joinToString(", ")}")
                    } else {
                        appendLine("- Bài học hiện tại: Đã hoàn thành tất cả kỹ năng!")
                    }
                }
            }.trim()
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun askParrot(
        history: List<ChatMessage>,
        userMessage: String,
        uid: String
    ): Result<String> {
        val apiKey = getApiKey()
        val contextSummary = buildUserContextSummary(uid)
        return geminiService.generateResponse(
            apiKey = apiKey,
            history = history,
            userMessage = userMessage,
            userContextSummary = contextSummary
        )
    }

    override fun getInitialGreeting(displayName: String?): String {
        val name = displayName?.ifBlank { "bạn" } ?: "bạn"
        return "Chào $name! 🦜 Vẹt là gia sư tiếng Anh của bạn đây!\n\nBạn có thể hỏi Vẹt bất kỳ điều gì: tra nghĩa & cách dùng từ, giải thích ngữ pháp hay xin lời khuyên học tập hôm nay nhé!"
    }
}
