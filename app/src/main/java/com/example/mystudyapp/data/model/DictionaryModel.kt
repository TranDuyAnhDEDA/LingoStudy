package com.example.mystudyapp.data.model

data class DictionaryResult(
    val word: String,
    val ipa: String? = null,
    val audioUrl: String? = null,
    val vietnameseMeanings: List<VietnameseMeaningGroup> = emptyList(),
    val englishMeanings: List<EnglishMeaningGroup> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val isVietnameseAvailable: Boolean = true,
    val isEnglishAvailable: Boolean = true,
    val primaryExample: String? = null,
    val primaryExampleVi: String? = null
) {
    val displayPartOfSpeech: String
        get() {
            val viPos = vietnameseMeanings.firstOrNull()?.partOfSpeech?.trim()
            if (!viPos.isNullOrBlank()) {
                return when (viPos.lowercase()) {
                    "noun", "danh từ" -> "danh từ"
                    "verb", "động từ" -> "động từ"
                    "adjective", "tính từ" -> "tính từ"
                    "adverb", "trạng từ" -> "trạng từ"
                    "interjection", "thán từ" -> "thán từ"
                    else -> viPos.lowercase()
                }
            }
            val enPos = englishMeanings.firstOrNull()?.partOfSpeech?.trim()
            return when (enPos?.lowercase()) {
                "noun" -> "danh từ"
                "verb" -> "động từ"
                "adjective" -> "tính từ"
                "adverb" -> "trạng từ"
                "interjection" -> "thán từ"
                else -> enPos?.lowercase() ?: "từ vựng"
            }
        }

    val displayMeaningVi: String
        get() {
            val firstVi = vietnameseMeanings.firstOrNull()?.definitions?.firstOrNull()?.trim()
            if (!firstVi.isNullOrBlank()) {
                return firstVi.replace(Regex("\\[.*?\\]"), "").trim().trimEnd('.', ',')
            }
            val firstEn = englishMeanings.firstOrNull()?.definitions?.firstOrNull()?.definition?.trim()
            return firstEn ?: "Không có định nghĩa"
        }

    val displayExampleEn: String?
        get() {
            if (!primaryExample.isNullOrBlank()) return primaryExample
            for (group in englishMeanings) {
                for (def in group.definitions) {
                    if (!def.example.isNullOrBlank()) return def.example
                }
            }
            return null
        }

    val displayExampleVi: String?
        get() {
            if (!primaryExampleVi.isNullOrBlank()) return primaryExampleVi
            return null
        }
}

data class VietnameseMeaningGroup(
    val partOfSpeech: String,
    val definitions: List<String>
)

data class EnglishMeaningGroup(
    val partOfSpeech: String,
    val definitions: List<EnglishDefinition>
)

data class EnglishDefinition(
    val definition: String,
    val example: String? = null
)
