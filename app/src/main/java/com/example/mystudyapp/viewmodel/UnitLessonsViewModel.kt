package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonItemUiModel(
    val lesson: Lesson,
    val progress: LessonProgress? = null
)

data class UnitLessonsUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val unit: Unit? = null,
    val lessons: List<LessonItemUiModel> = emptyList()
)

class UnitLessonsViewModel(
    private val unitId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnitLessonsUiState())
    val uiState: StateFlow<UnitLessonsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadData() {
        if (unitId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid unit ID.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().flatMapLatest { user ->
                if (user == null) {
                    flowOf(UnitLessonsUiState(isLoading = false, isAuthenticated = false))
                } else {
                    val uid = user.uid
                    val unitFlow = learningRepository.getUnit(unitId)
                    val lessonsFlow = learningRepository.getLessonsByUnit(unitId)

                    combine(unitFlow, lessonsFlow) { unitRes, lessonsRes ->
                        val unit = unitRes.getOrNull()
                        val lessonsList = lessonsRes.getOrNull() ?: emptyList()
                        Pair(unit, lessonsList)
                    }.flatMapLatest { (unit, lessonsList) ->
                        if (lessonsList.isEmpty()) {
                            flowOf(
                                UnitLessonsUiState(
                                    isLoading = false,
                                    isAuthenticated = true,
                                    errorMessage = null,
                                    unit = unit,
                                    lessons = emptyList()
                                )
                            )
                        } else {
                            val progressFlows = lessonsList.map { lesson ->
                                progressRepository.getLessonProgress(uid, lesson.id)
                            }

                            combine(progressFlows) { progressResults ->
                                val uiModels = lessonsList.mapIndexed { index, lesson ->
                                    val progress = progressResults.getOrNull(index)?.getOrNull()
                                    LessonItemUiModel(lesson = lesson, progress = progress)
                                }
                                UnitLessonsUiState(
                                    isLoading = false,
                                    isAuthenticated = true,
                                    errorMessage = null,
                                    unit = unit,
                                    lessons = uiModels
                                )
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

class UnitLessonsViewModelFactory(
    private val unitId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UnitLessonsViewModel(
            unitId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
