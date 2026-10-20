package com.example.mystudyapp.ui.screen.pronunciation

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.mystudyapp.viewmodel.PronunciationUiModel
import com.example.mystudyapp.viewmodel.PronunciationViewModel
import com.example.mystudyapp.viewmodel.PronunciationViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PronunciationScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: PronunciationViewModel = viewModel(factory = PronunciationViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowInfo = rememberWindowInfo()

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "pronunciation")

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
                                text = uiState.lessonTitle.ifBlank { "Luyện phát âm" },
                                fontSize = if (windowInfo.isCompact) 14.sp else 16.sp,
                                lineHeight = if (windowInfo.isCompact) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )

                            if (!uiState.isLoading && uiState.pronunciationList.isNotEmpty() && !uiState.isFinished) {
                                val currentPos = uiState.currentIndex + 1
                                val totalPos = uiState.pronunciationList.size
                                Text(
                                    text = "$currentPos / $totalPos",
                                    fontSize = if (windowInfo.isCompact) 12.5.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LingoBlue,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (!uiState.isLoading && uiState.pronunciationList.isNotEmpty() && !uiState.isFinished) {
                            Spacer(Modifier.height(10.dp))
                            val progressRatio = (uiState.currentIndex + 1).toFloat() / uiState.pronunciationList.size
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
            if (!uiState.isLoading && uiState.pronunciationList.isNotEmpty() && !uiState.isFinished) {
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
                            val isLast = uiState.currentIndex >= uiState.pronunciationList.size - 1
                            Button(
                                onClick = { viewModel.nextPronunciation() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLast) "Hoàn thành phát âm" else "Từ tiếp theo",
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
                        text = uiState.errorMessage ?: "An error occurred.",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Back to Lesson")
                    }
                }
            }
        } else if (uiState.pronunciationList.isEmpty()) {
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
                        text = "No pronunciation items available for this lesson.",
                        fontSize = 16.sp,
                        color = LingoGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Back to Lesson")
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
                            text = "Hoàn thành bài phát âm!",
                            fontSize = if (windowInfo.isCompact) 18.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Đã hoàn thành ${uiState.completedCount} / ${uiState.pronunciationList.size} từ phát âm.",
                            fontSize = if (windowInfo.isCompact) 13.sp else 15.sp,
                            color = LingoGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = { onBackToLesson() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Text(
                                text = "Về bài học",
                                fontSize = if (windowInfo.isCompact) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Active Pronunciation Card View
            val currentPronunciation = uiState.pronunciationList.getOrNull(uiState.currentIndex)
            if (currentPronunciation != null) {
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
                            targetState = currentPronunciation,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "pronun_card_transition"
                        ) { item ->
                            PronunciationCard(
                                pronunciation = item,
                                isCompact = windowInfo.isCompact
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PronunciationCard(
    pronunciation: PronunciationUiModel,
    isCompact: Boolean = false
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LUYỆN PHÁT ÂM",
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingoGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (pronunciation.targetScore > 0) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Mục tiêu: ${pronunciation.targetScore}%",
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (isCompact) 16.dp else 24.dp))

            Text(
                text = pronunciation.word,
                fontSize = if (isCompact) 26.sp else 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center
            )

            if (pronunciation.phonetic.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = pronunciation.phonetic,
                        fontSize = if (isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F766E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(if (isCompact) 18.dp else 24.dp))

            // Practice container
            Surface(
                color = Color(0xFFF8FAFF),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LingoGreen.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(if (isCompact) 42.dp else 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = LingoGreen,
                            modifier = Modifier.padding(if (isCompact) 10.dp else 12.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Lắng nghe & Phát âm",
                            fontSize = if (isCompact) 14.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Luyện nói từ to rõ ràng",
                            fontSize = if (isCompact) 11.5.sp else 12.sp,
                            color = LingoGray
                        )
                    }
                }
            }
        }
    }
}
