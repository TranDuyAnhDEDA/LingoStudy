package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.LearningRepository
import com.example.mystudyapp.data.repository.LearningRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import com.example.mystudyapp.ui.screen.game.GameQuestion
import com.example.mystudyapp.ui.screen.game.GameQuestionGenerator
import com.example.mystudyapp.ui.screen.game.GameType
import com.example.mystudyapp.ui.screen.game.WordToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val unitTitle: String = "",
    val unitOrder: Int = 1,
    val gameType: GameType = GameType.SENTENCE_BUILDER,
    val questions: List<GameQuestion> = emptyList(),
    val currentIndex: Int = 0,
    // Game 1: Ghép chữ thành câu
    val placedTokens: List<WordToken> = emptyList(),
    val availableTokens: List<WordToken> = emptyList(),
    // Game 2: Điền từ vào ô trống
    val selectedOption: String? = null,
    // Feedback & Tiến độ
    val isChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val xpEarned: Int = 20
)

class GameViewModel(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState(lessonId = lessonId))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    init {
        loadGameData()
    }

    fun loadGameData() {
        if (lessonId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Mã bài học không hợp lệ.") }
            return
        }

        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
                    return@collect
                }

                currentUserId = user.uid
                _uiState.update { it.copy(isLoading = true, errorMessage = null, isAuthenticated = true) }

                // 1. Fetch lesson
                val lessonResult = learningRepository.getLesson(lessonId).firstOrNull()
                val lesson = lessonResult?.getOrNull()

                if (lesson == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy bài học.") }
                    return@collect
                }

                // 2. Fetch unit to determine odd / even
                val unitResult = learningRepository.getUnit(lesson.unitId).firstOrNull()
                val unit = unitResult?.getOrNull()
                val unitOrder = GameQuestionGenerator.resolveUnitOrder(lesson.unitId, unit)
                val isOdd = (unitOrder % 2 != 0)
                val gameType = if (isOdd) GameType.SENTENCE_BUILDER else GameType.FILL_IN_BLANK

                // 3. Fetch vocabulary & grammar for this lesson
                val vocabResult = learningRepository.getVocabularyByLesson(lessonId).firstOrNull()
                val vocabList = vocabResult?.getOrNull() ?: emptyList()

                val grammarResult = learningRepository.getGrammarByLesson(lessonId).firstOrNull()
                val grammarList = grammarResult?.getOrNull() ?: emptyList()

                // 4. Generate 5 challenges based on lesson content & game type
                val questions = GameQuestionGenerator.generateQuestions(
                    lesson = lesson,
                    unit = unit,
                    vocabularies = vocabList,
                    grammars = grammarList
                )

                val firstQuestion = questions.firstOrNull()

                _uiState.value = GameUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    errorMessage = null,
                    lessonId = lessonId,
                    lessonTitle = lesson.titleVi ?: lesson.title,
                    unitTitle = unit?.titleVi ?: unit?.title ?: "Bài học",
                    unitOrder = unitOrder,
                    gameType = gameType,
                    questions = questions,
                    currentIndex = 0,
                    placedTokens = emptyList(),
                    availableTokens = firstQuestion?.scrambledTokens ?: emptyList(),
                    selectedOption = null,
                    isChecked = false,
                    isCorrect = false,
                    score = 0,
                    isFinished = false
                )
            }
        }
    }

    // GAME 1: Trò chơi ghép chữ thành câu
    fun selectToken(token: WordToken) {
        val state = _uiState.value
        if (state.isChecked) return // Don't allow changes after checking
        if (state.availableTokens.any { it.id == token.id }) {
            _uiState.update { current ->
                current.copy(
                    availableTokens = current.availableTokens.filter { it.id != token.id },
                    placedTokens = current.placedTokens + token
                )
            }
        }
    }

    fun removeToken(token: WordToken) {
        val state = _uiState.value
        if (state.isChecked) return
        if (state.placedTokens.any { it.id == token.id }) {
            _uiState.update { current ->
                current.copy(
                    placedTokens = current.placedTokens.filter { it.id != token.id },
                    availableTokens = current.availableTokens + token
                )
            }
        }
    }

    fun clearPlacedTokens() {
        val state = _uiState.value
        if (state.isChecked) return
        val currentQ = state.questions.getOrNull(state.currentIndex)
        _uiState.update { current ->
            current.copy(
                placedTokens = emptyList(),
                availableTokens = currentQ?.scrambledTokens ?: emptyList()
            )
        }
    }

    // GAME 2: Trò chơi điền từ vào ô trống
    fun selectOption(option: String) {
        val state = _uiState.value
        if (state.isChecked) return
        _uiState.update { it.copy(selectedOption = option) }
    }

    // CHECK ANSWER FOR BOTH GAMES
    fun checkAnswer() {
        val state = _uiState.value
        if (state.isChecked) return

        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        var correct = false

        if (state.gameType == GameType.SENTENCE_BUILDER) {
            val userJoined = state.placedTokens.joinToString(" ") { it.text.trim() }
            val cleanUser = state.placedTokens.map { cleanWord(it.text) }
            val cleanTarget = currentQ.targetTokens.map { cleanWord(it) }

            correct = userJoined.equals(currentQ.fullSentence.trim(), ignoreCase = true) ||
                (cleanUser.isNotEmpty() && cleanUser == cleanTarget)
        } else {
            // Fill in the blank
            val userAns = state.selectedOption?.trim().orEmpty()
            val target = currentQ.targetWord.trim()
            correct = userAns.equals(target, ignoreCase = true)
        }

        _uiState.update { current ->
            current.copy(
                isChecked = true,
                isCorrect = correct,
                score = if (correct) current.score + 1 else current.score
            )
        }
    }

    private fun cleanWord(word: String): String {
        return word.trim().lowercase()
            .replace(Regex("""[.,!?;:]"""), "")
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIdx = state.currentIndex + 1

        if (nextIdx < state.questions.size) {
            val nextQ = state.questions[nextIdx]
            _uiState.update { current ->
                current.copy(
                    currentIndex = nextIdx,
                    placedTokens = emptyList(),
                    availableTokens = nextQ.scrambledTokens,
                    selectedOption = null,
                    isChecked = false,
                    isCorrect = false
                )
            }
        } else {
            // Finished game!
            _uiState.update { it.copy(isFinished = true) }
            saveGameCompletion()
        }
    }

    fun retryCurrentQuestion() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        _uiState.update { current ->
            current.copy(
                placedTokens = emptyList(),
                availableTokens = currentQ.scrambledTokens,
                selectedOption = null,
                isChecked = false,
                isCorrect = false
            )
        }
    }

    fun restartGame() {
        val state = _uiState.value
        val firstQ = state.questions.firstOrNull()
        _uiState.update { current ->
            current.copy(
                currentIndex = 0,
                placedTokens = emptyList(),
                availableTokens = firstQ?.scrambledTokens ?: emptyList(),
                selectedOption = null,
                isChecked = false,
                isCorrect = false,
                score = 0,
                isFinished = false
            )
        }
    }

    private fun saveGameCompletion() {
        if (currentUserId.isBlank() || lessonId.isBlank()) return
        viewModelScope.launch {
            try {
                // Save pronunciationCompleted = true (used as the game completed flag)
                progressRepository.updateLessonProgress(
                    uid = currentUserId,
                    progress = LessonProgress(
                        lessonId = lessonId,
                        pronunciationCompleted = true
                    )
                )
                // Complete lesson if all eligible modules are done
                progressRepository.completeLessonIfEligible(currentUserId, lessonId)
            } catch (e: Exception) {
                // Ignore background sync errors
            }
        }
    }
}

class GameViewModelFactory(
    private val lessonId: String,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(),
    private val learningRepository: LearningRepository = LearningRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(
            lessonId,
            authRepository,
            progressRepository,
            learningRepository
        ) as T
    }
}
