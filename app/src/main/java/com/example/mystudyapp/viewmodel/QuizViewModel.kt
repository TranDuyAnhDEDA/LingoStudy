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

data class QuizUiModel(
    val id: String,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val xpReward: Int = 10
)

data class QuizUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val quizList: List<QuizUiModel> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: String? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false,
    val currentScore: Int = 0,
    val isFinished: Boolean = false
)

class QuizViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState(lessonId = lessonId))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuizData()
    }

    fun loadQuizData() {
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

                val quizResult = learningRepository.getQuizzesByLesson(lessonId).firstOrNull()
                val rawQuizList = quizResult?.getOrNull() ?: emptyList()

                val lessonProgressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val lessonProgress = lessonProgressResult?.getOrNull()

                if (rawQuizList.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Bài kiểm tra",
                            quizList = emptyList(),
                            errorMessage = null
                        )
                    }
                    return@collect
                }

                val uiModels = rawQuizList.map { q ->
                    QuizUiModel(
                        id = q.id,
                        question = q.question,
                        options = q.options,
                        correctAnswer = q.correctAnswer,
                        xpReward = q.xpReward
                    )
                }

                val lastIndex = lessonProgress?.lastQuizIndex ?: 0
                val startingScore = lessonProgress?.quizScore ?: 0
                val isAlreadyFinished = (lessonProgress?.quizCompleted == true)
                val startingIndex = if (lastIndex in uiModels.indices && !isAlreadyFinished) {
                    lastIndex
                } else {
                    0
                }

                _uiState.value = QuizUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Bài kiểm tra",
                    quizList = uiModels,
                    currentIndex = startingIndex,
                    currentScore = if (!isAlreadyFinished) startingScore else 0,
                    isFinished = false
                )
            }
        }
    }

    fun selectOption(option: String) {
        if (_uiState.value.isSubmitted) return
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        val selected = currentState.selectedOption ?: return
        if (currentState.isSubmitted) return

        val currentQuiz = currentState.quizList.getOrNull(currentState.currentIndex) ?: return
        val isCorrect = (selected.trim() == currentQuiz.correctAnswer.trim())
        val newScore = if (isCorrect) currentState.currentScore + 1 else currentState.currentScore

        _uiState.update {
            it.copy(
                isSubmitted = true,
                isCorrect = isCorrect,
                currentScore = newScore
            )
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val list = currentState.quizList
        if (list.isEmpty()) return

        val nextIndex = currentState.currentIndex + 1
        val currentUser = authRepository.getCurrentUser()
        val uid = currentUser?.uid

        viewModelScope.launch {
            if (nextIndex < list.size) {
                _uiState.update {
                    it.copy(
                        currentIndex = nextIndex,
                        selectedOption = null,
                        isSubmitted = false,
                        isCorrect = false
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
                            pronunDone = progressObj?.pronunciationCompleted == true,
                            quizDone = false
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = progressObj?.listeningCompleted == true,
                        pronunciationCompleted = progressObj?.pronunciationCompleted == true,
                        quizCompleted = false,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = progressObj?.lastListeningIndex ?: 0,
                        lastPronunciationIndex = progressObj?.lastPronunciationIndex ?: 0,
                        lastQuizIndex = nextIndex,
                        quizScore = currentState.currentScore
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
                            pronunDone = progressObj?.pronunciationCompleted == true,
                            quizDone = true
                        ),
                        completed = false,
                        vocabularyCompleted = progressObj?.vocabularyCompleted == true,
                        grammarCompleted = progressObj?.grammarCompleted == true,
                        listeningCompleted = progressObj?.listeningCompleted == true,
                        pronunciationCompleted = progressObj?.pronunciationCompleted == true,
                        quizCompleted = true,
                        lastVocabularyIndex = progressObj?.lastVocabularyIndex ?: 0,
                        lastGrammarIndex = progressObj?.lastGrammarIndex ?: 0,
                        lastListeningIndex = progressObj?.lastListeningIndex ?: 0,
                        lastPronunciationIndex = progressObj?.lastPronunciationIndex ?: 0,
                        lastQuizIndex = list.size,
                        quizScore = currentState.currentScore
                    )
                    val saveResult = progressRepository.updateLessonProgress(uid, updatedProgress)
                    if (saveResult.isSuccess) {
                        progressRepository.completeLessonIfEligible(uid, lessonId)
                        _uiState.update {
                            it.copy(
                                currentIndex = list.size - 1,
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
                            isFinished = true
                        )
                    }
                }
            }
        }
    }

    fun retakeQuiz() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedOption = null,
                isSubmitted = false,
                isCorrect = false,
                currentScore = 0,
                isFinished = false
            )
        }
    }

    private suspend fun getExistingProgress(uid: String): LessonProgress? {
        return progressRepository.getLessonProgress(uid, lessonId).firstOrNull()?.getOrNull()
    }

    private fun calculateLessonProgressPct(
        vocabDone: Boolean,
        grammarDone: Boolean,
        listeningDone: Boolean,
        pronunDone: Boolean,
        quizDone: Boolean
    ): Int {
        var pct = 0
        if (vocabDone) pct += 20
        if (grammarDone) pct += 20
        if (listeningDone) pct += 20
        if (pronunDone) pct += 20
        if (quizDone) pct += 20
        return pct.coerceAtMost(100)
    }
}

class QuizViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuizViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
