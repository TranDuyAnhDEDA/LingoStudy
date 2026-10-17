package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.local.DictionaryLocalStore
import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.data.model.EnglishDefinition
import com.example.mystudyapp.data.model.EnglishMeaningGroup
import com.example.mystudyapp.data.model.VietnameseMeaningGroup
import com.example.mystudyapp.data.network.DictionaryApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface DictionaryRepository {
    fun lookupWord(word: String): Flow<Result<DictionaryResult>>
    fun getSearchHistory(): List<String> = emptyList()
    fun addSearchHistory(word: String) {}
    fun removeSearchHistory(word: String) {}
    fun clearSearchHistory() {}
    fun getFavoriteWords(): Set<String> = emptySet()
    fun isFavorite(word: String): Boolean = false
    fun toggleFavorite(word: String): Boolean = false
}

class DictionaryRepositoryImpl(
    private val apiClient: DictionaryApiClient = DictionaryApiClient(),
    private val localStore: DictionaryLocalStore = DictionaryLocalStore()
) : DictionaryRepository {

    override fun getSearchHistory(): List<String> = localStore.getSearchHistory()

    override fun addSearchHistory(word: String) = localStore.addSearchHistory(word)

    override fun removeSearchHistory(word: String) = localStore.removeSearchHistory(word)

    override fun clearSearchHistory() = localStore.clearSearchHistory()

    override fun getFavoriteWords(): Set<String> = localStore.getFavoriteWords()

    override fun isFavorite(word: String): Boolean = localStore.isFavorite(word)

    override fun toggleFavorite(word: String): Boolean = localStore.toggleFavorite(word)

    override fun lookupWord(word: String): Flow<Result<DictionaryResult>> = flow {
        val trimmed = word.trim()
        if (trimmed.isBlank()) {
            emit(Result.failure(Exception("Vui lòng nhập từ cần tra cứu.")))
            return@flow
        }

        val key = trimmed.lowercase()
        val curated = CURATED_DICTIONARY[key]

        val result = coroutineScope {
            val viDeferred = async { apiClient.lookupVietnameseDictionary(trimmed) }
            val enDeferred = async { apiClient.lookupEnglishDictionary(trimmed) }

            val viResult = viDeferred.await()
            val enResult = enDeferred.await()

            val viData = viResult.getOrNull()
            val enData = enResult.getOrNull()

            if (curated != null) {
                // Curated entry available: merge with online phonetics/audioUrl/meanings if available
                val mergedIpa = curated.ipa ?: viData?.ipa ?: enData?.ipa
                val mergedAudio = enData?.audioUrl ?: curated.audioUrl
                val mergedVi = if (viData != null && viData.meaningGroups.isNotEmpty()) {
                    viData.meaningGroups
                } else {
                    curated.vietnameseMeanings
                }
                val mergedEn = if (enData != null && enData.meaningGroups.isNotEmpty()) {
                    enData.meaningGroups
                } else {
                    curated.englishMeanings
                }
                val synonyms = (curated.synonyms + viData?.synonyms.orEmpty() + enData?.synonyms.orEmpty()).distinct()
                val antonyms = (curated.antonyms + viData?.antonyms.orEmpty() + enData?.antonyms.orEmpty()).distinct()

                Result.success(
                    curated.copy(
                        ipa = mergedIpa,
                        audioUrl = mergedAudio,
                        vietnameseMeanings = mergedVi,
                        englishMeanings = mergedEn,
                        synonyms = synonyms,
                        antonyms = antonyms,
                        isVietnameseAvailable = true,
                        isEnglishAvailable = true
                    )
                )
            } else if (viData == null && enData == null) {
                val errorMsg = "Không tìm thấy từ '$trimmed' trong từ điển. Hãy kiểm tra lại chính tả nhé!"
                Result.failure(Exception(errorMsg))
            } else {
                val wordName = viData?.word ?: enData?.word ?: trimmed
                val ipa = viData?.ipa ?: enData?.ipa
                val audioUrl = enData?.audioUrl

                val synonyms = (viData?.synonyms.orEmpty() + enData?.synonyms.orEmpty()).distinct()
                val antonyms = (viData?.antonyms.orEmpty() + enData?.antonyms.orEmpty()).distinct()

                val enExample = enData?.meaningGroups
                    ?.flatMap { it.definitions }
                    ?.firstOrNull { !it.example.isNullOrBlank() }
                    ?.example

                val unifiedResult = DictionaryResult(
                    word = wordName,
                    ipa = ipa,
                    audioUrl = audioUrl,
                    vietnameseMeanings = viData?.meaningGroups.orEmpty(),
                    englishMeanings = enData?.meaningGroups.orEmpty(),
                    synonyms = synonyms,
                    antonyms = antonyms,
                    isVietnameseAvailable = viData != null,
                    isEnglishAvailable = enData != null,
                    primaryExample = enExample,
                    primaryExampleVi = null
                )

                Result.success(unifiedResult)
            }
        }

        // Record successful search in history
        result.onSuccess {
            addSearchHistory(trimmed)
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

    companion object {
        // High quality curated entries for popular & essential vocabulary
        val CURATED_DICTIONARY = mapOf(
            "apple" to DictionaryResult(
                word = "apple",
                ipa = "/ˈæpəl/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/apple-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("quả táo", "cây táo"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A common, round fruit with red, yellow, or green skin and crisp white flesh.", "I like apples.")
                    ))
                ),
                primaryExample = "I like apples.",
                primaryExampleVi = "Tôi thích táo."
            ),
            "house" to DictionaryResult(
                word = "house",
                ipa = "/haʊs/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/house-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("ngôi nhà", "nhà ở", "gia đình"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A building in which people live.", "This is my house.")
                    ))
                ),
                primaryExample = "This is my house.",
                primaryExampleVi = "Đây là ngôi nhà của tôi."
            ),
            "cat" to DictionaryResult(
                word = "cat",
                ipa = "/kæt/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/cat-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("con mèo"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A small domesticated carnivorous mammal with soft fur.", "The cat is sleeping.")
                    ))
                ),
                primaryExample = "The cat is sleeping.",
                primaryExampleVi = "Con mèo đang ngủ."
            ),
            "dog" to DictionaryResult(
                word = "dog",
                ipa = "/dɔːɡ/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/dog-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("con chó", "chú cún"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A domesticated carnivorous mammal that typically has a long snout and barking voice.", "I have a friendly dog.")
                    ))
                ),
                primaryExample = "I have a friendly dog.",
                primaryExampleVi = "Tôi có một chú chó thân thiện."
            ),
            "sun" to DictionaryResult(
                word = "sun",
                ipa = "/sʌn/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/sun-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("mặt trời", "ánh nắng"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("The star round which the earth orbits.", "The sun is shining bright.")
                    ))
                ),
                primaryExample = "The sun is shining bright.",
                primaryExampleVi = "Mặt trời đang chiếu sáng rực rỡ."
            ),
            "tree" to DictionaryResult(
                word = "tree",
                ipa = "/triː/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/tree-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("cái cây", "cây cối"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A woody perennial plant, typically having a single stem or trunk.", "Birds live in the tree.")
                    ))
                ),
                primaryExample = "Birds live in the tree.",
                primaryExampleVi = "Những chú chim sống trên cây."
            ),
            "book" to DictionaryResult(
                word = "book",
                ipa = "/bʊk/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/book-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("quyển sách", "cuốn sách"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("A written or printed work consisting of pages glued or sewn together.", "I read a good book.")
                    ))
                ),
                primaryExample = "I read a good book.",
                primaryExampleVi = "Tôi đọc một cuốn sách hay."
            ),
            "school" to DictionaryResult(
                word = "school",
                ipa = "/skuːl/",
                audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/school-us.mp3",
                vietnameseMeanings = listOf(
                    VietnameseMeaningGroup("Danh từ", listOf("ngôi trường", "trường học"))
                ),
                englishMeanings = listOf(
                    EnglishMeaningGroup("noun", listOf(
                        EnglishDefinition("An institution for educating children.", "I walk to school every day.")
                    ))
                ),
                primaryExample = "I walk to school every day.",
                primaryExampleVi = "Tôi đi bộ đến trường mỗi ngày."
            )
        )
    }
}
