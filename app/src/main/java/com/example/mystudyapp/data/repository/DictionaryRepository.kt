package com.example.mystudyapp.data.repository

import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.data.network.DictionaryApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface DictionaryRepository {
    fun lookupWord(word: String): Flow<Result<DictionaryResult>>
}

class DictionaryRepositoryImpl(
    private val apiClient: DictionaryApiClient = DictionaryApiClient()
) : DictionaryRepository {

    override fun lookupWord(word: String): Flow<Result<DictionaryResult>> = flow {
        val trimmed = word.trim()
        if (trimmed.isBlank()) {
            emit(Result.failure(Exception("Search query cannot be empty.")))
            return@flow
        }

        val result = coroutineScope {
            val viDeferred = async { apiClient.lookupVietnameseDictionary(trimmed) }
            val enDeferred = async { apiClient.lookupEnglishDictionary(trimmed) }

            val viResult = viDeferred.await()
            val enResult = enDeferred.await()

            val viData = viResult.getOrNull()
            val enData = enResult.getOrNull()

            if (viData == null && enData == null) {
                val errorMsg = viResult.exceptionOrNull()?.message
                    ?: enResult.exceptionOrNull()?.message
                    ?: "No dictionary definitions found for '$trimmed'."
                Result.failure(Exception(errorMsg))
            } else {
                val wordName = viData?.word ?: enData?.word ?: trimmed
                val ipa = viData?.ipa ?: enData?.ipa
                val audioUrl = enData?.audioUrl

                val synonyms = (viData?.synonyms.orEmpty() + enData?.synonyms.orEmpty()).distinct()
                val antonyms = (viData?.antonyms.orEmpty() + enData?.antonyms.orEmpty()).distinct()

                val unifiedResult = DictionaryResult(
                    word = wordName,
                    ipa = ipa,
                    audioUrl = audioUrl,
                    vietnameseMeanings = viData?.meaningGroups.orEmpty(),
                    englishMeanings = enData?.meaningGroups.orEmpty(),
                    synonyms = synonyms,
                    antonyms = antonyms,
                    isVietnameseAvailable = viData != null,
                    isEnglishAvailable = enData != null
                )

                Result.success(unifiedResult)
            }
        }

        emit(result)
    }.flowOn(Dispatchers.IO)
}
