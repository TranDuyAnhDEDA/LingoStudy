package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.data.model.AuthUser
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val authState: AuthState = AuthState.Loading,
    val currentUser: AuthUser? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                val user = if (state is AuthState.Authenticated) state.user else null
                _uiState.update { currentState ->
                    currentState.copy(
                        authState = state,
                        currentUser = user
                    )
                }
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please fill in all fields.", isSuccess = false)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            val result = authRepository.register(email, password, displayName)
            result.fold(
                onSuccess = { user ->
                    val profile = UserProfile(
                        uid = user.uid,
                        displayName = displayName.trim(),
                        email = user.email ?: email.trim(),
                        levelId = "level_beginner_1",
                        levelName = "Beginner 1: Nền tảng & Giao tiếp",
                        xp = 0,
                        streak = 0,
                        longestStreak = 0,
                        totalLessonsCompleted = 0
                    )

                    val profileResult = userRepository.createUserProfile(profile)
                    if (profileResult.isFailure) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = profileResult.exceptionOrNull()?.message
                                    ?: "Failed to initialize user profile.",
                                isSuccess = false
                            )
                        }
                        return@launch
                    }

                    val initialProgress = UserProgress(
                        userId = user.uid,
                        vocabularyProgress = 0,
                        grammarProgress = 0,
                        listeningProgress = 0,
                        pronunciationProgress = 0,
                        currentLessonId = "lesson_b1_1_1",
                        completedLessons = 0,
                        totalLessons = 30
                    )

                    val progressResult = progressRepository.createUserProgress(initialProgress)
                    if (progressResult.isFailure) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = progressResult.exceptionOrNull()?.message
                                    ?: "Failed to initialize user progress.",
                                isSuccess = false
                            )
                        }
                        return@launch
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Registration failed",
                            isSuccess = false
                        )
                    }
                }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter email and password.", isSuccess = false)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Login failed",
                            isSuccess = false
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            it.copy(currentUser = null, isSuccess = false, errorMessage = null)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, userRepository, progressRepository) as T
    }
}
