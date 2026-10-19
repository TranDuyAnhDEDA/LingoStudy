package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Listening
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

data class ListeningUiModel(
    val id: String,
    val title: String,
    val audioUrl: String? = null,
    val transcript: String = "",
    val difficulty: String = "",
    val order: Int = 0
)

data class ListeningUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val listeningList: List<ListeningUiModel> = emptyList(),
    val currentIndex: Int = 0,
    val completedCount: Int = 0,
    val isFinished: Boolean = false
)

class ListeningViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListeningUiState(lessonId = lessonId))
    val uiState: StateFlow<ListeningUiState> = _uiState.asStateFlow()

    init {
        loadListeningData()
    }

    fun loadListeningData() {
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

                val listeningResult = learningRepository.getListeningByLesson(lessonId).firstOrNull()
                val rawListeningList = listeningResult?.getOrNull() ?: emptyList()

                val sortedListening = rawListeningList.sortedBy { it.order }

                val lessonProgressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val lessonProgress = lessonProgressResult?.getOrNull()

                if (sortedListening.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Luyện nghe bài học",
                            listeningList = emptyList(),
                            errorMessage = null
                        )
                    }
                    return@collect
                }

                val uiModels = sortedListening.map { l ->
                    ListeningUiModel(
                        id = l.id,
                        title = l.title,
                        audioUrl = l.audioUrl,
                        transcript = l.transcript,
                        difficulty = l.difficulty,
                        order = l.order
                    )
                }

                val lastIndex = lessonProgress?.lastListeningIndex ?: 0
                val isAlreadyFinished = (lessonProgress?.listeningCompleted == true)
                val startingIndex = if (lastIndex in uiModels.indices && !isAlreadyFinished) {
                    lastIndex
                } else {
                    0
                }

                _uiState.value = ListeningUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Luyện nghe bài học",
                    listeningList = uiModels,
                    currentIndex = startingIndex,
                    completedCount = if (isAlreadyFinished) uiModels.size else startingIndex,
                    isFinished = false
                )
            }
        }
    }

    fun nextListening() {
        val currentState = _uiState.value
        val list = currentState.listeningList
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
                            listeningDone = false
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = false,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = nextIndex
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
                            listeningDone = true
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = true,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = list.size
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

    private suspend fun getExistingProgress(uid: String): LessonProgress? {
        return progressRepository.getLessonProgress(uid, lessonId).firstOrNull()?.getOrNull()
    }

    private fun calculateLessonProgressPct(vocabDone: Boolean, grammarDone: Boolean, listeningDone: Boolean): Int {
        var pct = 0
        if (vocabDone) pct += 20
        if (grammarDone) pct += 20
        if (listeningDone) pct += 20
        return pct
    }
}

class ListeningViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListeningViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
