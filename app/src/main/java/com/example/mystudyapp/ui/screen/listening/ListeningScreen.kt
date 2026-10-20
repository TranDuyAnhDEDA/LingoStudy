package com.example.mystudyapp.ui.screen.listening

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGrayLight
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.ListeningUiModel
import com.example.mystudyapp.viewmodel.ListeningViewModel
import com.example.mystudyapp.viewmodel.ListeningViewModelFactory
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ListeningScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: ListeningViewModel = viewModel(factory = ListeningViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo()

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

    fun speakAudio(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "listening_tts_id")
    }

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "listening")

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .responsiveContentWidth(600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = if (windowInfo.isCompact) 12.dp else 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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

                            Spacer(Modifier.width(if (windowInfo.isCompact) 10.dp else 16.dp))

                            Text(
                                text = uiState.lessonTitle.ifBlank { "Luyện nghe bài học" },
                                fontSize = if (windowInfo.isCompact) 14.sp else 16.sp,
                                lineHeight = if (windowInfo.isCompact) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )

                            if (!uiState.isLoading && uiState.listeningList.isNotEmpty() && !uiState.isFinished) {
                                val currentPos = uiState.currentIndex + 1
                                val totalPos = uiState.listeningList.size
                                Text(
                                    text = "Câu $currentPos / $totalPos",
                                    fontSize = if (windowInfo.isCompact) 12.5.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LingoBlue,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (!uiState.isLoading && uiState.listeningList.isNotEmpty() && !uiState.isFinished) {
                            Spacer(Modifier.height(10.dp))
                            val progressRatio = (uiState.currentIndex + 1).toFloat() / uiState.listeningList.size
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LingoBlue,
                                trackColor = LingoGrayLight,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.listeningList.isNotEmpty() && !uiState.isFinished) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .responsiveContentWidth(600.dp)
                                .fillMaxWidth()
                                .padding(horizontal = if (windowInfo.isCompact) 12.dp else 16.dp, vertical = 10.dp)
                        ) {
                            val isLast = uiState.currentIndex >= uiState.listeningList.size - 1
                            Button(
                                onClick = { viewModel.nextListening() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLast) "Hoàn thành bài nghe" else "Câu tiếp theo",
                                        fontSize = if (windowInfo.isCompact) 14.5.sp else 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Quay lại bài học")
                    }
                }
            }
        } else if (uiState.listeningList.isEmpty()) {
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
                        text = "Chưa có bài luyện nghe cho bài học này.",
                        fontSize = 16.sp,
                        color = LingoGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Quay lại bài học")
                    }
                }
            }
        } else if (uiState.isFinished) {
            // Completion View
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
                        .responsiveContentWidth(480.dp)
                        .fillMaxWidth()
                        .padding(if (windowInfo.isCompact) 16.dp else 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(if (windowInfo.isCompact) 16.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MascotView(
                            modifier = Modifier.size(if (windowInfo.isCompact || windowInfo.isShortScreen) 110.dp else 160.dp),
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
                            text = "Hoàn thành bài luyện nghe!",
                            fontSize = if (windowInfo.isCompact) 18.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Bạn đã lắng nghe và hoàn thành đủ ${uiState.listeningList.size} câu của bài học.",
                            fontSize = if (windowInfo.isCompact) 13.sp else 14.sp,
                            color = LingoGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.restartReview() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Luyện lại",
                                        fontSize = if (windowInfo.isCompact) 12.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Button(
                                onClick = { onBackToLesson() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                            ) {
                                Text(
                                    text = "Về bài học",
                                    fontSize = if (windowInfo.isCompact) 12.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Active Listening Question View
            val currentListening = uiState.listeningList.getOrNull(uiState.currentIndex)
            if (currentListening != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(if (windowInfo.isCompact) 10.dp else 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.responsiveContentWidth(540.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentListening,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "listening_card_transition"
                        ) { listening ->
                            ListeningCard(
                                listening = listening,
                                isCompact = windowInfo.isCompact,
                                onPlayAudio = { speakAudio(listening.title) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListeningCard(
    listening: ListeningUiModel,
    isCompact: Boolean = false,
    onPlayAudio: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isCompact) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(if (isCompact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFF3E8FF),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "BÀI TẬP LUYỆN NGHE",
                    fontSize = if (isCompact) 10.5.sp else 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7E22CE),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(if (isCompact) 14.dp else 20.dp))

            // Audio Player Button
            Surface(
                color = Color(0xFFEFF6FF),
                shape = CircleShape,
                modifier = Modifier
                    .size(if (isCompact) 62.dp else 72.dp)
                    .clickable { onPlayAudio() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Play Audio",
                        tint = LingoBlue,
                        modifier = Modifier.size(if (isCompact) 32.dp else 38.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Nhấn nút để nghe âm thanh",
                fontSize = if (isCompact) 12.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = LingoBlue
            )

            Spacer(Modifier.height(if (isCompact) 14.dp else 20.dp))

            Surface(
                color = Color(0xFFF8FAFF),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = listening.title,
                        fontSize = if (isCompact) 20.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )

                    if (listening.transcript.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = listening.transcript,
                            fontSize = if (isCompact) 13.sp else 14.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
