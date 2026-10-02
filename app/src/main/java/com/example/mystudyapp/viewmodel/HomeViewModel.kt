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
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.data.repository.UserRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val displayName: String = "",
    val streak: Int = 0,
    val levelName: String = "",
    val currentXp: Int = 0,
    val requiredXp: Int = 0,
    val currentLessonId: String = "",
    val currentLessonTitle: String = "",
    val currentLessonDescription: String = "",
    val currentLessonEstimatedMinutes: Int = 0,
    val currentLessonXpReward: Int = 0,
    val vocabularyProgress: Int = 0,
    val grammarProgress: Int = 0,
    val listeningProgress: Int = 0,
    val pronunciationProgress: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0
)

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadHomeData() {
        viewModelScope.launch {
            authRepository.observeAuthState().flatMapLatest { user ->
                if (user == null) {
                    flowOf(
                        HomeUiState(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = "User is not authenticated. Please log in."
                        )
                    )
                } else {
                    val uid = user.uid
                    val profileFlow = userRepository.getUserProfile(uid)
                    val progressFlow = progressRepository.getUserProgress(uid)

                    combine(profileFlow, progressFlow) { profileResult, progressResult ->
                        Pair(profileResult, progressResult)
                    }.flatMapLatest { (profileResult, progressResult) ->
                        if (profileResult.isFailure || progressResult.isFailure) {
                            val errorMsg = profileResult.exceptionOrNull()?.message
                                ?: progressResult.exceptionOrNull()?.message
                                ?: "Failed to load user profile or progress data."
                            flowOf(HomeUiState(isLoading = false, errorMessage = errorMsg))
                        } else {
                            val profile = profileResult.getOrNull()
                            val progress = progressResult.getOrNull()

                            if (profile == null || progress == null) {
                                flowOf(HomeUiState(isLoading = false, errorMessage = "User profile or progress document not found."))
                            } else {
                                val levelId = profile.levelId.ifBlank { "level_3" }
                                val currentLessonId = progress.currentLessonId ?: "lesson_3_1_1"

                                val levelFlow = learningRepository.getLevel(levelId)
                                val lessonFlow = learningRepository.getLesson(currentLessonId)

                                combine(levelFlow, lessonFlow) { levelResult, lessonResult ->
                                    val level = levelResult.getOrNull()
                                    val lesson = lessonResult.getOrNull()

                                    HomeUiState(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        errorMessage = null,
                                        displayName = profile.displayName.ifBlank { "Learner" },
                                        streak = profile.streak,
                                        levelName = profile.levelName.ifBlank { level?.name ?: "Explorer" },
                                        currentXp = profile.xp,
                                        requiredXp = level?.xpRequired ?: 600,
                                        currentLessonId = currentLessonId,
                                        currentLessonTitle = lesson?.title ?: "At the Airport",
                                        currentLessonDescription = lesson?.description ?: "",
                                        currentLessonEstimatedMinutes = lesson?.estimatedMinutes ?: 8,
                                        currentLessonXpReward = lesson?.xpReward ?: 30,
                                        vocabularyProgress = progress.vocabularyProgress,
                                        grammarProgress = progress.grammarProgress,
                                        listeningProgress = progress.listeningProgress,
                                        pronunciationProgress = progress.pronunciationProgress,
                                        completedLessons = progress.completedLessons,
                                        totalLessons = progress.totalLessons
                                    )
                                }
                            }
                        }
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            authRepository,
            userRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
