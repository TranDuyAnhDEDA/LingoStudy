package com.example.mystudyapp.ui.screen.game

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.ActiveStudyTimeTracker
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.rememberWindowInfo
import com.example.mystudyapp.ui.component.shared.responsiveContentWidth
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.GameUiState
import com.example.mystudyapp.viewmodel.GameViewModel
import com.example.mystudyapp.viewmodel.GameViewModelFactory
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo()

    // Text-to-speech for game pronunciation
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    fun speakSentence(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "game_tts_id")
    }

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "game")

    val mascotExpression = when {
        uiState.isFinished -> MascotExpression.EXCITED
        uiState.isChecked && uiState.isCorrect -> MascotExpression.EXCITED
        uiState.isChecked && !uiState.isCorrect -> MascotExpression.THINKING
        else -> MascotExpression.HAPPY
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier
                            .responsiveContentWidth(680.dp)
                            .fillMaxWidth()
                            .padding(horizontal = if (windowInfo.isCompact) 12.dp else 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(if (windowInfo.isCompact) 36.dp else 40.dp)
                                .clickable { onBackToLesson() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFF1E3A8A)
                            )
                        }

                        Spacer(Modifier.width(if (windowInfo.isCompact) 10.dp else 14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.gameType == GameType.SENTENCE_BUILDER) "Ghép chữ thành câu" else "Điền từ vào ô trống",
                                fontSize = if (windowInfo.isCompact) 13.5.sp else 16.sp,
                                lineHeight = if (windowInfo.isCompact) 17.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                maxLines = 2
                            )
                            Text(
                                text = "Unit ${uiState.unitOrder} · ${if (uiState.gameType == GameType.SENTENCE_BUILDER) "Trò chơi 1" else "Trò chơi 2"}",
                                fontSize = if (windowInfo.isCompact) 11.sp else 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }

                        if (!uiState.isLoading && !uiState.isFinished && uiState.questions.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "Câu ${uiState.currentIndex + 1} / ${uiState.questions.size}",
                                    fontSize = if (windowInfo.isCompact) 11.sp else 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LingoBlue,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorMessage == null && !uiState.isFinished && uiState.questions.isNotEmpty()) {
                GameBottomBar(
                    uiState = uiState,
                    isCompact = windowInfo.isCompact,
                    onCheck = { viewModel.checkAnswer() },
                    onNext = { viewModel.nextQuestion() },
                    onRetry = { viewModel.retryCurrentQuestion() },
                    onSpeak = ::speakSentence
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LingoBlue)
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Có lỗi xảy ra.",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBackToLesson,
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Quay lại bài học")
                    }
                }
            }
        } else if (uiState.isFinished) {
            GameFinishedView(
                uiState = uiState,
                isCompact = windowInfo.isCompact,
                isShortScreen = windowInfo.isShortScreen,
                onRestart = { viewModel.restartGame() },
                onBackToLesson = onBackToLesson,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            val currentQ = uiState.questions.getOrNull(uiState.currentIndex)
            if (currentQ == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có câu hỏi trò chơi nào.", color = Color(0xFF64748B))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .responsiveContentWidth(680.dp)
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(if (windowInfo.isCompact) 12.dp else 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 5-Segment Story-Style Top Progress Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.questions.indices.forEach { idx ->
                                val isCompletedOrActive = idx <= uiState.currentIndex
                                val segmentColor by animateColorAsState(
                                    targetValue = if (isCompletedOrActive) LingoBlue else Color(0xFFE2E8F0),
                                    animationSpec = tween(durationMillis = 250),
                                    label = "game_bar_$idx"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(segmentColor)
                                )
                            }
                        }

                        // Mascot expression header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = currentQ.categoryLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            MascotView(
                                modifier = Modifier.size(if (windowInfo.isCompact) 56.dp else 76.dp),
                                expression = mascotExpression,
                                enableAnimation = true
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Question Prompt Card (Vietnamese translation)
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Color(0xFFEEF2F6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(if (windowInfo.isCompact) 14.dp else 18.dp)) {
                                Text(
                                    text = if (uiState.gameType == GameType.SENTENCE_BUILDER) {
                                        "Ghép các từ bên dưới để tạo thành câu hoàn chỉnh:"
                                    } else {
                                        "Chọn từ chính xác để điền vào ô trống:"
                                    },
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = currentQ.promptVi,
                                        fontSize = if (windowInfo.isCompact) 16.sp else 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { speakSentence(currentQ.audioSentence) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Nghe phát âm",
                                            tint = LingoBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Interactive Game Body depending on GameType
                        if (uiState.gameType == GameType.SENTENCE_BUILDER) {
                            SentenceBuilderGameContent(
                                uiState = uiState,
                                isCompact = windowInfo.isCompact,
                                onSelectToken = { viewModel.selectToken(it) },
                                onRemoveToken = { viewModel.removeToken(it) },
                                onClearPlaced = { viewModel.clearPlacedTokens() }
                            )
                        } else {
                            FillInBlankGameContent(
                                question = currentQ,
                                uiState = uiState,
                                isCompact = windowInfo.isCompact,
                                onSelectOption = { viewModel.selectOption(it) }
                            )
                        }

                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SentenceBuilderGameContent(
    uiState: GameUiState,
    isCompact: Boolean = false,
    onSelectToken: (WordToken) -> Unit,
    onRemoveToken: (WordToken) -> Unit,
    onClearPlaced: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Drop zone / Placed Words Box
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (uiState.isChecked) (if (uiState.isCorrect) LingoGreen else Color(0xFFDC2626)) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 12.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Câu ghép của bạn:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    if (uiState.placedTokens.isNotEmpty() && !uiState.isChecked) {
                        Text(
                            text = "Xóa tất cả",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .clickable { onClearPlaced() }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (uiState.placedTokens.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chạm vào các từ bên dưới theo thứ tự để ghép câu",
                            fontSize = if (isCompact) 12.sp else 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                    ) {
                        uiState.placedTokens.forEach { token ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.clickable(enabled = !uiState.isChecked) {
                                    onRemoveToken(token)
                                }
                            ) {
                                Text(
                                    text = token.text,
                                    fontSize = if (isCompact) 13.5.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A),
                                    modifier = Modifier.padding(horizontal = if (isCompact) 10.dp else 12.dp, vertical = if (isCompact) 6.dp else 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Word Bank
        Text(
            text = "Kho từ vựng của bài học:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp)
        ) {
            uiState.availableTokens.forEach { token ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable(enabled = !uiState.isChecked) {
                        onSelectToken(token)
                    }
                ) {
                    Text(
                        text = token.text,
                        fontSize = if (isCompact) 13.5.sp else 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = if (isCompact) 11.dp else 14.dp, vertical = if (isCompact) 8.dp else 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FillInBlankGameContent(
    question: GameQuestion,
    uiState: GameUiState,
    isCompact: Boolean = false,
    onSelectOption: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sentence with Blank Card
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFEEF2F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) 16.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hoàn thành câu:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                // Render sentence with filled blank pill
                val parts = question.sentenceWithBlank.split("_____")
                val before = parts.getOrNull(0).orEmpty()
                val after = parts.getOrNull(1).orEmpty()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (before.isNotBlank()) {
                        Text(
                            text = before,
                            fontSize = if (isCompact) 15.5.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }

                    // Blank slot
                    val blankBg = when {
                        uiState.isChecked && uiState.isCorrect -> Color(0xFFECFDF5)
                        uiState.isChecked && !uiState.isCorrect -> Color(0xFFFEF2F2)
                        uiState.selectedOption != null -> Color(0xFFEFF6FF)
                        else -> Color(0xFFF1F5F9)
                    }

                    val blankBorder = when {
                        uiState.isChecked && uiState.isCorrect -> LingoGreen
                        uiState.isChecked && !uiState.isCorrect -> Color(0xFFDC2626)
                        uiState.selectedOption != null -> LingoBlue
                        else -> Color(0xFFCBD5E1)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = blankBg,
                        border = BorderStroke(1.5.dp, blankBorder),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = uiState.selectedOption ?: "  _____  ",
                            fontSize = if (isCompact) 14.5.sp else 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.selectedOption != null) Color(0xFF1E3A8A) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    if (after.isNotBlank()) {
                        Text(
                            text = after,
                            fontSize = if (isCompact) 15.5.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Options choices
        Text(
            text = "Lựa chọn từ thích hợp:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(10.dp))

        question.options.forEachIndexed { idx, option ->
            val isSelected = uiState.selectedOption == option
            val optionLetter = ('A' + idx).toString()

            val optionBg = when {
                uiState.isChecked && option.equals(question.targetWord, ignoreCase = true) -> Color(0xFFECFDF5)
                uiState.isChecked && isSelected && !uiState.isCorrect -> Color(0xFFFEF2F2)
                isSelected -> Color(0xFFEFF6FF)
                else -> Color.White
            }

            val optionBorder = when {
                uiState.isChecked && option.equals(question.targetWord, ignoreCase = true) -> LingoGreen
                uiState.isChecked && isSelected && !uiState.isCorrect -> Color(0xFFDC2626)
                isSelected -> LingoBlue
                else -> Color(0xFFE2E8F0)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = optionBg,
                border = BorderStroke(1.5.dp, optionBorder),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !uiState.isChecked) {
                        onSelectOption(option)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) LingoBlue else Color(0xFFF1F5F9),
                        modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = optionLetter,
                                fontSize = if (isCompact) 12.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = option,
                        fontSize = if (isCompact) 14.5.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBottomBar(
    uiState: GameUiState,
    isCompact: Boolean = false,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val currentQ = uiState.questions.getOrNull(uiState.currentIndex)
    val canCheck = if (uiState.gameType == GameType.SENTENCE_BUILDER) {
        uiState.placedTokens.isNotEmpty()
    } else {
        uiState.selectedOption != null
    }

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .responsiveContentWidth(680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = 10.dp)
            ) {
                // Feedback Banner when checked
                AnimatedVisibility(
                    visible = uiState.isChecked,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (currentQ != null) {
                        Surface(
                            color = if (uiState.isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (uiState.isCorrect) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (uiState.isCorrect) LingoGreen else Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (uiState.isCorrect) "Chính xác! Làm rất tốt!" else "Chưa chính xác!",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isCorrect) Color(0xFF065F46) else Color(0xFF991B1B)
                                    )
                                    if (!uiState.isCorrect) {
                                        Text(
                                            text = "Đáp án đúng: ${currentQ.fullSentence}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF7F1D1D)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onSpeak(currentQ.audioSentence) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Nghe lại",
                                        tint = if (uiState.isCorrect) LingoGreen else Color(0xFFDC2626),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Buttons
                if (!uiState.isChecked) {
                    Button(
                        onClick = onCheck,
                        enabled = canCheck,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LingoBlue,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        )
                    ) {
                        Text("Kiểm tra đáp án", fontSize = if (isCompact) 14.sp else 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!uiState.isCorrect) {
                            OutlinedButton(
                                onClick = onRetry,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Thử lại", fontSize = if (isCompact) 13.sp else 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }

                        Button(
                            onClick = onNext,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isCorrect) LingoGreen else LingoBlue
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (uiState.currentIndex < uiState.questions.size - 1) "Câu tiếp theo" else "Hoàn thành",
                                    fontSize = if (isCompact) 12.5.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameFinishedView(
    uiState: GameUiState,
    isCompact: Boolean = false,
    isShortScreen: Boolean = false,
    onRestart: () -> Unit,
    onBackToLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isCompact) 16.dp else 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .responsiveContentWidth(480.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(if (isCompact) 18.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotView(
                    modifier = Modifier.size(if (isCompact || isShortScreen) 100.dp else 140.dp),
                    expression = MascotExpression.EXCITED,
                    enableAnimation = true
                )

                Spacer(Modifier.height(14.dp))

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LingoGreen,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Hoàn thành trò chơi!",
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Điểm số: ${uiState.score} / ${uiState.questions.size} câu chính xác",
                    fontSize = if (isCompact) 13.5.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LingoBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "+${uiState.xpEarned} XP kinh nghiệm",
                            fontSize = if (isCompact) 12.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF334155)
                        )
                    ) {
                        Text("Chơi lại", fontSize = if (isCompact) 12.5.sp else 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = onBackToLesson,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Về bài học", fontSize = if (isCompact) 12.5.sp else 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}
