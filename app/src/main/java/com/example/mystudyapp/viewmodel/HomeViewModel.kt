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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val isNewUser: Boolean = false,
    val displayName: String = "",
    val streak: Int = 0,
    val levelName: String = "",
    val levelOrder: Int = 1,
    val currentXp: Int = 0,
    val requiredXp: Int = 900,
    val currentLessonId: String = "",
    val currentLessonTitle: String = "",
    val currentLessonDescription: String = "",
    val currentUnitTitle: String = "",
    val currentProgramTitle: String = "",
    val currentLessonEstimatedMinutes: Int = 0,
    val currentLessonXpReward: Int = 0,
    val vocabularyProgress: Int = 0,
    val grammarProgress: Int = 0,
    val listeningProgress: Int = 0,
    val pronunciationProgress: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val unfinishedLessonProgressPct: Int = 0,
    val unfinishedSkillsCount: Int = 0,
    val todayStudySeconds: Long = 0L,
    val todayStudyMinutes: Int = 0,
    val resumeRoute: String? = null
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
                                val levelId = profile.levelId.ifBlank { "level_beginner_1" }
                                val currentLessonId = progress.currentLessonId ?: "lesson_b1_1_1"

                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val todayStr = sdf.format(Calendar.getInstance().time)

                                val programLevelsFlow = learningRepository.getLevelsByProgram("program_beginner")
                                val lessonFlow = learningRepository.getLesson(currentLessonId)
                                val lessonProgressFlow = progressRepository.getLessonProgress(uid, currentLessonId)
                                val todayTimeFlow = progressRepository.getDailyStudyTime(uid, todayStr)

                                combine(programLevelsFlow, lessonFlow, lessonProgressFlow, todayTimeFlow) { levelsResult, lessonResult, lessonProgressResult, todayTimeResult ->
                                    val levelsList = levelsResult.getOrNull() ?: emptyList()
                                    val lesson = lessonResult.getOrNull()
                                    val lessonProgress = lessonProgressResult.getOrNull()
                                    val todayStudyRecord = todayTimeResult.getOrNull()
                                    val todaySecs = todayStudyRecord?.totalSeconds ?: 0L
                                    val todayMins = (todaySecs / 60).toInt()

                                    val matchedLevel = levelsList.firstOrNull { it.id == levelId }
                                        ?: levelsList.firstOrNull { profile.xp < it.xpRequired }
                                        ?: levelsList.firstOrNull()

                                    val currentLevelName = matchedLevel?.nameVi ?: matchedLevel?.name ?: "Beginner 1: Nền tảng & Giao tiếp"
                                    val currentLevelOrder = matchedLevel?.order ?: 1
                                    val currentLevelRequiredXp = matchedLevel?.xpRequired ?: 900

                                    val unitResult = if (lesson?.unitId != null) {
                                        learningRepository.getUnit(lesson.unitId).firstOrNull()
                                    } else null
                                    val unit = unitResult?.getOrNull()

                                    val programResult = if (unit?.programId != null) {
                                        learningRepository.getProgram(unit.programId).firstOrNull()
                                    } else null
                                    val program = programResult?.getOrNull()

                                    var skillsDone = 0
                                    if (lessonProgress?.vocabularyCompleted == true) skillsDone++
                                    if (lessonProgress?.grammarCompleted == true) skillsDone++
                                    if (lessonProgress?.listeningCompleted == true) skillsDone++
                                    if (lessonProgress?.pronunciationCompleted == true) skillsDone++
                                    if (lessonProgress?.quizCompleted == true) skillsDone++

                                    val resumeAct = progress.currentActivity
                                    val calculatedResumeRoute = if (!resumeAct.isNullOrBlank()) {
                                        "$resumeAct/$currentLessonId"
                                    } else {
                                        "lesson/$currentLessonId"
                                    }

                                    val isNewUser = (progress.completedLessons == 0) &&
                                            (lessonProgress == null || (!lessonProgress.completed && lessonProgress.progress == 0 && lessonProgress.lastAccessedAt == null))

                                    HomeUiState(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        errorMessage = null,
                                        isNewUser = isNewUser,
                                        displayName = profile.displayName.ifBlank { "Bạn học viên" },
                                        streak = profile.streak,
                                        levelName = currentLevelName,
                                        levelOrder = currentLevelOrder,
                                        currentXp = profile.xp,
                                        requiredXp = currentLevelRequiredXp,
                                        currentLessonId = currentLessonId,
                                        currentLessonTitle = lesson?.titleVi ?: lesson?.title ?: "Saying Hello & Goodbye",
                                        currentLessonDescription = lesson?.descriptionVi ?: lesson?.description ?: "",
                                        currentUnitTitle = unit?.titleVi ?: unit?.title ?: "Bài 1: Chào hỏi & Giới thiệu",
                                        currentProgramTitle = program?.titleVi ?: program?.title ?: "Tiếng Anh cho người mới bắt đầu",
                                        currentLessonEstimatedMinutes = lesson?.estimatedMinutes ?: 8,
                                        currentLessonXpReward = lesson?.xpReward ?: 30,
                                        vocabularyProgress = progress.vocabularyProgress,
                                        grammarProgress = progress.grammarProgress,
                                        listeningProgress = progress.listeningProgress,
                                        pronunciationProgress = progress.pronunciationProgress,
                                        completedLessons = progress.completedLessons,
                                        totalLessons = progress.totalLessons,
                                        unfinishedLessonProgressPct = lessonProgress?.progress ?: 0,
                                        unfinishedSkillsCount = skillsDone,
                                        todayStudySeconds = todaySecs,
                                        todayStudyMinutes = todayMins,
                                        resumeRoute = calculatedResumeRoute
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
