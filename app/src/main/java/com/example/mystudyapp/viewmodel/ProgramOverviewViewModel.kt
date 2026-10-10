package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.LearningProgram
import com.example.mystudyapp.data.model.Level
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

data class ProgramOverviewUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val program: LearningProgram? = null,
    val levels: List<Level> = emptyList()
)

class ProgramOverviewViewModel(
    private val programId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramOverviewUiState())
    val uiState: StateFlow<ProgramOverviewUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (programId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid program ID.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
                    return@collect
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val programResult = learningRepository.getProgram(programId).firstOrNull()
                val program = programResult?.getOrNull()

                val levelsResult = learningRepository.getLevelsByProgram(programId).firstOrNull()
                val levelsList = levelsResult?.getOrNull() ?: emptyList()

                _uiState.value = ProgramOverviewUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    program = program,
                    levels = levelsList
                )
            }
        }
    }
}

class ProgramOverviewViewModelFactory(
    private val programId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProgramOverviewViewModel(
            programId,
            authRepository,
            learningRepository
        ) as T
    }
}
