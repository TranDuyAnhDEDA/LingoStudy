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
    val isEnglishAvailable: Boolean = true
)

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
