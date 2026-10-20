package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.local.DictionaryLocalStore
import com.example.mystudyapp.data.model.UserLevelCalculator
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
    val minutes: Int,
    val isToday: Boolean = false,
    val dateDisplay: String = ""
)

data class LearningHistoryItem(
    val lessonId: String,
    val title: String,
    val unitTitle: String,
    val progressPct: Int,
    val isCompleted: Boolean,
    val completedAtText: String,
    val vocabDone: Boolean = false,
    val grammarDone: Boolean = false,
    val listeningDone: Boolean = false,
    val pronunDone: Boolean = false,
    val quizDone: Boolean = false
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
    val currentLevelXp: Int = 0,
    val requiredXp: Int = 200,
    val nextLevelThresholdXp: Int = 200,
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
    val last7DaysChart: List<DailyChartBar> = emptyList(),
    val learningHistory: List<LearningHistoryItem> = emptyList(),
    val favoriteWords: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val reminderTime: String = "20:00"
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl(),
    private val dictionaryStore: DictionaryLocalStore = DictionaryLocalStore()
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
                            errorMessage = "Người dùng chưa đăng nhập. Vui lòng đăng nhập để tiếp tục."
                        )
                    )
                } else {
                    val uid = user.uid
                    val profileFlow = userRepository.getUserProfile(uid)
                    val progressFlow = progressRepository.getUserProgress(uid)
                    val studyTimeFlow = progressRepository.getLast7DaysStudyTime(uid)
                    val totalTimeFlow = progressRepository.getAllTimeStudyTime(uid)
                    val programLevelsFlow = learningRepository.getLevelsByProgram("program_beginner")
                    val historyFlow = progressRepository.getLearningHistory(uid)

                    val baseFlow = combine(profileFlow, progressFlow, studyTimeFlow) { prof, prog, study ->
                        Triple(prof, prog, study)
                    }
                    val extraFlow = combine(totalTimeFlow, programLevelsFlow, historyFlow) { tot, lvl, hist ->
                        Triple(tot, lvl, hist)
                    }

                    combine(baseFlow, extraFlow) { (profileResult, progressResult, studyTimeResult), (totalTimeResult, levelsResult, historyResult) ->
                        val levelsList = levelsResult.getOrNull() ?: emptyList()
                        val rawProfile = profileResult.getOrNull()
                        val progress = progressResult.getOrNull()
                        val studyList = studyTimeResult.getOrNull() ?: emptyList()
                        val totalSecs = totalTimeResult.getOrNull() ?: 0L
                        val historyList = historyResult.getOrNull() ?: emptyList()

                        // Resolved profile using authenticated user's actual info or defaults if not found
                        val effectiveProfile = rawProfile ?: UserProfile(
                            uid = uid,
                            displayName = user.displayName?.ifBlank { "Bạn học viên" } ?: "Bạn học viên",
                            email = user.email.orEmpty(),
                            xp = 0,
                            streak = 0,
                            avatarId = "boy_hoodie",
                            totalLessonsCompleted = 0
                        )

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

                        val levelId = effectiveProfile.levelId.ifBlank { "level_beginner_1" }
                        val matchedLevel = levelsList.firstOrNull { it.id == levelId }
                            ?: levelsList.firstOrNull { effectiveProfile.xp < it.xpRequired }
                            ?: levelsList.firstOrNull()

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val todayStr = sdf.format(Calendar.getInstance().time)
                        val effectiveTotalMins = (totalSecs / 60).toInt()

                        val chartBars = studyList.mapIndexed { index, daily ->
                            val date = try { sdf.parse(daily.date) } catch (e: Exception) { null }
                            val isToday = (daily.date == todayStr)

                            val vietnameseDay = if (date != null) {
                                val cal = Calendar.getInstance().apply { time = date }
                                when (cal.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.MONDAY -> "T2"
                                    Calendar.TUESDAY -> "T3"
                                    Calendar.WEDNESDAY -> "T4"
                                    Calendar.THURSDAY -> "T5"
                                    Calendar.FRIDAY -> "T6"
                                    Calendar.SATURDAY -> "T7"
                                    Calendar.SUNDAY -> "CN"
                                    else -> "T2"
                                }
                            } else {
                                daily.date.takeLast(2)
                            }

                            val dateDisplay = if (date != null) {
                                SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
                            } else ""

                            val mins = (daily.totalSeconds / 60).toInt()

                            DailyChartBar(
                                dayLabel = vietnameseDay,
                                dateString = daily.date,
                                minutes = mins,
                                isToday = isToday,
                                dateDisplay = dateDisplay
                            )
                        }

                        val todayMins = (studyList.firstOrNull { it.date == todayStr }?.totalSeconds ?: 0L) / 60

                        // Construct learning history items from actual user progress
                        val timeFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                        val items: List<LearningHistoryItem> = historyList.map { lp ->
                            val l = learningRepository.getLesson(lp.lessonId).firstOrNull()?.getOrNull()
                            val u = if (l?.unitId != null) learningRepository.getUnit(l.unitId).firstOrNull()?.getOrNull() else null
                            val completedTime = lp.lastAccessedAt?.toDate()
                            val timeText = if (completedTime != null) timeFormat.format(completedTime) else "Gần đây"
                            LearningHistoryItem(
                                lessonId = lp.lessonId,
                                title = l?.titleVi ?: l?.title ?: "Bài học",
                                unitTitle = u?.titleVi ?: u?.title ?: "Unit",
                                progressPct = if (lp.completed) 100 else lp.progress,
                                isCompleted = lp.completed,
                                completedAtText = timeText,
                                vocabDone = lp.vocabularyCompleted,
                                grammarDone = lp.grammarCompleted,
                                listeningDone = lp.listeningCompleted,
                                pronunDone = lp.pronunciationCompleted,
                                quizDone = lp.quizCompleted
                            )
                        }

                        val favs = dictionaryStore.getFavoriteWords().toList()

                        val levelInfo = UserLevelCalculator.calculate(
                            effectiveProfile.xp,
                            matchedLevel?.nameVi ?: matchedLevel?.name ?: effectiveProfile.levelName.ifBlank { null }
                        )
                        ProfileUiState(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null,
                            profile = effectiveProfile,
                            progress = progress,
                            currentLevelName = levelInfo.levelName,
                            currentXp = effectiveProfile.xp,
                            currentLevelXp = levelInfo.currentLevelXp,
                            requiredXp = levelInfo.levelRequiredXp,
                            nextLevelThresholdXp = levelInfo.nextLevelThresholdXp,
                            vocabMasteryPct = progress?.vocabularyProgress ?: 0,
                            grammarMasteryPct = progress?.grammarProgress ?: 0,
                            listeningMasteryPct = progress?.listeningProgress ?: 0,
                            pronunMasteryPct = progress?.pronunciationProgress ?: 0,
                            quizMasteryPct = if ((progress?.completedLessons ?: 0) > 0) 100 else 0,
                            unfinishedLessonId = if (progress != null && progress.completedLessons > 0) currentLessonId else null,
                            unfinishedLessonTitle = if (lesson != null && progress != null && progress.completedLessons > 0) (lesson.titleVi ?: lesson.title) else null,
                            unfinishedUnitTitle = if (unit != null && progress != null && progress.completedLessons > 0) (unit.titleVi ?: unit.title) else null,
                            unfinishedProgressPct = lessonProgress?.progress ?: 0,
                            resumeRoute = calculatedResumeRoute,
                            todayStudyMinutes = todayMins.toInt(),
                            totalStudyMinutes = effectiveTotalMins,
                            last7DaysChart = chartBars,
                            learningHistory = items,
                            favoriteWords = favs
                        )
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateAvatar(avatarId: String, customAvatarPath: String? = null) {
        val user = authRepository.getCurrentUser()
        val uid = user?.uid ?: _uiState.value.profile?.uid ?: "current_user"

        val currentProf = _uiState.value.profile ?: UserProfile(uid = uid)
        _uiState.update {
            it.copy(
                profile = currentProf.copy(
                    avatarId = avatarId,
                    customAvatarPath = customAvatarPath
                ),
                successMessage = "Đã cập nhật ảnh đại diện thành công!"
            )
        }

        viewModelScope.launch {
            userRepository.updateAvatar(uid, avatarId, customAvatarPath)
        }
    }

    fun updateProfileDetails(
        newName: String,
        newPhone: String = "",
        newGoalMinutes: Int = 30,
        newLevelName: String = "Beginner 1: Nền tảng & Giao tiếp"
    ) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tên hiển thị không được để trống.") }
            return
        }

        val user = authRepository.getCurrentUser()
        val uid = user?.uid ?: _uiState.value.profile?.uid ?: "current_user"

        val currentProf = _uiState.value.profile ?: UserProfile(uid = uid)
        _uiState.update {
            it.copy(
                profile = currentProf.copy(
                    displayName = trimmed,
                    phoneNumber = newPhone.trim(),
                    dailyGoalMinutes = newGoalMinutes,
                    levelName = newLevelName
                ),
                currentLevelName = newLevelName,
                successMessage = "Đã cập nhật thông tin cá nhân thành công!"
            )
        }

        viewModelScope.launch {
            userRepository.updateDisplayName(uid, trimmed)
            userRepository.updateProfileDetails(
                uid = uid,
                displayName = trimmed,
                phoneNumber = newPhone.trim(),
                dailyGoalMinutes = newGoalMinutes,
                levelName = newLevelName
            )
        }
    }

    fun updateDisplayName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
            return
        }

        val user = authRepository.getCurrentUser()
        val uid = user?.uid ?: _uiState.value.profile?.uid ?: return

        val currentProf = _uiState.value.profile ?: UserProfile(uid = uid)
        _uiState.update {
            it.copy(
                profile = currentProf.copy(displayName = trimmed),
                isUpdatingName = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
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

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setReminderTime(time: String) {
        _uiState.update { it.copy(reminderTime = time) }
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
    private val learningRepository: LearningRepository = LearningRepositoryImpl(),
    private val dictionaryStore: DictionaryLocalStore = DictionaryLocalStore()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(authRepository, userRepository, progressRepository, learningRepository, dictionaryStore) as T
    }
}
