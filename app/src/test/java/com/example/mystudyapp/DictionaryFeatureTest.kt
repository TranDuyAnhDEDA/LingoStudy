package com.example.mystudyapp

import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.data.network.DictionaryApiClient
import com.example.mystudyapp.data.network.EnglishDataDTO
import com.example.mystudyapp.data.network.VietnameseDataDTO
import com.example.mystudyapp.data.repository.DictionaryRepository
import com.example.mystudyapp.data.repository.DictionaryRepositoryImpl
import com.example.mystudyapp.viewmodel.DictionaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryFeatureTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testVietnameseResponseParsing() {
        val json = """
            {
                "exists": true,
                "word": "hello",
                "results": [
                    {
                        "lang_code": "en",
                        "pronunciations": [{"ipa": "/həˈloʊ/"}],
                        "meanings": [
                            {"pos": "Thán từ", "definition": "Chào anh!"},
                            {"pos": "Danh từ", "definition": "Tiếng chào."}
                        ],
                        "relations": [
                            {"related_word": "hi", "relation_type": "Đồng nghĩa"}
                        ]
                    }
                ]
            }
        """.trimIndent()

        val client = DictionaryApiClient()
        val parsed = client.parseVietnameseResponse(json, "hello")

        assertNotNull(parsed)
        assertEquals("hello", parsed?.word)
        assertEquals("/həˈloʊ/", parsed?.ipa)
        assertEquals(2, parsed?.meaningGroups?.size)
        assertEquals("Thán từ", parsed?.meaningGroups?.get(0)?.partOfSpeech)
        assertEquals("Chào anh!", parsed?.meaningGroups?.get(0)?.definitions?.get(0))
        assertTrue(parsed?.synonyms?.contains("hi") == true)
    }

    @Test
    fun testEnglishResponseParsing() {
        val json = """
            [
                {
                    "word": "hello",
                    "phonetic": "/həˈloʊ/",
                    "phonetics": [
                        {"text": "/həˈloʊ/", "audio": "https://api.dictionaryapi.dev/media/hello.mp3"}
                    ],
                    "meanings": [
                        {
                            "partOfSpeech": "interjection",
                            "definitions": [
                                {"definition": "A greeting salutation.", "example": "Hello everyone."}
                            ]
                        }
                    ]
                }
            ]
        """.trimIndent()

        val client = DictionaryApiClient()
        val parsed = client.parseEnglishResponse(json, "hello")

        assertNotNull(parsed)
        assertEquals("hello", parsed?.word)
        assertEquals("/həˈloʊ/", parsed?.ipa)
        assertEquals("https://api.dictionaryapi.dev/media/hello.mp3", parsed?.audioUrl)
        assertEquals(1, parsed?.meaningGroups?.size)
        assertEquals("interjection", parsed?.meaningGroups?.get(0)?.partOfSpeech)
        assertEquals("A greeting salutation.", parsed?.meaningGroups?.get(0)?.definitions?.get(0)?.definition)
        assertEquals("Hello everyone.", parsed?.meaningGroups?.get(0)?.definitions?.get(0)?.example)
    }

    @Test
    fun testRepositoryMergesVietnameseAndEnglishData() {
        val fakeClient = object : DictionaryApiClient() {
            override suspend fun lookupVietnameseDictionary(word: String): Result<VietnameseDataDTO> {
                val json = """
                    {
                        "exists": true,
                        "word": "hello",
                        "results": [{"meanings": [{"pos": "Thán từ", "definition": "Xin chào"}]}]
                    }
                """.trimIndent()
                return Result.success(parseVietnameseResponse(json, word)!!)
            }

            override suspend fun lookupEnglishDictionary(word: String): Result<EnglishDataDTO> {
                val json = """
                    [{"word": "hello", "phonetic": "/həˈloʊ/", "phonetics": [{"audio": "https://media.mp3"}]}]
                """.trimIndent()
                return Result.success(parseEnglishResponse(json, word)!!)
            }
        }

        val repository = DictionaryRepositoryImpl(apiClient = fakeClient)

        testDispatcher.scheduler.advanceUntilIdle()

        var result: DictionaryResult? = null
        val flow = repository.lookupWord("hello")

        kotlinx.coroutines.test.runTest(testDispatcher) {
            flow.collect { res ->
                result = res.getOrNull()
            }
        }

        assertNotNull(result)
        assertEquals("hello", result?.word)
        assertEquals("/həˈloʊ/", result?.ipa)
        assertEquals("https://media.mp3", result?.audioUrl)
        assertTrue(result?.isVietnameseAvailable == true)
        assertTrue(result?.isEnglishAvailable == true)
    }

    @Test
    fun testRepositoryHandlesOneApiFailureGracefully() {
        val fakeClient = object : DictionaryApiClient() {
            override suspend fun lookupVietnameseDictionary(word: String): Result<VietnameseDataDTO> {
                return Result.failure(Exception("Vietnamese API failed"))
            }

            override suspend fun lookupEnglishDictionary(word: String): Result<EnglishDataDTO> {
                val json = """
                    [{"word": "hello", "phonetic": "/həˈloʊ/", "meanings": [{"partOfSpeech": "noun", "definitions": [{"definition": "A greeting"}]}]}]
                """.trimIndent()
                return Result.success(parseEnglishResponse(json, word)!!)
            }
        }

        val repository = DictionaryRepositoryImpl(apiClient = fakeClient)

        var result: DictionaryResult? = null
        kotlinx.coroutines.test.runTest(testDispatcher) {
            repository.lookupWord("hello").collect { res ->
                result = res.getOrNull()
            }
        }

        assertNotNull(result)
        assertFalse(result?.isVietnameseAvailable == true)
        assertTrue(result?.isEnglishAvailable == true)
        assertEquals("A greeting", result?.englishMeanings?.get(0)?.definitions?.get(0)?.definition)
    }

    @Test
    fun testDictionaryViewModelSearchAndClear() {
        val fakeRepository = object : DictionaryRepository {
            override fun lookupWord(word: String): Flow<Result<DictionaryResult>> {
                return flowOf(Result.success(DictionaryResult(word = word, ipa = "/test/")))
            }
        }

        val viewModel = DictionaryViewModel(repository = fakeRepository)

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.result)

        viewModel.onQueryChanged("hello")
        assertEquals("hello", viewModel.uiState.value.searchQuery)

        viewModel.searchWord("hello")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals("hello", state.result?.word)

        viewModel.clearQuery()
        val clearedState = viewModel.uiState.value
        assertEquals("", clearedState.searchQuery)
        assertNull(clearedState.result)
    }
}
