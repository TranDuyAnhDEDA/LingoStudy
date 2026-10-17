package com.example.mystudyapp.ui.screen.quiz

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.ActiveStudyTimeTracker
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.QuizUiModel
import com.example.mystudyapp.viewmodel.QuizUiState
import com.example.mystudyapp.viewmodel.QuizViewModel
import com.example.mystudyapp.viewmodel.QuizViewModelFactory
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: QuizViewModel = viewModel(factory = QuizViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    fun speakQuestion(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "quiz_tts_id")
    }

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "quiz")

    Scaffold(
        containerColor = Color(0xFFF3F9F6)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2A8A5E))
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
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A8A5E))
                    ) {
                        Text("Quay lại bài học")
                    }
                }
            }
        } else if (uiState.quizList.isEmpty()) {
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
                        text = "Chưa có câu hỏi kiểm tra cho bài học này.",
                        fontSize = 16.sp,
                        color = LingoGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A8A5E))
                    ) {
                        Text("Quay lại bài học")
                    }
                }
            }
        } else if (uiState.isFinished) {
            // Completion / Results View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MascotView(
                            modifier = Modifier.size(160.dp),
                            expression = MascotExpression.EXCITED,
                            enableAnimation = true
                        )

                        Spacer(Modifier.height(16.dp))

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LingoGreen,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Hoàn thành Bài kiểm tra!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Kết quả: ${uiState.currentScore} / ${uiState.quizList.size} câu đúng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LingoBlue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "+30 XP hoàn thành bài học",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.retakeQuiz() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF27438), contentColor = Color.White)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Làm lại bài test",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { onBackToLesson() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A8A5E))
                            ) {
                                Text(
                                    text = "Về bài học",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Main Quiz Question Screen
            val currentQuiz = uiState.quizList.getOrNull(uiState.currentIndex)
            if (currentQuiz != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Top Header & Progress
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { onBackToLesson() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.padding(8.dp),
                                    tint = Color(0xFF0E4D2E)
                                )
                            }

                            Text(
                                text = uiState.lessonTitle.ifBlank { "Nói lời chào & tạm biệt" },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0E4D2E),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            )

                            Surface(
                                color = Color(0xFFE6F4EA),
                                border = BorderStroke(1.dp, Color(0xFF2A8A5E)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = Color(0xFF2A8A5E),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "Câu ${uiState.currentIndex + 1} / ${uiState.quizList.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0E4D2E)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val progressRatio = (uiState.currentIndex + 1).toFloat() / uiState.quizList.size
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color(0xFF2A8A5E),
                            trackColor = Color(0xFFDCECE5),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 2. Main Quiz Card with Mascot & Speech Bubble Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentQuiz,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "quiz_card_transition"
                        ) { quiz ->
                            QuizMainCard(
                                quiz = quiz,
                                uiState = uiState,
                                onOptionSelected = { option -> viewModel.selectOption(option) },
                                onPlayAudio = { speakQuestion(quiz.question) }
                            )
                        }

                        // Mascot & Speech Bubble Overlay at Bottom Right
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = when {
                                        uiState.isSubmitted && uiState.isCorrect -> "Tuyệt vời! +10 XP"
                                        uiState.isSubmitted && !uiState.isCorrect -> "Cố lên bạn ơi!"
                                        else -> "Chọn câu trả lời đúng nhé!"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            MascotView(
                                modifier = Modifier.size(110.dp),
                                expression = if (uiState.isSubmitted && uiState.isCorrect) MascotExpression.EXCITED else MascotExpression.THINKING,
                                enableAnimation = true
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 3. Bottom Action Button
                    val isLast = uiState.currentIndex >= uiState.quizList.size - 1
                    val buttonEnabled = uiState.selectedOption != null || uiState.isSubmitted
                    val buttonText = when {
                        !uiState.isSubmitted -> "Kiểm tra đáp án"
                        isLast -> "Hoàn thành bài test"
                        else -> "Câu tiếp theo"
                    }

                    Button(
                        onClick = {
                            if (!uiState.isSubmitted) {
                                viewModel.submitAnswer()
                            } else {
                                viewModel.nextQuestion()
                            }
                        },
                        enabled = buttonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isSubmitted && uiState.isCorrect) Color(0xFF2A8A5E) else Color(0xFFF27438),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = buttonText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = if (uiState.isSubmitted && isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizMainCard(
    quiz: QuizUiModel,
    uiState: QuizUiState,
    onOptionSelected: (String) -> Unit,
    onPlayAudio: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header Row (Badge & Audio Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFE6F4EA),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Color(0xFF137333),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "BÀI KIỂM TRA TRẮC NGHIỆM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF137333)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFE6F4EA),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onPlayAudio() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio Question",
                                tint = Color(0xFF2A8A5E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Question Row
                Text(
                    text = quiz.question,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0E4D2E),
                    lineHeight = 26.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Options List (A, B, C, D)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                val optionLetters = listOf("A", "B", "C", "D")

                quiz.options.forEachIndexed { index, option ->
                    val isSelected = (option.trim() == uiState.selectedOption?.trim())
                    val isCorrectOption = (option.trim() == quiz.correctAnswer.trim())
                    val letter = optionLetters.getOrElse(index) { "${index + 1}" }

                    val (bgColor, borderColor, circleBg, circleTextColor) = when {
                        uiState.isSubmitted && isCorrectOption -> Quadruple(
                            Color(0xFFE6F4EA),
                            Color(0xFF2A8A5E),
                            Color(0xFF2A8A5E),
                            Color.White
                        )
                        uiState.isSubmitted && isSelected && !isCorrectOption -> Quadruple(
                            Color(0xFFFEE2E2),
                            Color(0xFFEF4444),
                            Color(0xFFEF4444),
                            Color.White
                        )
                        isSelected -> Quadruple(
                            Color(0xFFE6F4EA),
                            Color(0xFF2A8A5E),
                            Color(0xFF2A8A5E),
                            Color.White
                        )
                        else -> Quadruple(
                            Color.White,
                            Color(0xFFE2E8F0),
                            Color(0xFFF1F5F9),
                            Color(0xFF475569)
                        )
                    }

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (isSelected || (uiState.isSubmitted && isCorrectOption)) 2.dp else 1.5.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isSubmitted) { onOptionSelected(option) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = circleBg,
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = letter,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = circleTextColor
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = option,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected || (uiState.isSubmitted && isCorrectOption)) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )

                            if (uiState.isSubmitted && isCorrectOption) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = Color(0xFF2A8A5E),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (uiState.isSubmitted && isSelected && !isCorrectOption) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Incorrect",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
