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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class LessonUiModel(
    val lessonId: String,
    val title: String,
    val description: String,
    val order: Int,
    val estimatedMinutes: Int,
    val xpReward: Int,
    val progress: Int,
    val completed: Boolean,
    val isCurrentLesson: Boolean
)

data class UnitUiModel(
    val unitId: String,
    val title: String,
    val description: String,
    val order: Int,
    val imageUrl: String? = null,
    val lessons: List<LessonUiModel> = emptyList()
)

data class LearnUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val displayName: String = "",
    val levelName: String = "",
    val currentXp: Int = 0,
    val requiredXp: Int = 0,
    val streak: Int = 0,
    val currentLevelId: String = "",
    val units: List<UnitUiModel> = emptyList(),
    val vocabularyProgress: Int = 0,
    val grammarProgress: Int = 0,
    val listeningProgress: Int = 0,
    val pronunciationProgress: Int = 0
)

class LearnViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    init {
        loadLearnData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadLearnData() {
        viewModelScope.launch {
            authRepository.observeAuthState().flatMapLatest { user ->
                if (user == null) {
                    flowOf(
                        LearnUiState(
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
                                ?: "Failed to load user learning data."
                            flowOf(LearnUiState(isLoading = false, errorMessage = errorMsg))
                        } else {
                            val profile = profileResult.getOrNull()
                            val userProgress = progressResult.getOrNull()

                            if (profile == null || userProgress == null) {
                                flowOf(LearnUiState(isLoading = false, errorMessage = "User profile or progress document not found."))
                            } else {
                                val levelId = profile.levelId.ifBlank { "level_3" }
                                val currentLessonId = userProgress.currentLessonId ?: "lesson_3_1_1"

                                val levelFlow = learningRepository.getLevel(levelId)
                                val unitsFlow = learningRepository.getUnitsByLevel(levelId)

                                combine(levelFlow, unitsFlow) { levelResult, unitsResult ->
                                    val level = levelResult.getOrNull()
                                    val unitsList = unitsResult.getOrNull() ?: emptyList()
                                    val sortedUnits = unitsList.sortedBy { it.order }

                                    val unitUiModels = sortedUnits.map { unit ->
                                        val lessonsResult = learningRepository.getLessonsByUnit(unit.id).firstOrNull()
                                        val lessonsList = lessonsResult?.getOrNull() ?: emptyList()
                                        val sortedLessons = lessonsList.sortedBy { it.order }

                                        val lessonUiModels = sortedLessons.map { lesson ->
                                            val lessonProgressResult = progressRepository.getLessonProgress(uid, lesson.id).firstOrNull()
                                            val lessonProgress = lessonProgressResult?.getOrNull()

                                            LessonUiModel(
                                                lessonId = lesson.id,
                                                title = lesson.title,
                                                description = lesson.description,
                                                order = lesson.order,
                                                estimatedMinutes = lesson.estimatedMinutes,
                                                xpReward = lesson.xpReward,
                                                progress = lessonProgress?.progress ?: 0,
                                                completed = lessonProgress?.completed ?: false,
                                                isCurrentLesson = (lesson.id == currentLessonId)
                                            )
                                        }

                                        UnitUiModel(
                                            unitId = unit.id,
                                            title = unit.title,
                                            description = unit.description,
                                            order = unit.order,
                                            imageUrl = unit.imageUrl,
                                            lessons = lessonUiModels
                                        )
                                    }

                                    LearnUiState(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        errorMessage = null,
                                        displayName = profile.displayName.ifBlank { "Learner" },
                                        levelName = profile.levelName.ifBlank { level?.name ?: "Explorer" },
                                        currentXp = profile.xp,
                                        requiredXp = level?.xpRequired ?: 600,
                                        streak = profile.streak,
                                        currentLevelId = levelId,
                                        units = unitUiModels,
                                        vocabularyProgress = userProgress.vocabularyProgress,
                                        grammarProgress = userProgress.grammarProgress,
                                        listeningProgress = userProgress.listeningProgress,
                                        pronunciationProgress = userProgress.pronunciationProgress
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

class LearnViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LearnViewModel(
            authRepository,
            userRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
