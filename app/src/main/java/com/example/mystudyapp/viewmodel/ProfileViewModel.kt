package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DailyChartBar(
    val dayLabel: String,
    val dateString: String,
    val minutes: Int
)

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val profile: UserProfile? = null,
    val progress: UserProgress? = null,
    val isUpdatingName: Boolean = false,
    val currentLevelName: String = "Beginner 1: Nền tảng & Giao tiếp",
    val currentXp: Int = 0,
    val requiredXp: Int = 900,
    val vocabMasteryPct: Int = 0,
    val grammarMasteryPct: Int = 0,
    val listeningMasteryPct: Int = 0,
    val pronunMasteryPct: Int = 0,
    val quizMasteryPct: Int = 0,
    val unfinishedLessonId: String? = null,
    val unfinishedLessonTitle: String? = null,
    val unfinishedUnitTitle: String? = null,
    val unfinishedProgressPct: Int = 0,
    val resumeRoute: String? = null,
    val todayStudyMinutes: Int = 0,
    val totalStudyMinutes: Int = 0,
    val last7DaysChart: List<DailyChartBar> = emptyList()
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadProfileData() {
        viewModelScope.launch {
            authRepository.observeAuthState().flatMapLatest { user ->
                if (user == null) {
                    flowOf(
                        ProfileUiState(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = "User is not authenticated. Please log in."
                        )
                    )
                } else {
                    val uid = user.uid
                    val profileFlow = userRepository.getUserProfile(uid)
                    val progressFlow = progressRepository.getUserProgress(uid)
                    val studyTimeFlow = progressRepository.getLast7DaysStudyTime(uid)
                    val totalTimeFlow = progressRepository.getAllTimeStudyTime(uid)
                    val programLevelsFlow = learningRepository.getLevelsByProgram("program_beginner")

                    combine(
                        profileFlow,
                        progressFlow,
                        studyTimeFlow,
                        totalTimeFlow,
                        programLevelsFlow
                    ) { profileResult, progressResult, studyTimeResult, totalTimeResult, levelsResult ->
                        val levelsList = levelsResult.getOrNull() ?: emptyList()
                        val profile = profileResult.getOrNull()
                        val progress = progressResult.getOrNull()
                        val studyList = studyTimeResult.getOrNull() ?: emptyList()
                        val totalSecs = totalTimeResult.getOrNull() ?: 0L

                        if (profileResult.isFailure || progressResult.isFailure) {
                            val errorMsg = profileResult.exceptionOrNull()?.message
                                ?: progressResult.exceptionOrNull()?.message
                                ?: "Failed to load profile data."
                            ProfileUiState(isLoading = false, errorMessage = errorMsg)
                        } else {
                            val currentLessonId = progress?.currentLessonId ?: "lesson_b1_1_1"
                            val lessonResult = learningRepository.getLesson(currentLessonId).firstOrNull()
                            val lesson = lessonResult?.getOrNull()

                            val unitResult = if (lesson?.unitId != null) {
                                learningRepository.getUnit(lesson.unitId).firstOrNull()
                            } else null
                            val unit = unitResult?.getOrNull()

                            val lessonProgressResult = progressRepository.getLessonProgress(uid, currentLessonId).firstOrNull()
                            val lessonProgress = lessonProgressResult?.getOrNull()

                            val resumeAct = progress?.currentActivity
                            val calculatedResumeRoute = if (!resumeAct.isNullOrBlank()) {
                                "$resumeAct/$currentLessonId"
                            } else {
                                "lesson/$currentLessonId"
                            }

                            val levelId = profile?.levelId ?: "level_beginner_1"
                            val matchedLevel = levelsList.firstOrNull { it.id == levelId }
                                ?: levelsList.firstOrNull { (profile?.xp ?: 0) < it.xpRequired }
                                ?: levelsList.firstOrNull()

                            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val todayStr = sdf.format(Calendar.getInstance().time)

                            val chartBars = studyList.map { daily ->
                                val date = try { sdf.parse(daily.date) } catch (e: Exception) { null }
                                val dayName = if (date != null) dayFormat.format(date) else daily.date.takeLast(2)
                                DailyChartBar(
                                    dayLabel = dayName,
                                    dateString = daily.date,
                                    minutes = (daily.totalSeconds / 60).toInt()
                                )
                            }

                            val todayMins = (studyList.firstOrNull { it.date == todayStr }?.totalSeconds ?: 0L) / 60

                            ProfileUiState(
                                isLoading = false,
                                isAuthenticated = true,
                                errorMessage = null,
                                profile = profile,
                                progress = progress,
                                currentLevelName = matchedLevel?.nameVi ?: matchedLevel?.name ?: profile?.levelName.orEmpty().ifBlank { "Beginner 1: Nền tảng & Giao tiếp" },
                                currentXp = profile?.xp ?: 0,
                                requiredXp = matchedLevel?.xpRequired ?: 900,
                                vocabMasteryPct = progress?.vocabularyProgress ?: 0,
                                grammarMasteryPct = progress?.grammarProgress ?: 0,
                                listeningMasteryPct = progress?.listeningProgress ?: 0,
                                pronunMasteryPct = progress?.pronunciationProgress ?: 0,
                                quizMasteryPct = if ((progress?.completedLessons ?: 0) > 0) 100 else 0,
                                unfinishedLessonId = currentLessonId,
                                unfinishedLessonTitle = lesson?.titleVi ?: lesson?.title ?: "Saying Hello & Goodbye",
                                unfinishedUnitTitle = unit?.titleVi ?: unit?.title ?: "Bài 1: Chào hỏi & Giới thiệu",
                                unfinishedProgressPct = lessonProgress?.progress ?: 0,
                                resumeRoute = calculatedResumeRoute,
                                todayStudyMinutes = todayMins.toInt(),
                                totalStudyMinutes = (totalSecs / 60).toInt(),
                                last7DaysChart = chartBars
                            )
                        }
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
            return
        }

        val user = authRepository.getCurrentUser()
        val uid = user?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingName = true, errorMessage = null) }
            val result = userRepository.updateDisplayName(uid, trimmed)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isUpdatingName = false,
                            successMessage = "Name updated successfully!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingName = false,
                            errorMessage = error.message ?: "Failed to update name."
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(authRepository, userRepository, progressRepository, learningRepository) as T
    }
}
