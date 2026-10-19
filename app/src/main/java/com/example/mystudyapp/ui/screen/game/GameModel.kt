package com.example.mystudyapp.ui.screen.game

import com.example.mystudyapp.data.model.Grammar
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.model.Vocabulary

enum class GameType {
    SENTENCE_BUILDER, // Trò 1: Ghép chữ để hoàn thành câu (Áp dụng cho Unit lẻ)
    FILL_IN_BLANK     // Trò 2: Điền từ còn thiếu vào ô trống (Áp dụng cho Unit chẵn)
}

data class WordToken(
    val id: Int,
    val text: String
)

data class GameQuestion(
    val id: String,
    val type: GameType,
    val promptVi: String,
    val fullSentence: String,
    val targetTokens: List<String> = emptyList(),
    val scrambledTokens: List<WordToken> = emptyList(),
    val sentenceWithBlank: String = "",
    val targetWord: String = "",
    val options: List<String> = emptyList(),
    val categoryLabel: String = "Ôn tập bài học",
    val audioSentence: String = fullSentence
)

object GameQuestionGenerator {

    fun resolveUnitOrder(unitId: String, unit: Unit?): Int {
        if (unit != null && unit.order > 0) return unit.order
        val match = Regex("""unit_.*_(\d+)""").find(unitId)
            ?: Regex("""unit_(\d+)""").find(unitId)
            ?: Regex("""(\d+)""").find(unitId)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }

    fun isUnitOdd(unitId: String, unit: Unit?): Boolean {
        val order = resolveUnitOrder(unitId, unit)
        return order % 2 != 0
    }

    private data class Candidate(
        val english: String,
        val vietnamese: String,
        val focusWord: String? = null,
        val category: String = "Từ vựng & Ngữ pháp"
    )

    fun generateQuestions(
        lesson: Lesson,
        unit: Unit?,
        vocabularies: List<Vocabulary>,
        grammars: List<Grammar>
    ): List<GameQuestion> {
        val isOdd = isUnitOdd(lesson.unitId, unit)
        val gameType = if (isOdd) GameType.SENTENCE_BUILDER else GameType.FILL_IN_BLANK

        val candidates = mutableListOf<Candidate>()

        // 1. Gather sentences from Grammar examples
        grammars.forEach { grammar ->
            grammar.examples.forEachIndexed { idx, en ->
                val vi = grammar.examplesVi?.getOrNull(idx) ?: "Mẫu câu thực hành"
                if (en.isNotBlank()) {
                    candidates.add(Candidate(en.trim(), vi.trim(), null, "Ngữ pháp: ${grammar.title}"))
                }
            }
        }

        // 2. Gather sentences from Vocabulary examples
        vocabularies.forEach { vocab ->
            if (vocab.example.isNotBlank()) {
                val vi = vocab.exampleVi?.ifBlank { null }
                    ?: vocab.meaningVi?.ifBlank { null }
                    ?: vocab.meaning
                candidates.add(Candidate(vocab.example.trim(), vi.trim(), vocab.word.trim(), "Từ vựng: ${vocab.word}"))
            } else if (vocab.word.isNotBlank()) {
                val viMean = vocab.meaningVi?.ifBlank { null } ?: vocab.meaning
                val builtSentence = "This is a ${vocab.word}."
                val builtVi = "Đây là một ${viMean}."
                candidates.add(Candidate(builtSentence, builtVi, vocab.word.trim(), "Từ vựng: ${vocab.word}"))
            }
        }

        // 3. Fallback candidates to guarantee at least 5 questions
        val fallbacks = listOf(
            Candidate("Hello, nice to meet you.", "Xin chào, rất vui được gặp bạn.", "Hello", "Giao tiếp cơ bản"),
            Candidate("Good morning to my teacher.", "Chào buổi sáng gửi đến cô giáo của tôi.", "Good", "Chào hỏi"),
            Candidate("I am a happy student.", "Tôi là một học sinh vui vẻ.", "happy", "Đại từ & Tính từ"),
            Candidate("How are you doing today?", "Hôm nay bạn cảm thấy thế nào?", "today", "Hỏi thăm sức khỏe"),
            Candidate("Thank you very much my friend.", "Cảm ơn người bạn của tôi rất nhiều.", "friend", "Lịch sự")
        )

        val allCandidates = (candidates + fallbacks).distinctBy { it.english.lowercase() }.take(5)

        // Convert candidates into game questions
        return allCandidates.mapIndexed { index, cand ->
            val qId = "game_q_${lesson.id}_$index"
            if (gameType == GameType.SENTENCE_BUILDER) {
                buildSentenceBuilderQuestion(qId, cand)
            } else {
                buildFillInBlankQuestion(qId, cand, vocabularies)
            }
        }
    }

    private fun buildSentenceBuilderQuestion(id: String, cand: Candidate): GameQuestion {
        val cleanEn = cand.english.trim()
        val tokens = cleanEn.split(Regex("""\s+""")).filter { it.isNotBlank() }

        // Scramble tokens
        var tokenObjects = tokens.mapIndexed { idx, t -> WordToken(idx, t) }.shuffled()
        if (tokenObjects.size > 1 && tokenObjects.map { it.text } == tokens) {
            tokenObjects = tokenObjects.reversed()
        }

        return GameQuestion(
            id = id,
            type = GameType.SENTENCE_BUILDER,
            promptVi = cand.vietnamese,
            fullSentence = cleanEn,
            targetTokens = tokens,
            scrambledTokens = tokenObjects,
            categoryLabel = cand.category,
            audioSentence = cleanEn
        )
    }

    private fun buildFillInBlankQuestion(
        id: String,
        cand: Candidate,
        allVocabs: List<Vocabulary>
    ): GameQuestion {
        val cleanEn = cand.english.trim()
        val tokens = cleanEn.split(Regex("""\s+""")).filter { it.isNotBlank() }

        // Determine target word to blank out
        var targetToken = ""
        if (!cand.focusWord.isNullOrBlank()) {
            val found = tokens.firstOrNull { it.contains(cand.focusWord, ignoreCase = true) }
            if (found != null) {
                targetToken = found
            }
        }

        if (targetToken.isBlank()) {
            // Find a common grammar / key word in tokens
            val keywords = listOf(
                "am", "is", "are", "do", "does", "have", "has", "can",
                "good", "nice", "morning", "afternoon", "evening", "name",
                "hello", "hi", "happy", "student", "teacher", "friend"
            )
            targetToken = tokens.firstOrNull { t ->
                val cleanWord = t.lowercase().replace(Regex("""[^a-z]"""), "")
                keywords.contains(cleanWord)
            } ?: tokens.firstOrNull { it.length >= 3 } ?: tokens.firstOrNull().orEmpty()
        }

        val cleanTargetWord = targetToken.replace(Regex("""[^a-zA-Z]"""), "")
        val sentenceWithBlank = cleanEn.replaceFirst(targetToken, "_____")

        // Build 4 options: correct word + 3 distractors
        val distractors = generateDistractors(cleanTargetWord, allVocabs)
        val allOptions = (listOf(cleanTargetWord) + distractors)
            .distinctBy { it.lowercase() }
            .take(4)
            .shuffled()

        return GameQuestion(
            id = id,
            type = GameType.FILL_IN_BLANK,
            promptVi = cand.vietnamese,
            fullSentence = cleanEn,
            sentenceWithBlank = sentenceWithBlank,
            targetWord = cleanTargetWord,
            options = allOptions,
            categoryLabel = cand.category,
            audioSentence = cleanEn
        )
    }

    private fun generateDistractors(targetWord: String, allVocabs: List<Vocabulary>): List<String> {
        val targetLower = targetWord.lowercase()

        // 1. Check known grammar sets
        val grammarSets = listOf(
            listOf("am", "is", "are", "be"),
            listOf("do", "does", "did", "doing"),
            listOf("have", "has", "had", "having"),
            listOf("Good", "Nice", "Happy", "Great"),
            listOf("morning", "afternoon", "evening", "night"),
            listOf("my", "your", "his", "her"),
            listOf("what", "who", "where", "how")
        )

        for (set in grammarSets) {
            if (set.any { it.equals(targetLower, ignoreCase = true) }) {
                return set.filter { !it.equals(targetLower, ignoreCase = true) }.take(3)
            }
        }

        // 2. Pull from other lesson vocabularies
        val vocabWords = allVocabs
            .map { it.word.trim() }
            .filter { it.isNotBlank() && !it.equals(targetLower, ignoreCase = true) }

        if (vocabWords.size >= 3) {
            return vocabWords.take(3)
        }

        // 3. Common fallback word bank
        val fallbackPool = listOf(
            "apple", "banana", "orange", "book", "pen", "desk",
            "friend", "school", "happy", "student", "teacher", "table"
        ).filter { !it.equals(targetLower, ignoreCase = true) }

        return (vocabWords + fallbackPool).distinctBy { it.lowercase() }.take(3)
    }
}
