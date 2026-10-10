package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.Level
import com.example.mystudyapp.data.model.Unit
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LevelUnitsUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val level: Level? = null,
    val units: List<Unit> = emptyList()
)

class LevelUnitsViewModel(
    private val levelId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelUnitsUiState())
    val uiState: StateFlow<LevelUnitsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (levelId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid level ID.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
                    return@collect
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val levelResult = learningRepository.getLevel(levelId).firstOrNull()
                val level = levelResult?.getOrNull()

                val unitsResult = learningRepository.getUnitsByLevel(levelId).firstOrNull()
                val unitsList = unitsResult?.getOrNull() ?: emptyList()

                _uiState.value = LevelUnitsUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    level = level,
                    units = unitsList
                )
            }
        }
    }
}

class LevelUnitsViewModelFactory(
    private val levelId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LevelUnitsViewModel(
            levelId,
            authRepository,
            learningRepository
        ) as T
    }
}
