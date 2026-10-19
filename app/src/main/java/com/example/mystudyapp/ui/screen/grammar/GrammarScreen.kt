package com.example.mystudyapp.ui.screen.grammar

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.GrammarViewModel
import com.example.mystudyapp.viewmodel.GrammarViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GrammarScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: GrammarViewModel = viewModel(factory = GrammarViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Text to Speech engine for native pronunciation on example sentences
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

    fun speakSentence(sentence: String) {
        tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "grammar_slide_tts")
    }

    // Active Study Time Tracker
    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "grammar")

    // Topic selection state
    var selectedGrammarIndex by remember { mutableIntStateOf(0) }
    val currentGrammarIndex = selectedGrammarIndex.coerceIn(0, (uiState.grammarList.size - 1).coerceAtLeast(0))
    val selectedGrammar = uiState.grammarList.getOrNull(currentGrammarIndex)

    // Generate exactly 5 pedagogical slides for the selected grammar topic
    val slides = remember(selectedGrammar) {
        selectedGrammar?.let { GrammarSlideGenerator.generateSlides(it) } ?: emptyList()
    }

    val pagerState = rememberPagerState(initialPage = 0) { slides.size.coerceAtLeast(1) }

    // Reset to first slide when switching grammar topics
    LaunchedEffect(currentGrammarIndex) {
        if (pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

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

                    Spacer(Modifier.width(14.dp))

                    Text(
                        text = if (uiState.showQuickQuiz) {
                            "Kiểm tra nhanh Ngữ pháp (5 câu)"
                        } else {
                            uiState.lessonTitle.ifBlank { "Ngữ pháp" }
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.weight(1f)
                    )

                    if (!uiState.showQuickQuiz && !uiState.isFinished && slides.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "Slide ${pagerState.currentPage + 1} / ${slides.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LingoBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorMessage == null && !uiState.isFinished && !uiState.showQuickQuiz && slides.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isFirstSlide = pagerState.currentPage == 0
                        val isLastSlide = pagerState.currentPage == slides.size - 1

                        // Back Slide Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            enabled = !isFirstSlide,
                            modifier = Modifier
                                .weight(if (isLastSlide) 0.85f else 1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF334155),
                                disabledContainerColor = Color(0xFFF8FAFC),
                                disabledContentColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Quay lại", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Next Slide / Start Quiz Button
                        if (!isLastSlide) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LingoBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Tiếp theo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.onFinishReadingContent() },
                                modifier = Modifier
                                    .weight(1.35f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LingoGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Làm bài kiểm tra (5 câu)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
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
            // 5-Question Quick Quiz View (Kept intact as requested)
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
            // Finished View (Kept intact as requested)
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
            // 5-Slide Grammar Learning Presentation
            if (slides.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chưa có nội dung ngữ pháp cho bài học này.",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onBackToLesson() },
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Text("Quay lại")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Topic Selector Chips (if multiple grammar topics exist in this lesson)
                    if (uiState.grammarList.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.grammarList.forEachIndexed { index, grammar ->
                                val isSelected = index == currentGrammarIndex
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGrammarIndex = index },
                                    label = {
                                        Text(
                                            text = grammar.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LingoBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 5-Segment Story-Style Top Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        slides.indices.forEach { idx ->
                            val isCompletedOrActive = idx <= pagerState.currentPage
                            val segmentColor by animateColorAsState(
                                targetValue = if (isCompletedOrActive) LingoBlue else Color(0xFFE2E8F0),
                                animationSpec = tween(durationMillis = 250),
                                label = "segmentColor_$idx"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(segmentColor)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(idx)
                                        }
                                    }
                            )
                        }
                    }

                    // Slide Horizontal Pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        val slide = slides.getOrNull(page)
                        if (slide != null) {
                            GrammarSlideCard(
                                slide = slide,
                                onSpeak = ::speakSentence,
                                onGoToQuiz = { viewModel.onFinishReadingContent() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarSlideCard(
    slide: GrammarSlide,
    onSpeak: (String) -> Unit,
    onGoToQuiz: () -> Unit
) {
    val scrollState = rememberScrollState()

    val (badgeBg, badgeTextColor, categoryIcon) = when (slide.category) {
        SlideCategory.OVERVIEW -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Default.School)
        SlideCategory.FORMULA -> Triple(Color(0xFFF0FDF4), Color(0xFF15803D), Icons.Default.AutoAwesome)
        SlideCategory.USAGE -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Category)
        SlideCategory.EXAMPLES -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), Icons.AutoMirrored.Filled.VolumeUp)
        SlideCategory.SUMMARY -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Icons.Default.Lightbulb)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFEEF2F6)),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Category Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = slide.category.badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeTextColor
                            )
                        }
                    }

                    Text(
                        text = slide.category.stepLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Slide Title & Subtitle
                Text(
                    text = slide.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 28.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = slide.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                // Slide Content by Category
                when (slide.category) {
                    SlideCategory.OVERVIEW -> {
                        OverviewSlideContent(slide = slide)
                    }
                    SlideCategory.FORMULA -> {
                        FormulaSlideContent(slide = slide)
                    }
                    SlideCategory.USAGE -> {
                        UsageSlideContent(slide = slide)
                    }
                    SlideCategory.EXAMPLES -> {
                        ExamplesSlideContent(slide = slide, onSpeak = onSpeak)
                    }
                    SlideCategory.SUMMARY -> {
                        SummarySlideContent(slide = slide, onGoToQuiz = onGoToQuiz)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OverviewSlideContent(slide: GrammarSlide) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Concept explanation card
        Surface(
            color = Color(0xFFF8FAFF),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Khái niệm cốt lõi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LingoBlue
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = slide.overviewText,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 22.sp
                )
            }
        }

        // Key Points
        if (slide.keyPoints.isNotEmpty()) {
            Text(
                text = "Mục tiêu bài học cần nắm vững:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                slide.keyPoints.forEach { point ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LingoGreen,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = point,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        // Swipe hint banner
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Vuốt sang trái hoặc bấm 'Tiếp theo' để xem công thức chuẩn.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun FormulaSlideContent(slide: GrammarSlide) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Các mẫu câu & Công thức chuẩn:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        slide.formulas.forEach { formula ->
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = formula.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formula.pattern,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = formula.explanation,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageSlideContent(slide: GrammarSlide) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Áp dụng theo từng ngữ cảnh thực tế:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        slide.usageItems.forEach { item ->
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )

                    if (item.bulletPoints.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.bulletPoints.forEach { bp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = bp,
                                        fontSize = 13.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamplesSlideContent(
    slide: GrammarSlide,
    onSpeak: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            color = Color(0xFFF3E8FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF7E22CE),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Bấm vào biểu tượng loa để nghe phát âm chuẩn bản xứ.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B21A8)
                )
            }
        }

        slide.examples.forEach { example ->
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = example.english,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = example.vietnamese,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )

                        if (!example.note.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Ghi chú: ${example.note}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onSpeak(example.english) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = LingoBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySlideContent(
    slide: GrammarSlide,
    onGoToQuiz: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Tips
        if (slide.tips.isNotEmpty()) {
            Text(
                text = "Mẹo ghi nhớ nhanh:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slide.tips.forEach { tip ->
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = tip,
                                fontSize = 13.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        // Common Mistakes
        if (slide.commonMistakes.isNotEmpty()) {
            Text(
                text = "Lỗi sai thường gặp cần tránh:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            slide.commonMistakes.forEach { (wrong, correct) ->
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Tránh: $wrong",
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = LingoGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Nên dùng: $correct",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }
        }

        // Call-to-action Test Readiness Box
        Surface(
            color = Color(0xFFECFDF5),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hoàn thành 5 slide lý thuyết!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Làm ngay bài kiểm tra 5 câu để củng cố kiến thức và tích lũy sao bài học.",
                    fontSize = 13.sp,
                    color = Color(0xFF047857),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onGoToQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LingoGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Làm bài kiểm tra (5 câu)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
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
