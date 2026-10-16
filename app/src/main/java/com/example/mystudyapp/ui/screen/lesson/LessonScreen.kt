package com.example.mystudyapp.ui.screen.lesson

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.ActiveStudyTimeTracker
import com.example.mystudyapp.ui.theme.CategoryGrammarBg
import com.example.mystudyapp.ui.theme.CategoryListeningBg
import com.example.mystudyapp.ui.theme.CategoryPronunBg
import com.example.mystudyapp.ui.theme.CategoryVocabBg
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGrayLight
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.ui.theme.LingoPink
import com.example.mystudyapp.ui.theme.LingoPurple
import com.example.mystudyapp.ui.theme.LingoYellow
import com.example.mystudyapp.viewmodel.LessonViewModel
import com.example.mystudyapp.viewmodel.LessonViewModelFactory

@Composable
fun LessonScreen(
    lessonId: String,
    onBackClick: () -> Unit = {},
    onNavigateToVocabulary: () -> Unit = {},
    onNavigateToGrammar: () -> Unit = {},
    onNavigateToListening: () -> Unit = {},
    onNavigateToPronunciation: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    viewModel: LessonViewModel = viewModel(factory = LessonViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActiveStudyTimeTracker(lessonId = lessonId)

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.padding(8.dp),
                            tint = Color(0xFF1E3A8A)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = uiState.title.ifBlank { "Chi tiết bài học" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorMessage == null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val nextAction = when {
                            !uiState.vocabularyCompleted -> onNavigateToVocabulary
                            !uiState.grammarCompleted -> onNavigateToGrammar
                            !uiState.listeningCompleted -> onNavigateToListening
                            !uiState.quizCompleted -> onNavigateToQuiz
                            else -> onNavigateToVocabulary
                        }

                        Button(
                            onClick = { nextAction() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isCompleted) LingoGreen else LingoBlue
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.isCompleted) "Ôn tập bài học" else "Bắt đầu bài học",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
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
                Text(
                    text = uiState.errorMessage ?: "Không thể tải bài học.",
                    fontSize = 16.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Lesson Header Card
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFDCEEFF),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = "Bài ${uiState.order}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (uiState.isCompleted) {
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = LingoGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Đã hoàn thành (+${uiState.xpReward} XP)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LingoGreen
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = uiState.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )

                            if (uiState.description.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = uiState.description,
                                    fontSize = 14.sp,
                                    color = LingoGray,
                                    lineHeight = 20.sp
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InfoChip(emoji = "⏱️", text = "${uiState.estimatedMinutes} phút")
                                InfoChip(emoji = "⭐", text = "+${uiState.xpReward} XP")
                            }

                            Spacer(Modifier.height(16.dp))

                            // Progress bar
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tiến độ bài học",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LingoGray
                                    )
                                    Text(
                                        text = "${uiState.progressPercentage}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isCompleted) LingoGreen else LingoBlue
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uiState.progressPercentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (uiState.isCompleted) LingoGreen else LingoBlue,
                                    trackColor = LingoGrayLight,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Các kỹ năng cần học",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ContentOverviewCard(
                            title = "Từ vựng",
                            countText = "${uiState.vocabularyCount} từ vựng",
                            icon = Icons.Default.AutoStories,
                            bgColor = CategoryVocabBg,
                            accentColor = LingoPink,
                            isCompleted = uiState.vocabularyCompleted,
                            onClick = onNavigateToVocabulary
                        )

                        ContentOverviewCard(
                            title = "Ngữ pháp",
                            countText = "${uiState.grammarCount} chủ đề",
                            icon = Icons.Default.School,
                            bgColor = CategoryGrammarBg,
                            accentColor = LingoYellow,
                            isCompleted = uiState.grammarCompleted,
                            onClick = onNavigateToGrammar
                        )

                        ContentOverviewCard(
                            title = "Luyện nghe",
                            countText = "${uiState.listeningCount} bài nghe",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            bgColor = CategoryListeningBg,
                            accentColor = LingoPurple,
                            isCompleted = uiState.listeningCompleted,
                            onClick = onNavigateToListening
                        )

                        ContentOverviewCard(
                            title = "Phát âm (Tạm ẩn)",
                            countText = "Bỏ qua cho bài học này",
                            icon = Icons.Default.Mic,
                            bgColor = Color(0xFFF1F5F9),
                            accentColor = Color(0xFF94A3B8),
                            isCompleted = false,
                            onClick = {}
                        )

                        ContentOverviewCard(
                            title = "Bài kiểm tra",
                            countText = "${uiState.quizCount} câu hỏi",
                            icon = Icons.Default.Psychology,
                            bgColor = Color(0xFFF1F5F9),
                            accentColor = LingoBlue,
                            isCompleted = uiState.quizCompleted,
                            onClick = onNavigateToQuiz
                        )
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun InfoChip(emoji: String, text: String) {
    Surface(
        color = Color(0xFFF8FAFF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

@Composable
private fun ContentOverviewCard(
    title: String,
    countText: String,
    icon: ImageVector,
    bgColor: Color,
    accentColor: Color,
    isCompleted: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = countText,
                    fontSize = 13.sp,
                    color = LingoGray
                )
            }

            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = LingoGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
