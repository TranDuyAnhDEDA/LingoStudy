package com.example.mystudyapp.ui.screen.grammar

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.mystudyapp.viewmodel.GrammarUiModel
import com.example.mystudyapp.viewmodel.GrammarViewModel
import com.example.mystudyapp.viewmodel.GrammarViewModelFactory

@Composable
fun GrammarScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: GrammarViewModel = viewModel(factory = GrammarViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val isScrolledToBottom by remember {
        derivedStateOf {
            scrollState.value >= (scrollState.maxValue - 80).coerceAtLeast(0) || !scrollState.canScrollForward
        }
    }

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "grammar")

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
                            .clickable { onBackToLesson() }
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
                        text = if (uiState.showQuickQuiz) "Kiểm tra nhanh Ngữ pháp (5 câu)" else (uiState.lessonTitle.ifBlank { "Ngữ pháp" }),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorMessage == null && !uiState.isFinished && !uiState.showQuickQuiz) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.onFinishReadingContent() },
                            enabled = isScrolledToBottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LingoBlue,
                                disabledContainerColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isScrolledToBottom) "Đã đọc xong · Làm bài kiểm tra" else "Cuộn xuống cuối để tiếp tục",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isScrolledToBottom) {
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
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
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
        } else if (uiState.showQuickQuiz) {
            // 5-Question Quick Quiz View
            val questions = uiState.quickQuizQuestions
            val currentIndex = uiState.currentQuizIndex
            val currentQuiz = questions.getOrNull(currentIndex)

            if (currentQuiz != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(10.dp)) {
                                Text(
                                    text = "Câu ${currentIndex + 1} / ${questions.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(text = "Kiểm tra ngữ pháp", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Question card
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = currentQuiz.questionVi?.ifBlank { currentQuiz.question } ?: currentQuiz.question,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )

                                Spacer(Modifier.height(16.dp))

                                currentQuiz.options.forEach { option ->
                                    val isSelected = uiState.selectedAnswers[currentIndex] == option
                                    Surface(
                                        color = if (isSelected) Color(0xFFE0E7FF) else Color(0xFFF8FAFF),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { viewModel.selectQuizAnswer(currentIndex, option) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.selectQuizAnswer(currentIndex, option) },
                                                colors = RadioButtonDefaults.colors(selectedColor = LingoBlue)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = option,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = Color(0xFF1E3A8A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentIndex > 0) {
                            Button(
                                onClick = { viewModel.prevQuizQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Câu trước")
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        if (currentIndex < questions.size - 1) {
                            Button(
                                onClick = { viewModel.nextQuizQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Câu tiếp")
                            }
                        } else {
                            val allAnswered = uiState.selectedAnswers.size >= questions.size
                            Button(
                                onClick = { viewModel.submitQuickQuiz() },
                                enabled = allAnswered,
                                colors = ButtonDefaults.buttonColors(containerColor = LingoGreen, disabledContainerColor = Color(0xFFCBD5E1)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Nộp bài")
                            }
                        }
                    }
                }
            }
        } else if (uiState.isFinished) {
            // Finished View
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
                            modifier = Modifier.size(150.dp),
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
                            text = "Hoàn thành bài Ngữ pháp!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Điểm bài kiểm tra: ${uiState.quizScore} / 5 câu đúng",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.retakeQuickQuiz() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155))
                            ) {
                                Text("Làm lại quiz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onBackToLesson() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                            ) {
                                Text("Về bài học", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Active Grammar Reading Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.grammarList.forEach { grammar ->
                    GrammarCard(grammar = grammar)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GrammarCard(grammar: GrammarUiModel) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "BÀI ĐỌC NGỮ PHÁP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = grammar.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = grammar.explanation,
                fontSize = 14.sp,
                color = Color(0xFF334155),
                lineHeight = 22.sp
            )

            if (grammar.examples.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFF))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Mẫu câu ví dụ:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue
                        )
                        Spacer(Modifier.height(8.dp))
                        grammar.examples.forEachIndexed { idx, example ->
                            val trans = grammar.examplesVi.getOrNull(idx)
                            Text(
                                text = "• $example",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            if (!trans.isNullOrBlank()) {
                                Text(
                                    text = "  ($trans)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
