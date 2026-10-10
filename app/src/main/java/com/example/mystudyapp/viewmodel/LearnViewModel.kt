package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class ProgramUiModel(
    val id: String,
    val title: String,
    val description: String,
    val targetAudience: String,
    val iconEmoji: String,
    val isAvailable: Boolean,
    val levelCount: Int
)

data class LearnUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val programs: List<ProgramUiModel> = emptyList()
)

class LearnViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    init {
        loadPrograms()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadPrograms() {
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
                    learningRepository.getPrograms().flatMapLatest { result ->
                        if (result.isFailure) {
                            flowOf(
                                LearnUiState(
                                    isLoading = false,
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load learning programs."
                                )
                            )
                        } else {
                            val programsList = result.getOrNull() ?: emptyList()
                            val uiModels = programsList.map { p ->
                                ProgramUiModel(
                                    id = p.id,
                                    title = p.title,
                                    description = p.description,
                                    targetAudience = p.targetAudience,
                                    iconEmoji = p.iconEmoji,
                                    isAvailable = p.isAvailable,
                                    levelCount = p.levelIds.size
                                )
                            }

                            flowOf(
                                LearnUiState(
                                    isLoading = false,
                                    isAuthenticated = true,
                                    errorMessage = null,
                                    programs = uiModels
                                )
                            )
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
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LearnViewModel(
            authRepository,
            learningRepository
        ) as T
    }
}
