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

data class GrammarUiModel(
    val id: String,
    val title: String,
    val explanation: String,
    val examples: List<String> = emptyList(),
    val order: Int = 0
)

data class GrammarUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val grammarList: List<GrammarUiModel> = emptyList(),
    val currentIndex: Int = 0,
    val completedCount: Int = 0,
    val isFinished: Boolean = false
)

class GrammarViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarUiState(lessonId = lessonId))
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    init {
        loadGrammarData()
    }

    fun loadGrammarData() {
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

                val grammarResult = learningRepository.getGrammarByLesson(lessonId).firstOrNull()
                val rawGrammarList = grammarResult?.getOrNull() ?: emptyList()

                val sortedGrammar = rawGrammarList.sortedBy { it.order }

                val lessonProgressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val lessonProgress = lessonProgressResult?.getOrNull()

                if (sortedGrammar.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lessonTitle = lesson?.title ?: "Grammar Lesson",
                            grammarList = emptyList(),
                            errorMessage = null
                        )
                    }
                    return@collect
                }

                val uiModels = sortedGrammar.map { g ->
                    GrammarUiModel(
                        id = g.id,
                        title = g.title,
                        explanation = g.explanation,
                        examples = g.examples,
                        order = g.order
                    )
                }

                val lastIndex = lessonProgress?.lastGrammarIndex ?: 0
                val startingIndex = if (lastIndex in uiModels.indices && !(lessonProgress?.grammarCompleted == true)) {
                    lastIndex
                } else {
                    0
                }

                val isAlreadyFinished = (lessonProgress?.grammarCompleted == true)
                val completedCount = if (isAlreadyFinished) uiModels.size else startingIndex

                _uiState.value = GrammarUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.title ?: "Grammar Lesson",
                    grammarList = uiModels,
                    currentIndex = if (isAlreadyFinished) uiModels.size - 1 else startingIndex,
                    completedCount = completedCount,
                    isFinished = isAlreadyFinished
                )
            }
        }
    }

    fun nextGrammar() {
        val currentState = _uiState.value
        val list = currentState.grammarList
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
                    val currentVocabCompleted = isVocabCompleted(uid)
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(vocabDone = currentVocabCompleted, grammarDone = false),
                        completed = false,
                        vocabularyCompleted = currentVocabCompleted,
                        grammarCompleted = false,
                        lastGrammarIndex = nextIndex
                    )
                    progressRepository.updateLessonProgress(uid, updatedProgress)
                }
            } else {
                if (uid != null) {
                    val currentVocabCompleted = isVocabCompleted(uid)
                    val updatedProgress = LessonProgress(
                        lessonId = lessonId,
                        progress = calculateLessonProgressPct(vocabDone = currentVocabCompleted, grammarDone = true),
                        completed = false,
                        vocabularyCompleted = currentVocabCompleted,
                        grammarCompleted = true,
                        lastGrammarIndex = list.size
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

    private suspend fun isVocabCompleted(uid: String): Boolean {
        val progress = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()?.getOrNull()
        return progress?.vocabularyCompleted == true
    }

    private fun calculateLessonProgressPct(vocabDone: Boolean, grammarDone: Boolean): Int {
        var pct = 0
        if (vocabDone) pct += 20
        if (grammarDone) pct += 20
        return pct
    }
}

class GrammarViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GrammarViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
