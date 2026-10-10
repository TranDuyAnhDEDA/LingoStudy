package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.UnitLessonsViewModel
import com.example.mystudyapp.viewmodel.UnitLessonsViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun UnitLessonsScreen(
    unitId: String,
    onBackClick: () -> Unit = {},
    onSelectLesson: (String) -> Unit = {},
    viewModel: UnitLessonsViewModel = viewModel(factory = UnitLessonsViewModelFactory(unitId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                        text = uiState.unit?.title ?: "Unit Roadmap",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Compact Unit Header & Skill Key Legend
                item {
                    EntranceWrapper(delayMillis = 100) {
                        val unit = uiState.unit
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFFFEF3C7), Color.White)
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = Color(0xFFFDE68A),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "CIRCULAR LEARNING ROADMAP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MetadataChip(
                                            icon = Icons.Default.Schedule,
                                            iconTint = Color(0xFF1E3A8A),
                                            text = "${unit?.estimatedMinutes ?: 24}m"
                                        )
                                        MetadataChip(
                                            icon = Icons.AutoMirrored.Filled.MenuBook,
                                            iconTint = LingoBlue,
                                            text = "${uiState.lessons.size} Lessons"
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = unit?.title ?: "Unit Roadmap",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )

                                if (unit?.description?.isNotBlank() == true) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = unit.description,
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                // Skill Key Legend
                                Surface(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "5 Skill Satellites Around Every Lesson:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SkillLegendTag(icon = Icons.Default.AutoStories, label = "Vocab", tint = Color(0xFFEC4899))
                                            SkillLegendTag(icon = Icons.Default.School, label = "Grammar", tint = Color(0xFFD97706))
                                            SkillLegendTag(icon = Icons.AutoMirrored.Filled.VolumeUp, label = "Listening", tint = Color(0xFF7C3AED))
                                            SkillLegendTag(icon = Icons.Default.Mic, label = "Pronun", tint = LingoGreen)
                                            SkillLegendTag(icon = Icons.Default.Psychology, label = "Quiz", tint = LingoBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Lesson Roadmap",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                // Roadmap Nodes with 5 Skill Stars
                items(uiState.lessons.size) { index ->
                    val item = uiState.lessons[index]
                    val lesson = item.lesson
                    val progress = item.progress
                    val isLastItem = index == uiState.lessons.size - 1

                    EntranceWrapper(delayMillis = 200 + (index * 90)) {
                        RoadmapNodeRow(
                            index = index,
                            lesson = lesson,
                            progress = progress,
                            isLastItem = isLastItem,
                            onSelectLesson = { onSelectLesson(lesson.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillLegendTag(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
    }
}

@Composable
private fun RoadmapNodeRow(
    index: Int,
    lesson: Lesson,
    progress: LessonProgress?,
    isLastItem: Boolean,
    onSelectLesson: () -> Unit
) {
    val isCompleted = progress?.completed == true
    val isCurrent = !isCompleted && (progress?.progress ?: 0) > 0 || (index == 0 && progress == null)

    val vocabDone = progress?.vocabularyCompleted == true
    val grammarDone = progress?.grammarCompleted == true
    val listeningDone = progress?.listeningCompleted == true
    val pronunDone = progress?.pronunciationCompleted == true
    val quizDone = progress?.quizCompleted == true

    var completedSkillsCount = 0
    if (vocabDone) completedSkillsCount++
    if (grammarDone) completedSkillsCount++
    if (listeningDone) completedSkillsCount++
    if (pronunDone) completedSkillsCount++
    if (quizDone) completedSkillsCount++

    val nodeAlignmentLeft = index % 2 == 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!nodeAlignmentLeft) {
            // Text Card on Left
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                LessonRoadmapCard(
                    lesson = lesson,
                    completedSkillsCount = completedSkillsCount,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    onSelectLesson = onSelectLesson
                )
            }
        }

        // Central Circular Lesson Node with 5 Surrounding Stars Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(150.dp)
        ) {
            CircularLessonNodeWithStars(
                order = lesson.order,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                vocabDone = vocabDone,
                grammarDone = grammarDone,
                listeningDone = listeningDone,
                pronunDone = pronunDone,
                quizDone = quizDone,
                onClick = onSelectLesson
            )

            if (!isLastItem) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(Color(0xFFCBD5E1))
                )
            }
        }

        if (nodeAlignmentLeft) {
            // Text Card on Right
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                LessonRoadmapCard(
                    lesson = lesson,
                    completedSkillsCount = completedSkillsCount,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    onSelectLesson = onSelectLesson
                )
            }
        }
    }
}

@Composable
private fun CircularLessonNodeWithStars(
    order: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    vocabDone: Boolean,
    grammarDone: Boolean,
    listeningDone: Boolean,
    pronunDone: Boolean,
    quizDone: Boolean,
    onClick: () -> Unit
) {
    val nodeGradient = when {
        isCompleted -> Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        isCurrent -> Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        else -> Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
    }

    val borderBrush = when {
        isCompleted -> Brush.verticalGradient(listOf(Color.White, Color(0xFFA7F3D0)))
        isCurrent -> Brush.verticalGradient(listOf(Color.White, Color(0xFF93C5FD)))
        else -> Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
    }

    val nodeContentColor = when {
        isCompleted || isCurrent -> Color.White
        else -> Color(0xFF1E3A8A)
    }

    // Pulse animation for current lesson
    val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val currentScale = if (isCurrent) pulseScale else 1f

    Box(
        modifier = Modifier
            .size(150.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Spacious Orbital Satellite Ring (116.dp) encircling 68.dp node with clear breathing gap!
        Surface(
            modifier = Modifier.size(116.dp),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, if (isCurrent) Color(0xFF93C5FD).copy(alpha = 0.6f) else Color(0xFFE2E8F0))
        ) {}

        // Main 3D Bevel Circular Lesson Node (68.dp x 68.dp)
        Surface(
            shape = CircleShape,
            shadowElevation = if (isCurrent) 8.dp else 2.dp,
            modifier = Modifier
                .size(68.dp)
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                }
                .background(brush = nodeGradient, shape = CircleShape)
                .border(
                    border = BorderStroke(3.dp, borderBrush),
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = nodeGradient),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed Lesson $order",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = "$order",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = nodeContentColor
                    )
                }
            }
        }

        // 5 Compact Gemstone Skill Satellites (22.dp x 22.dp) placed at distance R = 58.dp
        // 13.dp clear breathing gap between 68.dp node edge and 22.dp star inner edge!
        // 1. Top (Vocabulary) -> (0, -58.dp)
        SkillStarIndicator(
            modifier = Modifier.offset(x = 0.dp, y = (-58).dp),
            isDone = vocabDone,
            icon = Icons.Default.AutoStories,
            skillName = "Vocabulary"
        )

        // 2. Upper Right (Grammar) -> (+55.dp, -18.dp)
        SkillStarIndicator(
            modifier = Modifier.offset(x = 55.dp, y = (-18).dp),
            isDone = grammarDone,
            icon = Icons.Default.School,
            skillName = "Grammar"
        )

        // 3. Lower Right (Listening) -> (+34.dp, +47.dp)
        SkillStarIndicator(
            modifier = Modifier.offset(x = 34.dp, y = 47.dp),
            isDone = listeningDone,
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            skillName = "Listening"
        )

        // 4. Lower Left (Pronunciation) -> (-34.dp, +47.dp)
        SkillStarIndicator(
            modifier = Modifier.offset(x = (-34).dp, y = 47.dp),
            isDone = pronunDone,
            icon = Icons.Default.Mic,
            skillName = "Pronunciation"
        )

        // 5. Upper Left (Quiz) -> (-55.dp, -18.dp)
        SkillStarIndicator(
            modifier = Modifier.offset(x = (-55).dp, y = (-18).dp),
            isDone = quizDone,
            icon = Icons.Default.Psychology,
            skillName = "Quiz"
        )
    }
}

@Composable
private fun SkillStarIndicator(
    modifier: Modifier = Modifier,
    isDone: Boolean,
    icon: ImageVector,
    skillName: String
) {
    val activeBrush = Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
    val inactiveBg = Color.White
    val starColor = if (isDone) Color.White else Color(0xFF64748B)
    val borderClr = if (isDone) Color(0xFFF59E0B) else Color(0xFFCBD5E1)

    Surface(
        color = if (isDone) Color(0xFFF59E0B) else inactiveBg,
        shape = CircleShape,
        shadowElevation = if (isDone) 3.dp else 1.dp,
        border = BorderStroke(1.5.dp, borderClr),
        modifier = modifier
            .size(22.dp)
            .semantics { contentDescription = "$skillName: ${if (isDone) "Completed" else "Not completed"}" }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = if (isDone) activeBrush else Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFF))), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.Star else icon,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun LessonRoadmapCard(
    lesson: Lesson,
    completedSkillsCount: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onSelectLesson: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectLesson() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (isCompleted) Color(0xFFDCFCE7) else if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "LESSON ${lesson.order}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) Color(0xFF166534) else if (isCurrent) LingoBlue else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = if (completedSkillsCount == 5) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$completedSkillsCount/5 Skills",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedSkillsCount == 5) Color(0xFF166534) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = lesson.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            if (lesson.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lesson.description,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSelectLesson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(19.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) LingoGreen else LingoBlue
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCompleted) "Review" else "Open Lesson",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(icon: ImageVector, iconTint: Color, text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

@Composable
private fun EntranceWrapper(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "entrance_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(500, easing = EaseOutQuart),
        label = "entrance_offsetY"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = offsetY
        }
    ) {
        content()
    }
}
