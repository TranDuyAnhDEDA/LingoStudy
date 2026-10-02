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

data class PronunciationUiModel(
    val id: String,
    val word: String,
    val phonetic: String = "",
    val audioUrl: String? = null,
    val targetScore: Int = 0
)

data class PronunciationUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val pronunciationList: List<PronunciationUiModel> = emptyList(),
    val currentIndex: Int = 0,
    val completedCount: Int = 0,
    val isFinished: Boolean = false
)

class PronunciationViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PronunciationUiState(lessonId = lessonId))
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    init {
        loadPronunciationData()
    }

    fun loadPronunciationData() {
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

                val pronunResult = learningRepository.getPronunciationByLesson(lessonId).firstOrNull()
                val rawPronunList = pronunResult?.getOrNull() ?: emptyList()

                val lessonProgressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val lessonProgress = lessonProgressResult?.getOrNull()

                if (rawPronunList.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lessonTitle = lesson?.title ?: "Pronunciation Lesson",
                            pronunciationList = emptyList(),
                            errorMessage = null
                        )
                    }
                    return@collect
                }

                val uiModels = rawPronunList.map { p ->
                    PronunciationUiModel(
                        id = p.id,
                        word = p.word,
                        phonetic = p.phonetic,
                        audioUrl = p.audioUrl,
                        targetScore = p.targetScore
                    )
                }

                val lastIndex = lessonProgress?.lastPronunciationIndex ?: 0
                val startingIndex = if (lastIndex in uiModels.indices && !(lessonProgress?.pronunciationCompleted == true)) {
                    lastIndex
                } else {
                    0
                }

                val isAlreadyFinished = (lessonProgress?.pronunciationCompleted == true)
                val completedCount = if (isAlreadyFinished) uiModels.size else startingIndex

                _uiState.value = PronunciationUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.title ?: "Pronunciation Lesson",
                    pronunciationList = uiModels,
                    currentIndex = if (isAlreadyFinished) uiModels.size - 1 else startingIndex,
                    completedCount = completedCount,
                    isFinished = isAlreadyFinished
                )
            }
        }
    }

    fun nextPronunciation() {
        val currentState = _uiState.value
        val list = currentState.pronunciationList
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
                    val progressObj = getExistingProgress(uid)
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(
                            vocabDone = progressObj?.vocabularyCompleted == true,
                            grammarDone = progressObj?.grammarCompleted == true,
                            listeningDone = progressObj?.listeningCompleted == true,
                            pronunDone = false
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = progressObj?.listeningCompleted == true,
                        pronunciationCompleted = false,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = progressObj?.lastListeningIndex ?: 0,
                        lastPronunciationIndex = nextIndex
                    )
                    progressRepository.updateLessonProgress(uid, updatedProgress)
                }
            } else {
                if (uid != null) {
                    val progressObj = getExistingProgress(uid)
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(
                            vocabDone = progressObj?.vocabularyCompleted == true,
                            grammarDone = progressObj?.grammarCompleted == true,
                            listeningDone = progressObj?.listeningCompleted == true,
                            pronunDone = true
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = progressObj?.listeningCompleted == true,
                        pronunciationCompleted = true,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = progressObj?.lastListeningIndex ?: 0,
                        lastPronunciationIndex = list.size
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

    private suspend fun getExistingProgress(uid: String): LessonProgress? {
        return progressRepository.getLessonProgress(uid, lessonId).firstOrNull()?.getOrNull()
    }

    private fun calculateLessonProgressPct(
        vocabDone: Boolean,
        grammarDone: Boolean,
        listeningDone: Boolean,
        pronunDone: Boolean
    ): Int {
        var pct = 0
        if (vocabDone) pct += 20
        if (grammarDone) pct += 20
        if (listeningDone) pct += 20
        if (pronunDone) pct += 20
        return pct
    }
}

class PronunciationViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PronunciationViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
