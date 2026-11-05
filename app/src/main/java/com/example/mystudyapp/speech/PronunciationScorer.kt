package com.example.mystudyapp.speech

import java.util.Locale
import kotlin.math.max

data class CharacterMatch(
    val char: Char,
    val isMatch: Boolean
)

enum class PronunciationGrade(val labelVi: String, val stars: Int) {
    EXCELLENT("Xuất sắc! 🎉 Chuẩn giọng bản xứ!", 3),
    GOOD("Rất tốt! 👏 Gần như hoàn hảo!", 2),
    AVERAGE("Khá tốt! 💡 Cần phát âm rõ hơn một chút!", 1),
    NEEDS_PRACTICE("Cố lên! 💪 Hãy nghe loa mẫu và thử lại nhé!", 0)
}

data class PronunciationScoreResult(
    val score: Int,
    val stars: Int,
    val grade: PronunciationGrade,
    val spokenText: String,
    val targetWord: String,
    val feedbackVi: String,
    val characterMatches: List<CharacterMatch>
)

object PronunciationScorer {

    /**
     * Scores the user's spoken candidates against the target word/phrase.
     * Takes the candidate that best matches the target.
     */
    fun evaluate(target: String, candidates: List<String>): PronunciationScoreResult {
        val cleanTarget = normalize(target)
        if (cleanTarget.isEmpty() || candidates.isEmpty()) {
            return PronunciationScoreResult(
                score = 0,
                stars = 0,
                grade = PronunciationGrade.NEEDS_PRACTICE,
                spokenText = candidates.firstOrNull() ?: "",
                targetWord = target,
                feedbackVi = "Chưa nhận diện được phát âm. Bạn hãy thử nói lại nhé!",
                characterMatches = target.map { CharacterMatch(it, false) }
            )
        }

        // Find best candidate
        var bestCandidate = candidates.first()
        var bestScore = -1

        for (candidate in candidates) {
            val score = calculateRawScore(cleanTarget, normalize(candidate))
            if (score > bestScore) {
                bestScore = score
                bestCandidate = candidate
            }
        }

        val finalScore = bestScore.coerceIn(0, 100)
        val grade = when {
            finalScore >= 90 -> PronunciationGrade.EXCELLENT
            finalScore >= 75 -> PronunciationGrade.GOOD
            finalScore >= 50 -> PronunciationGrade.AVERAGE
            else -> PronunciationGrade.NEEDS_PRACTICE
        }

        val charMatches = alignCharacterMatches(target, bestCandidate)

        return PronunciationScoreResult(
            score = finalScore,
            stars = grade.stars,
            grade = grade,
            spokenText = bestCandidate,
            targetWord = target,
            feedbackVi = grade.labelVi,
            characterMatches = charMatches
        )
    }

    private fun normalize(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calculateRawScore(target: String, spoken: String): Int {
        if (target == spoken) return 100
        if (target.isEmpty() || spoken.isEmpty()) return 0

        val targetWords = target.split(" ").filter { it.isNotBlank() }
        val spokenWords = spoken.split(" ").filter { it.isNotBlank() }

        // If target is single word
        if (targetWords.size == 1) {
            val tWord = targetWords[0]

            // If the user's speech contains the target word (e.g. "a cat", "the apple", "cat please")
            if (spokenWords.contains(tWord)) {
                return 95
            }

            // Find best matching word in spoken sentence
            var bestWordSimilarity = 0f
            for (sWord in spokenWords) {
                val sim = similarityRatio(tWord, sWord)
                if (sim > bestWordSimilarity) {
                    bestWordSimilarity = sim
                }
            }

            // Whole string similarity
            val wholeSim = similarityRatio(target, spoken)
            val finalSim = max(bestWordSimilarity, wholeSim)

            return (finalSim * 100).toInt()
        }

        // Multi-word target (e.g. "Good morning")
        var matchedCount = 0
        var totalWordSim = 0f

        for (tw in targetWords) {
            var bestWSim = 0f
            for (sw in spokenWords) {
                val sim = similarityRatio(tw, sw)
                if (sim > bestWSim) bestWSim = sim
            }
            if (bestWSim >= 0.85f) matchedCount++
            totalWordSim += bestWSim
        }

        val avgWordSim = totalWordSim / targetWords.size
        val wholeSim = similarityRatio(target, spoken)
        val combined = (avgWordSim * 0.7f + wholeSim * 0.3f)

        return (combined * 100).toInt()
    }

    private fun similarityRatio(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        val len1 = s1.length
        val len2 = s2.length
        val maxLen = max(len1, len2)
        if (maxLen == 0) return 1.0f

        val dist = levenshteinDistance(s1, s2)
        return (1.0f - dist.toFloat() / maxLen.toFloat()).coerceIn(0.0f, 1.0f)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // deletion
                    dp[i][j - 1] + 1,       // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Determines which characters in target matched the spoken output for visual letter coloring.
     */
    private fun alignCharacterMatches(target: String, spoken: String): List<CharacterMatch> {
        val cleanTarget = target.lowercase(Locale.ROOT)
        val cleanSpoken = spoken.lowercase(Locale.ROOT)

        if (cleanTarget == cleanSpoken) {
            return target.map { CharacterMatch(it, true) }
        }

        val result = mutableListOf<CharacterMatch>()
        var spokenIdx = 0

        for (i in target.indices) {
            val char = target[i]
            val lower = char.lowercaseChar()

            if (char.isWhitespace() || !char.isLetterOrDigit()) {
                result.add(CharacterMatch(char, true))
                continue
            }

            // Look for matching character within a small window in spoken text
            var matched = false
            for (w in maxOf(0, spokenIdx - 2)..minOf(cleanSpoken.length - 1, spokenIdx + 2)) {
                if (cleanSpoken[w] == lower) {
                    matched = true
                    spokenIdx = w + 1
                    break
                }
            }

            if (!matched && cleanSpoken.contains(lower)) {
                // Secondary fallback: if the char is present anywhere in spoken
                matched = true
            }

            result.add(CharacterMatch(char, matched))
        }

        return result
    }
}
