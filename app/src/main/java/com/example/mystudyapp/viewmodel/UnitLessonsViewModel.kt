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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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

    fun loadData() {
        if (unitId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid unit ID.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
                    return@collect
                }

                val uid = user.uid
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val unitResult = learningRepository.getUnit(unitId).firstOrNull()
                val unit = unitResult?.getOrNull()

                val lessonsResult = learningRepository.getLessonsByUnit(unitId).firstOrNull()
                val lessonsList = lessonsResult?.getOrNull() ?: emptyList()

                val lessonUiModels = lessonsList.map { lesson ->
                    val progressResult = progressRepository.getLessonProgress(uid, lesson.id).firstOrNull()
                    LessonItemUiModel(
                        lesson = lesson,
                        progress = progressResult?.getOrNull()
                    )
                }

                _uiState.value = UnitLessonsUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    unit = unit,
                    lessons = lessonUiModels
                )
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
