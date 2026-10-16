package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Quiz
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import com.example.mystudyapp.data.seed.BeginnerCurriculumData
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
    val examplesVi: List<String> = emptyList(),
    val order: Int = 0
)

data class GrammarUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val grammarList: List<GrammarUiModel> = emptyList(),
    val isReadingFinished: Boolean = false,
    val showQuickQuiz: Boolean = false,
    val quickQuizQuestions: List<Quiz> = emptyList(),
    val currentQuizIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(),
    val isQuizSubmitted: Boolean = false,
    val quizScore: Int = 0,
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
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid lesson ID.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
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

                val quickQuizzes = BeginnerCurriculumData.sampleGrammarQuickQuizzes

                val uiModels = sortedGrammar.map { g ->
                    GrammarUiModel(
                        id = g.id,
                        title = g.titleVi ?: g.title,
                        explanation = g.explanationVi ?: g.explanation,
                        examples = g.examples,
                        examplesVi = g.examplesVi ?: emptyList(),
                        order = g.order
                    )
                }

                val isAlreadyFinished = (lessonProgress?.grammarCompleted == true)

                _uiState.value = GrammarUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson?.titleVi ?: lesson?.title ?: "Ngữ pháp bài học",
                    grammarList = uiModels,
                    quickQuizQuestions = quickQuizzes,
                    isReadingFinished = false,
                    isFinished = false
                )
            }
        }
    }

    fun onFinishReadingContent() {
        _uiState.update {
            it.copy(
                isReadingFinished = true,
                showQuickQuiz = true,
                currentQuizIndex = 0,
                selectedAnswers = emptyMap(),
                isQuizSubmitted = false
            )
        }
    }

    fun selectQuizAnswer(questionIndex: Int, answer: String) {
        _uiState.update { currentState ->
            val updated = currentState.selectedAnswers.toMutableMap()
            updated[questionIndex] = answer
            currentState.copy(selectedAnswers = updated)
        }
    }

    fun nextQuizQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuizIndex < currentState.quickQuizQuestions.size - 1) {
            _uiState.update { it.copy(currentQuizIndex = it.currentQuizIndex + 1) }
        }
    }

    fun prevQuizQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuizIndex > 0) {
            _uiState.update { it.copy(currentQuizIndex = it.currentQuizIndex - 1) }
        }
    }

    fun submitQuickQuiz() {
        val currentState = _uiState.value
        val questions = currentState.quickQuizQuestions
        var score = 0
        questions.forEachIndexed { index, quiz ->
            val selected = currentState.selectedAnswers[index]
            if (selected == quiz.correctAnswer) {
                score++
            }
        }

        val currentUser = authRepository.getCurrentUser()
        val uid = currentUser?.uid

        viewModelScope.launch {
            if (uid != null) {
                val currentProgressRes = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()
                val currentProgress = currentProgressRes?.getOrNull()
                val vocabDone = currentProgress?.vocabularyCompleted == true

                val updatedProgress = LessonProgress(
                    lessonId = lessonId,
                    progress = if (vocabDone) 50 else 25,
                    completed = false,
                    vocabularyCompleted = vocabDone,
                    grammarCompleted = true,
                    lastGrammarIndex = currentState.grammarList.size
                )

                val saveResult = progressRepository.updateLessonProgress(uid, updatedProgress)
                if (saveResult.isSuccess) {
                    progressRepository.completeLessonIfEligible(uid, lessonId)
                    _uiState.update {
                        it.copy(
                            isQuizSubmitted = true,
                            quizScore = score,
                            isFinished = true,
                            showQuickQuiz = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = saveResult.exceptionOrNull()?.message ?: "Lỗi lưu tiến độ ngữ pháp."
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isQuizSubmitted = true,
                        quizScore = score,
                        isFinished = true,
                        showQuickQuiz = false
                    )
                }
            }
        }
    }

    fun retakeQuickQuiz() {
        _uiState.update {
            it.copy(
                showQuickQuiz = true,
                currentQuizIndex = 0,
                selectedAnswers = emptyMap(),
                isQuizSubmitted = false,
                isFinished = false
            )
        }
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
