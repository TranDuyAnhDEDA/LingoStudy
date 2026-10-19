package com.example.mystudyapp.data.network

import com.example.mystudyapp.data.model.ChatMessage
import com.example.mystudyapp.data.model.MessageSender
import com.example.mystudyapp.data.model.cleanChatText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

open class GeminiChatService(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val GEMINI_PRIMARY_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
        private const val GEMINI_SECONDARY_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
        private const val GEMINI_LEGACY_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    }

    open suspend fun generateResponse(
        apiKey: String?,
        history: List<ChatMessage>,
        userMessage: String,
        userContextSummary: String
    ): Result<String> = withContext(ioDispatcher) {
        val trimmedKey = apiKey?.trim().orEmpty()

        // If no API key is set, use the intelligent fallback tutor response
        if (trimmedKey.isBlank()) {
            return@withContext Result.success(
                generateFallbackResponse(userMessage, userContextSummary, hasApiKey = false)
            )
        }

        val endpoints = listOf(
            "$GEMINI_PRIMARY_URL?key=$trimmedKey",
            "$GEMINI_SECONDARY_URL?key=$trimmedKey",
            "$GEMINI_LEGACY_URL?key=$trimmedKey"
        )

        var lastException: Exception? = null
        val requestBody = buildRequestBody(history, userMessage, userContextSummary)

        for (endpoint in endpoints) {
            try {
                val jsonResponse = executeHttpPost(endpoint, requestBody, trimmedKey)
                val parsedText = parseGeminiResponse(jsonResponse)
                if (!parsedText.isNullOrBlank()) {
                    return@withContext Result.success(parsedText)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        // Graceful fallback on network error or quota exceeded
        val fallback = generateFallbackResponse(
            userMessage = userMessage,
            userContextSummary = userContextSummary,
            hasApiKey = true,
            errorReason = lastException?.message
        )
        Result.success(fallback)
    }

    private fun buildRequestBody(
        history: List<ChatMessage>,
        userMessage: String,
        userContextSummary: String
    ): String {
        val root = JSONObject()

        // System Instruction defining the Persona
        val systemText = buildString {
            appendLine("Bạn là Vẹt (LingoParrot) - giáo viên tiếng Anh thông minh, thân thiện, vui tính và tâm lý của ứng dụng LingoStudy.")
            appendLine("QUY TẮC BẮT BUỘC:")
            appendLine("1. Luôn xưng là 'Vẹt' và gọi người dùng là 'Bạn'. Giữ giọng điệu ấm áp, ân cần, khích lệ như một gia sư tiếng Việt tận tâm.")
            appendLine("2. Khi người dùng nhập một từ hoặc cụm từ tiếng Anh (ví dụ 'happy', 'book', 'apple'): HÃY TỰ ĐỘNG GIẢI THÍCH NGAY từ loại, phiên âm dạng [phiên âm], nghĩa tiếng Việt rõ ràng, 2 câu ví dụ giao tiếp thực tế kèm dịch nghĩa và mẹo ghi nhớ. KHÔNG hỏi ngược lại hay lặp từ, mà hãy giải thích cặn kẽ ngay lập tức!")
            appendLine("3. Khi giải thích ngữ pháp: trình bày ngắn gọn, dễ hiểu, có công thức và ví dụ trực quan, tránh dài dòng học thuật.")
            appendLine("4. Khi người dùng hỏi lời khuyên hoặc tiến độ: hãy dựa vào dữ liệu học tập hiện tại của họ để tư vấn cụ thể và động viên.")
            appendLine("5. ĐỊNH DẠNG VĂN BẢN:")
            appendLine("   - TUYỆT ĐỐI KHÔNG dùng ký tự markdown hoa thị '*', '**', dấu thăng '###', gạch nối '---'.")
            appendLine("   - KHÔNG dùng dấu xẹt '/' quanh từ hoặc phiên âm (hãy viết [phiên âm] hoặc viết trực tiếp).")
            appendLine("   - Trình bày câu trả lời bằng văn bản thuần sạch sẽ, thoáng mắt, dùng dấu gạch đầu dòng '-' hoặc dấu chấm tròn '•' kèm emoji nhẹ nhàng.")
            if (userContextSummary.isNotBlank()) {
                appendLine("THÔNG TIN HỌC TẬP HIỆN TẠI CỦA BẠN HỌC VIÊN:")
                appendLine(userContextSummary)
            }
        }

        val systemObj = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", systemText) })
            })
        }
        root.put("system_instruction", systemObj)

        // Contents (Multi-turn conversation)
        val contentsArray = JSONArray()

        // Take last 8 messages for context, excluding any duplicate current turn
        val filtered = history.filter { !it.isLoading && !it.isError }
        val recentHistory = if (filtered.lastOrNull()?.sender == MessageSender.USER && filtered.lastOrNull()?.text?.trim() == userMessage.trim()) {
            filtered.dropLast(1).takeLast(8)
        } else {
            filtered.takeLast(8)
        }
        for (msg in recentHistory) {
            if (msg.isLoading || msg.isError) continue
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            val turn = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", msg.text) })
                })
            }
            contentsArray.put(turn)
        }

        // Current message
        val currentTurn = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", userMessage) })
            })
        }
        contentsArray.put(currentTurn)
        root.put("contents", contentsArray)

        // Generation Config
        val config = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 800)
            put("topP", 0.95)
        }
        root.put("generationConfig", config)

        return root.toString()
    }

    protected open fun executeHttpPost(urlString: String, jsonBody: String, apiKey: String? = null): String {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            if (!apiKey.isNullOrBlank()) {
                setRequestProperty("x-goog-api-key", apiKey)
            }
        }

        OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
            writer.write(jsonBody)
            writer.flush()
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: throw Exception("HTTP Error: $responseCode")
        }

        val response = StringBuilder()
        BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line).append('\n')
            }
        }

        if (responseCode !in 200..299) {
            val errMsg = try {
                val errJson = JSONObject(response.toString())
                errJson.optJSONObject("error")?.optString("message") ?: "Lỗi $responseCode từ Gemini"
            } catch (e: Exception) {
                "Lỗi kết nối $responseCode"
            }
            throw Exception(errMsg)
        }

        return response.toString()
    }

    private fun parseGeminiResponse(jsonString: String): String? {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                textBuilder.append(part.optString("text", ""))
            }
            cleanChatText(textBuilder.toString()).ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fallback tutor logic providing personalized advice & offline knowledge.
     */
    fun generateFallbackResponse(
        userMessage: String,
        userContextSummary: String,
        hasApiKey: Boolean,
        errorReason: String? = null
    ): String {
        val lower = userMessage.lowercase().trim()

        return when {
            lower.contains("chào") || lower.contains("hello") || lower.contains("hi") -> {
                "Chào bạn nhé! Vẹt rất vui được gặp bạn 🦜 Hôm nay bạn muốn Vẹt hướng dẫn tra từ, giải thích ngữ pháp hay xem lời khuyên học tập nào?"
            }
            lower.contains("khuyên") || lower.contains("lời khuyên") || lower.contains("nên học") || lower.contains("hôm nay") -> {
                buildString {
                    appendLine("🦜 Vẹt xem qua tiến độ của bạn rồi nè:")
                    if (userContextSummary.isNotBlank()) {
                        appendLine(userContextSummary)
                    }
                    appendLine("\n💡 Lời khuyên của Vẹt:")
                    appendLine("• Mỗi ngày dành ra 15 phút học đều đặn tốt hơn học dồn một lúc.")
                    appendLine("• Đừng quên luyện tập phần Trò chơi và Bài kiểm tra để củng cố phản xạ và nhận thưởng XP nhé!")
                    appendLine("• Cố gắng duy trì Streak học tập để hình thành thói quen ngôn ngữ tự nhiên bạn nhé!")
                }
            }
            lower.contains("tiến độ") || lower.contains("cấp") || lower.contains("xp") -> {
                buildString {
                    appendLine("🦜 Thống kê học tập của bạn:")
                    if (userContextSummary.isNotBlank()) {
                        appendLine(userContextSummary)
                    }
                    appendLine("\nCố gắng thêm một chút nữa là thăng cấp tiếp theo rồi bạn ơi! Cần hỏi gì cứ nhắn Vẹt nhé!")
                }
            }
            lower.contains("ngữ pháp") -> {
                "🦜 Bạn đang băn khoăn về chủ điểm ngữ pháp nào? Ví dụ thì Hiện tại đơn (Present Simple), câu hỏi với Wh- hay cách dùng mạo từ A/An/The? Bạn cứ gõ câu hỏi cụ thể, Vẹt sẽ giải thích cặn kẽ kèm ví dụ cho bạn ngay!"
            }
            lower.contains("từ vựng") || lower.contains("nghĩa của") || lower.contains("là gì") -> {
                "🦜 Để tra từ, bạn có thể gõ trực tiếp từ tiếng Anh cần tra vào đây (ví dụ: 'resilient là gì' hoặc 'cách dùng từ appreciate'). Vẹt sẽ chỉ bạn phiên âm chuẩn, từ loại, nghĩa tiếng Việt và cách ứng dụng vào câu giao tiếp nhé!"
            }
            else -> {
                "🦜 Vẹt hiểu câu hỏi của bạn! Vẹt luôn sẵn sàng hỗ trợ bạn học tập. Bạn có thể hỏi Vẹt bất kỳ từ vựng, ngữ pháp hay lời khuyên học tập nào nhé!"
            }
        }
    }
}
