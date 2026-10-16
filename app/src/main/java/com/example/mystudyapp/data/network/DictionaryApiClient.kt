package com.example.mystudyapp.data.network

import com.example.mystudyapp.data.model.EnglishDefinition
import com.example.mystudyapp.data.model.EnglishMeaningGroup
import com.example.mystudyapp.data.model.VietnameseMeaningGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

open class DictionaryApiClient {

    open suspend fun lookupVietnameseDictionary(word: String): Result<VietnameseDataDTO> = withContext(Dispatchers.IO) {
        val trimmedWord = word.trim()
        if (trimmedWord.isBlank()) return@withContext Result.failure(Exception("Search term cannot be empty."))

        return@withContext try {
            val encodedWord = URLEncoder.encode(trimmedWord, "UTF-8")
            val urlString = "https://dict.minhqnd.com/api/v1/lookup?word=$encodedWord&lang=en&def_lang=vi"
            val jsonString = executeHttpGet(urlString)
            val parsed = parseVietnameseResponse(jsonString, trimmedWord)
            if (parsed != null && (parsed.meaningGroups.isNotEmpty() || parsed.ipa != null)) {
                Result.success(parsed)
            } else {
                Result.failure(Exception("Word not found in Vietnamese dictionary."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun lookupEnglishDictionary(word: String): Result<EnglishDataDTO> = withContext(Dispatchers.IO) {
        val trimmedWord = word.trim()
        if (trimmedWord.isBlank()) return@withContext Result.failure(Exception("Search term cannot be empty."))

        return@withContext try {
            val encodedWord = URLEncoder.encode(trimmedWord, "UTF-8")
            val urlString = "https://api.dictionaryapi.dev/api/v2/entries/en/$encodedWord"
            val jsonString = executeHttpGet(urlString)
            val parsed = parseEnglishResponse(jsonString, trimmedWord)
            if (parsed != null && (parsed.meaningGroups.isNotEmpty() || parsed.ipa != null)) {
                Result.success(parsed)
            } else {
                Result.failure(Exception("Word not found in English dictionary."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected open fun executeHttpGet(urlString: String): String {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "LingoStudy-Android-App")
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            ?: throw Exception("HTTP Error: $responseCode")

        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()

        if (responseCode !in 200..299) {
            throw Exception("HTTP $responseCode: ${response.toString().take(100)}")
        }

        return response.toString()
    }

    fun parseVietnameseResponse(jsonString: String, rawWord: String): VietnameseDataDTO? {
        if (!jsonString.contains("\"exists\":true", ignoreCase = true) && !jsonString.contains("\"exists\": true", ignoreCase = true)) {
            return null
        }

        val ipaMatch = Regex("\"ipa\"\\s*:\\s*\"([^\"]+)\"").find(jsonString)
        val ipa = ipaMatch?.groupValues?.get(1)?.ifBlank { null }

        val meaningsMap = mutableMapOf<String, MutableList<String>>()
        val meaningMatches = Regex("\"pos\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"definition\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString)
        for (m in meaningMatches) {
            val pos = m.groupValues[1].ifBlank { "Khác" }
            val def = m.groupValues[2].replace("\\\"", "\"").replace("\\n", " ").trim()
            if (def.isNotBlank()) {
                meaningsMap.getOrPut(pos) { mutableListOf() }.add(def)
            }
        }

        if (meaningsMap.isEmpty()) {
            val defMatches = Regex("\"definition\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString)
            val posMatches = Regex("\"pos\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString).toList()
            val posList = posMatches.map { it.groupValues[1] }

            defMatches.forEachIndexed { index, matchResult ->
                val def = matchResult.groupValues[1].replace("\\\"", "\"").replace("\\n", " ").trim()
                val pos = posList.getOrNull(index) ?: "Nghĩa"
                if (def.isNotBlank()) {
                    meaningsMap.getOrPut(pos) { mutableListOf() }.add(def)
                }
            }
        }

        val synonyms = mutableSetOf<String>()
        val antonyms = mutableSetOf<String>()

        val relMatches = Regex("\"related_word\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"relation_type\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString)
        for (r in relMatches) {
            val word = r.groupValues[1].trim()
            val type = r.groupValues[2].trim()
            if (word.isNotBlank()) {
                if (type.contains("Đồng", ignoreCase = true) || type.contains("Synonym", ignoreCase = true)) {
                    synonyms.add(word)
                } else if (type.contains("Trái", ignoreCase = true) || type.contains("Antonym", ignoreCase = true)) {
                    antonyms.add(word)
                }
            }
        }

        val meaningGroups = meaningsMap.map { (pos, defs) ->
            VietnameseMeaningGroup(partOfSpeech = pos, definitions = defs.distinct())
        }

        return VietnameseDataDTO(
            word = rawWord,
            ipa = ipa,
            meaningGroups = meaningGroups,
            synonyms = synonyms.toList(),
            antonyms = antonyms.toList()
        )
    }

    fun parseEnglishResponse(jsonString: String, rawWord: String): EnglishDataDTO? {
        if (!jsonString.startsWith("[") && !jsonString.contains("\"word\":", ignoreCase = true)) {
            return null
        }

        val audioMatch = Regex("\"audio\"\\s*:\\s*\"(https?://[^\"]+)\"").find(jsonString)
        val audioUrl = audioMatch?.groupValues?.get(1)?.ifBlank { null }

        val ipaMatch = Regex("\"text\"\\s*:\\s*\"([^\"]+)\"").find(jsonString)
            ?: Regex("\"phonetic\"\\s*:\\s*\"([^\"]+)\"").find(jsonString)
        val ipa = ipaMatch?.groupValues?.get(1)?.ifBlank { null }

        val meaningsList = mutableListOf<EnglishMeaningGroup>()
        val posMatches = Regex("\"partOfSpeech\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString).toList()

        val defMatches = Regex("\"definition\"\\s*:\\s*\"([^\"]+)\"").findAll(jsonString).toList()
        val exampleMatch = Regex("\"example\"\\s*:\\s*\"([^\"]+)\"").find(jsonString)
        val exampleText = exampleMatch?.groupValues?.get(1)?.replace("\\\"", "\"")?.ifBlank { null }

        if (posMatches.isNotEmpty() && defMatches.isNotEmpty()) {
            val primaryPos = posMatches[0].groupValues[1]
            val defsList = defMatches.map { m ->
                EnglishDefinition(definition = m.groupValues[1].replace("\\\"", "\"").trim(), example = exampleText)
            }.distinct()

            meaningsList.add(EnglishMeaningGroup(partOfSpeech = primaryPos, definitions = defsList))
        }

        val synonyms = Regex("\"synonyms\"\\s*:\\s*\\[([^\\]]+)\\]").findAll(jsonString)
            .flatMap { it.groupValues[1].split(",") }
            .map { it.replace("\"", "").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        val antonyms = Regex("\"antonyms\"\\s*:\\s*\\[([^\\]]+)\\]").findAll(jsonString)
            .flatMap { it.groupValues[1].split(",") }
            .map { it.replace("\"", "").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        return EnglishDataDTO(
            word = rawWord,
            ipa = ipa,
            audioUrl = audioUrl,
            meaningGroups = meaningsList,
            synonyms = synonyms,
            antonyms = antonyms
        )
    }
}

data class VietnameseDataDTO(
    val word: String,
    val ipa: String?,
    val meaningGroups: List<VietnameseMeaningGroup>,
    val synonyms: List<String>,
    val antonyms: List<String>
)

data class EnglishDataDTO(
    val word: String,
    val ipa: String?,
    val audioUrl: String?,
    val meaningGroups: List<EnglishMeaningGroup>,
    val synonyms: List<String>,
    val antonyms: List<String>
)
