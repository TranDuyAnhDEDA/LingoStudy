package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VocabularyUiModel(
    val id: String,
    val word: String,
    val meaning: String,
    val meaningVi: String? = null,
    val partOfSpeech: String = "",
    val example: String = "",
    val exampleVi: String? = null,
    val translation: String = "",
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val phonetic: String = ""
)

data class VocabularyUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val unitLabel: String = "UNIT 1",
    val vocabularyList: List<VocabularyUiModel> = emptyList(),
    val currentIndex: Int = 0,
    val completedCount: Int = 0,
    val isFinished: Boolean = false
)

class VocabularyViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState(lessonId = lessonId))
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        loadVocabularyData()
    }

    fun loadVocabularyData() {
        if (lessonId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid lesson ID."
                )
            }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = "User is not authenticated. Please log in."
                        )
                    }
                    return@collect
                }

                val uid = user.uid
                _uiState.update { it.copy(isLoading = true, errorMessage = null, isAuthenticated = true) }

                val lessonResult = learningRepository.getLesson(lessonId).firstOrNull()
                val lesson = lessonResult?.getOrNull()

                val unitResult = if (lesson?.unitId.isNullOrBlank()) null else learningRepository.getUnit(lesson.unitId).firstOrNull()
                val unit = unitResult?.getOrNull()
                val unitLabel = if (unit != null) {
                    if (unit.order > 0) "UNIT ${unit.order}" else unit.title.substringBefore(":").trim().ifBlank { "UNIT 1" }
                } else {
                    val uId = lesson?.unitId ?: ""
                    val match = Regex("unit_.*_?(\\d+)").find(uId)
                    if (match != null) "UNIT ${match.groupValues[1]}" else "UNIT 1"
                }

                val vocabResult = learningRepository.getVocabularyByLesson(lessonId).firstOrNull()
                val vocabList = vocabResult?.getOrNull() ?: emptyList()

                val lessonProgressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val lessonProgress = lessonProgressResult?.getOrNull()

                if (vocabList.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Từ vựng bài học",
                            unitLabel = unitLabel,
                            vocabularyList = emptyList(),
                            errorMessage = null
                        )
                    }
                    return@collect
                }

                val uiModels = vocabList.map { v ->
                    val phonetic = if (!v.imageUrl.isNullOrBlank() && v.imageUrl.startsWith("/")) {
                        v.imageUrl
                    } else {
                        "/${v.word.lowercase()}/"
                    }
                    VocabularyUiModel(
                        id = v.id,
                        word = v.word,
                        meaning = v.meaning,
                        meaningVi = v.meaningVi,
                        partOfSpeech = v.partOfSpeech,
                        example = v.example,
                        exampleVi = v.exampleVi,
                        translation = v.translation,
                        audioUrl = v.audioUrl,
                        imageUrl = v.imageUrl,
                        phonetic = phonetic
                    )
                }

                val lastIndex = lessonProgress?.lastVocabularyIndex ?: 0
                val isAlreadyFinished = (lessonProgress?.vocabularyCompleted == true)
                val startingIndex = if (lastIndex in uiModels.indices && !isAlreadyFinished) {
                    lastIndex
                } else {
                    0
                }

                _uiState.value = VocabularyUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Từ vựng bài học",
                    unitLabel = unitLabel,
                    vocabularyList = uiModels,
                    currentIndex = startingIndex,
                    completedCount = if (isAlreadyFinished) uiModels.size else startingIndex,
                    isFinished = false
                )
            }
        }
    }

    fun previousVocabulary() {
        val currentState = _uiState.value
        if (currentState.currentIndex > 0) {
            val prevIndex = currentState.currentIndex - 1
            _uiState.update {
                it.copy(
                    currentIndex = prevIndex
                )
            }
            val currentUser = authRepository.getCurrentUser()
            val uid = currentUser?.uid
            if (uid != null) {
                viewModelScope.launch {
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(vocabularyCompleted = currentState.completedCount >= currentState.vocabularyList.size),
                        completed = false,
                        vocabularyCompleted = currentState.completedCount >= currentState.vocabularyList.size,
                        lastVocabularyIndex = prevIndex
                    )
                    progressRepository.updateLessonProgress(uid, updatedProgress)
                }
            }
        }
    }

    fun nextVocabulary() {
        val currentState = _uiState.value
        val list = currentState.vocabularyList
        if (list.isEmpty()) return

        val nextIndex = currentState.currentIndex + 1
        val newCompletedCount = (currentState.completedCount + 1).coerceAtMost(list.size)

        val currentUser = authRepository.getCurrentUser()
        val uid = currentUser?.uid

        viewModelScope.launch {
            if (nextIndex < list.size) {
                _uiState.update {
                    it.copy(
                        currentIndex = nextIndex,
                        completedCount = newCompletedCount
                    )
                }
                if (uid != null) {
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(vocabularyCompleted = false),
                        completed = false,
                        vocabularyCompleted = false,
                        lastVocabularyIndex = nextIndex
                    )
                    progressRepository.updateLessonProgress(uid, updatedProgress)
                }
            } else {
                if (uid != null) {
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(vocabularyCompleted = true),
                        completed = false,
                        vocabularyCompleted = true,
                        lastVocabularyIndex = list.size
                    )
                    val saveResult = progressRepository.updateLessonProgress(uid, updatedProgress)
                    if (saveResult.isSuccess) {
                        progressRepository.completeLessonIfEligible(uid, lessonId)
                        _uiState.update {
                            it.copy(
                                currentIndex = list.size - 1,
                                completedCount = list.size,
                                isFinished = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                errorMessage = saveResult.exceptionOrNull()?.message ?: "Failed to save progress."
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentIndex = list.size - 1,
                            completedCount = list.size,
                            isFinished = true
                        )
                    }
                }
            }
        }
    }

    fun restartReview() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                isFinished = false
            )
        }
    }

    private fun calculateLessonProgressPct(vocabularyCompleted: Boolean): Int {
        return if (vocabularyCompleted) 20 else 10
    }
}

class VocabularyViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VocabularyViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
