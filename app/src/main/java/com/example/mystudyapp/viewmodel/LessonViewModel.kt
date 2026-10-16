package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

data class LessonUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val estimatedMinutes: Int = 0,
    val xpReward: Int = 0,
    val unitId: String = "",
    val progressPercentage: Int = 0,
    val isCompleted: Boolean = false,
    val vocabularyCompleted: Boolean = false,
    val grammarCompleted: Boolean = false,
    val listeningCompleted: Boolean = false,
    val pronunciationCompleted: Boolean = false,
    val quizCompleted: Boolean = false,
    val vocabularyCount: Int = 0,
    val grammarCount: Int = 0,
    val listeningCount: Int = 0,
    val pronunciationCount: Int = 0,
    val quizCount: Int = 0
)

class LessonViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonUiState(lessonId = lessonId))
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        loadLessonData()
    }

    fun loadLessonData() {
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

                if (lesson == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Lesson $lessonId not found."
                        )
                    }
                    return@collect
                }

                progressRepository.completeLessonIfEligible(uid, lessonId)

                val vocabResult = learningRepository.getVocabularyByLesson(lessonId).firstOrNull()
                val grammarResult = learningRepository.getGrammarByLesson(lessonId).firstOrNull()
                val listeningResult = learningRepository.getListeningByLesson(lessonId).firstOrNull()
                val pronunResult = learningRepository.getPronunciationByLesson(lessonId).firstOrNull()
                val quizResult = learningRepository.getQuizzesByLesson(lessonId).firstOrNull()
                val progressResult = progressRepository.getLessonProgress(uid, lessonId).firstOrNull()

                val vocabCount = vocabResult?.getOrNull()?.size ?: 0
                val grammarCount = grammarResult?.getOrNull()?.size ?: 0
                val listeningCount = listeningResult?.getOrNull()?.size ?: 0
                val pronunCount = pronunResult?.getOrNull()?.size ?: 0
                val quizCount = quizResult?.getOrNull()?.size ?: 0
                val lessonProgress = progressResult?.getOrNull()

                val vocabDone = lessonProgress?.vocabularyCompleted == true
                val grammarDone = lessonProgress?.grammarCompleted == true
                val listeningDone = lessonProgress?.listeningCompleted == true
                val pronunDone = lessonProgress?.pronunciationCompleted == true
                val quizDone = lessonProgress?.quizCompleted == true

                var completedModules = 0
                if (vocabDone) completedModules++
                if (grammarDone) completedModules++
                if (listeningDone) completedModules++
                if (quizDone) completedModules++

                val isOverallCompleted = (lessonProgress?.completed == true) || (completedModules == 4)
                val calculatedPct = if (isOverallCompleted) 100 else completedModules * 25

                _uiState.value = LessonUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lesson.id,
                    title = lesson.title,
                    description = lesson.description,
                    order = lesson.order,
                    estimatedMinutes = lesson.estimatedMinutes,
                    xpReward = lesson.xpReward,
                    unitId = lesson.unitId,
                    progressPercentage = calculatedPct,
                    isCompleted = isOverallCompleted,
                    vocabularyCompleted = vocabDone,
                    grammarCompleted = grammarDone,
                    listeningCompleted = listeningDone,
                    pronunciationCompleted = pronunDone,
                    quizCompleted = quizDone,
                    vocabularyCount = vocabCount,
                    grammarCount = grammarCount,
                    listeningCount = listeningCount,
                    pronunciationCount = pronunCount,
                    quizCount = quizCount
                )
            }
        }
    }
}

class LessonViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LessonViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
