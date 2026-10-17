package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.R
import com.example.mystudyapp.data.model.Lesson
import com.example.mystudyapp.data.model.LessonProgress
import com.example.mystudyapp.data.model.Unit as LearningUnit
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.LessonItemUiModel
import com.example.mystudyapp.viewmodel.UnitLessonsViewModel
import com.example.mystudyapp.viewmodel.UnitLessonsViewModelFactory
import kotlinx.coroutines.delay

// =============================================================================
// TREE ROW DATA SPECIFICATION (Alternating 1 - 2 - 1 - 2)
// =============================================================================
private sealed class TreeRow {
    data class Single(val item: LessonItemUiModel, val overallIndex: Int) : TreeRow()
    data class Pair(
        val first: LessonItemUiModel,
        val second: LessonItemUiModel,
        val firstIndex: Int,
        val secondIndex: Int
    ) : TreeRow()
}

private fun groupLessonsIntoTree(lessons: List<LessonItemUiModel>): List<TreeRow> {
    val rows = mutableListOf<TreeRow>()
    var i = 0
    var isSingle = true
    while (i < lessons.size) {
        if (isSingle) {
            rows.add(TreeRow.Single(lessons[i], i))
            i += 1
            isSingle = false
        } else {
            if (i + 1 < lessons.size) {
                rows.add(TreeRow.Pair(lessons[i], lessons[i + 1], i, i + 1))
                i += 2
            } else {
                rows.add(TreeRow.Single(lessons[i], i))
                i += 1
            }
            isSingle = true
        }
    }
    return rows
}

// =============================================================================
// MAIN UNIT LESSONS SCREEN (Exact Match to User Mockup)
// =============================================================================
@Composable
fun UnitLessonsScreen(
    unitId: String,
    onBackClick: () -> Unit = {},
    onSelectLesson: (String) -> Unit = {},
    viewModel: UnitLessonsViewModel = viewModel(factory = UnitLessonsViewModelFactory(unitId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Preserve ON_RESUME refresh logic
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Modal popup state for clicked lesson
    var popupLessonItem by remember { mutableStateOf<Pair<LessonItemUiModel, Int>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Sky Background
        Image(
            painter = painterResource(id = R.drawable.bg_home_sky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Floating Top Bar matching mockup
                UnitTopBar(
                    unit = uiState.unit,
                    lessonsCount = uiState.lessons.size,
                    onBackClick = onBackClick
                )
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
                val lessons = uiState.lessons
                val treeRows = remember(lessons) { groupLessonsIntoTree(lessons) }

                val currentActiveIndex = remember(lessons) {
                    val idx = lessons.indexOfFirst { it.progress?.completed != true }
                    if (idx >= 0) idx else lessons.size - 1
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Checkpoint Header Card
                    item {
                        EntranceWrapper(delayMillis = 100) {
                            UnitCheckpointCard(unit = uiState.unit)
                        }
                    }

                    // Stepping path from Checkpoint Card to Row 0
                    item {
                        PathConnectorVertical(height = 36)
                    }

                    // Skill Tree Rows
                    items(treeRows.size) { rowIndex ->
                        val row = treeRows[rowIndex]
                        val isLastRow = rowIndex == treeRows.size - 1

                        EntranceWrapper(delayMillis = 180 + (rowIndex * 70)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                when (row) {
                                    is TreeRow.Single -> {
                                        val idx = row.overallIndex
                                        val isCompleted = row.item.progress?.completed == true
                                        val isUnlocked = idx == 0 || (idx > 0 && lessons[idx - 1].progress?.completed == true) || (row.item.progress?.progress ?: 0) > 0
                                        val isCurrent = idx == currentActiveIndex && !isCompleted
                                        val completedSkills = countCompletedSkills(row.item.progress)

                                        // Single Node Frosted Glass Card
                                        Surface(
                                            shape = RoundedCornerShape(26.dp),
                                            color = Color.White.copy(alpha = 0.88f),
                                            shadowElevation = 3.dp,
                                            border = BorderStroke(1.5.dp, Color.White),
                                            modifier = Modifier.width(152.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)
                                            ) {
                                                LessonCandyNode(
                                                    lesson = row.item.lesson,
                                                    completedSkillsCount = completedSkills,
                                                    isCompleted = isCompleted,
                                                    isUnlocked = isUnlocked,
                                                    isCurrent = isCurrent,
                                                    onClick = { popupLessonItem = Pair(row.item, idx) }
                                                )

                                                Spacer(Modifier.height(8.dp))

                                                // Pill label for lesson title
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color.White.copy(alpha = 0.96f),
                                                    shadowElevation = 1.dp,
                                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                                ) {
                                                    Text(
                                                        text = row.item.lesson.titleVi ?: row.item.lesson.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E3A8A),
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    is TreeRow.Pair -> {
                                        val idx1 = row.firstIndex
                                        val isCompleted1 = row.first.progress?.completed == true
                                        val isUnlocked1 = idx1 == 0 || (idx1 > 0 && lessons[idx1 - 1].progress?.completed == true) || (row.first.progress?.progress ?: 0) > 0
                                        val isCurrent1 = idx1 == currentActiveIndex && !isCompleted1
                                        val skills1 = countCompletedSkills(row.first.progress)

                                        val idx2 = row.secondIndex
                                        val isCompleted2 = row.second.progress?.completed == true
                                        val isUnlocked2 = idx2 == 0 || (idx2 > 0 && lessons[idx2 - 1].progress?.completed == true) || (row.second.progress?.progress ?: 0) > 0
                                        val isCurrent2 = idx2 == currentActiveIndex && !isCompleted2
                                        val skills2 = countCompletedSkills(row.second.progress)

                                        // Wide Frosted Glass Card for 2 Nodes
                                        Surface(
                                            shape = RoundedCornerShape(28.dp),
                                            color = Color.White.copy(alpha = 0.88f),
                                            shadowElevation = 3.dp,
                                            border = BorderStroke(1.5.dp, Color.White),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left Node Column
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.width(136.dp)
                                                ) {
                                                    LessonCandyNode(
                                                        lesson = row.first.lesson,
                                                        completedSkillsCount = skills1,
                                                        isCompleted = isCompleted1,
                                                        isUnlocked = isUnlocked1,
                                                        isCurrent = isCurrent1,
                                                        onClick = { popupLessonItem = Pair(row.first, idx1) }
                                                    )

                                                    Spacer(Modifier.height(8.dp))

                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color.White.copy(alpha = 0.96f),
                                                        shadowElevation = 1.dp,
                                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                                    ) {
                                                        Text(
                                                            text = row.first.lesson.titleVi ?: row.first.lesson.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1E3A8A),
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                            lineHeight = 15.sp,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                // Right Node Column
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.width(136.dp)
                                                ) {
                                                    LessonCandyNode(
                                                        lesson = row.second.lesson,
                                                        completedSkillsCount = skills2,
                                                        isCompleted = isCompleted2,
                                                        isUnlocked = isUnlocked2,
                                                        isCurrent = isCurrent2,
                                                        onClick = { popupLessonItem = Pair(row.second, idx2) }
                                                    )

                                                    Spacer(Modifier.height(8.dp))

                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color.White.copy(alpha = 0.96f),
                                                        shadowElevation = 1.dp,
                                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                                    ) {
                                                        Text(
                                                            text = row.second.lesson.titleVi ?: row.second.lesson.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isUnlocked2) Color(0xFF1E3A8A) else Color(0xFF64748B),
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                            lineHeight = 15.sp,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Stepping Path Connector to next row
                                if (!isLastRow) {
                                    val nextRow = treeRows[rowIndex + 1]
                                    when {
                                        row is TreeRow.Single && nextRow is TreeRow.Pair -> {
                                            PathConnectorSingleToPair(height = 36)
                                        }
                                        row is TreeRow.Pair && nextRow is TreeRow.Single -> {
                                            PathConnectorPairToSingle(height = 36)
                                        }
                                        else -> {
                                            PathConnectorVertical(height = 36)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Duolingo-style Lesson Detail Dialog
        popupLessonItem?.let { (item, idx) ->
            val lesson = item.lesson
            val progress = item.progress
            val isCompleted = progress?.completed == true
            val isUnlocked = idx == 0 || (idx > 0 && uiState.lessons[idx - 1].progress?.completed == true) || (progress?.progress ?: 0) > 0

            LessonDetailDialog(
                lesson = lesson,
                progress = progress,
                isCompleted = isCompleted,
                isUnlocked = isUnlocked,
                onDismiss = { popupLessonItem = null },
                onStartLesson = {
                    popupLessonItem = null
                    onSelectLesson(lesson.id)
                }
            )
        }
    }
}

// =============================================================================
// FLOATING TOP BAR (Matching Mockup)
// =============================================================================
@Composable
private fun UnitTopBar(
    unit: LearningUnit?,
    lessonsCount: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button: White circle with subtle shadow
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier
                .size(42.dp)
                .clickable { onBackClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Unit Tag & Title
        Column(modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = "UNIT ${unit?.order ?: 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = unit?.titleVi ?: unit?.title ?: "Bài 1: Chào hỏi & Giới thiệu",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Lessons count pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$lessonsCount bài",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

// =============================================================================
// CHECKPOINT CARD (Unit Header from Mockup)
// =============================================================================
@Composable
private fun UnitCheckpointCard(unit: LearningUnit?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main Frosted Glass Card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            border = BorderStroke(1.5.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = unit?.titleVi ?: unit?.title ?: "Bài 1: Chào hỏi & Giới thiệu",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center
                )

                val desc = unit?.descriptionVi ?: unit?.description ?: "Học cách chào hỏi, giới thiệu bản thân và trò chuyện thân mật."
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Chip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "~${unit?.estimatedMinutes ?: 24} phút",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    // Skills Chip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "5 Kỹ năng",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // Top-center badge: "UNIT 1"
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF3B82F6),
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "UNIT ${unit?.order ?: 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// CANDY 3D LESSON NODE (Glossy Gold / Ice Lock matching mockup)
// =============================================================================
@Composable
private fun LessonCandyNode(
    lesson: Lesson,
    completedSkillsCount: Int,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val currentScale = if (isCurrent) pulseScale else 1f

    Box(
        modifier = Modifier
            .size(92.dp)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Outer Progress Ring Canvas (explicitly centered within 86.dp)
        Canvas(modifier = Modifier.size(86.dp)) {
            val strokeWidth = 5.dp.toPx()
            val halfStroke = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val arcTopLeft = Offset(halfStroke, halfStroke)

            // Track Ring
            drawArc(
                color = if (!isUnlocked) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.75f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = when {
                isCompleted -> 360f
                completedSkillsCount > 0 -> (completedSkillsCount / 5f) * 360f
                else -> 0f
            }

            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFDE047),
                            Color(0xFFF59E0B),
                            Color(0xFFEA580C),
                            Color(0xFFFDE047)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // 3D Pushable Button Disc - Centered symmetrically inside progress ring
        val (topGradient, bevelColor) = when {
            !isUnlocked -> Pair(
                listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                Color(0xFF94A3B8)
            )
            isCompleted -> Pair(
                listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                Color(0xFFD97706)
            )
            else -> Pair(
                listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                Color(0xFFD97706)
            )
        }

        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Bottom 3D bevel shadow (offset +2.dp so disc + shadow is vertically centered)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = 2.dp)
                    .background(bevelColor, CircleShape)
            )

            // Top disc surface (offset -2.dp so disc + shadow is vertically centered)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = (-2).dp)
                    .background(
                        brush = Brush.verticalGradient(topGradient),
                        shape = CircleShape
                    )
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Crescent glossy highlight on top (subtle, non-obstructive)
                Box(
                    modifier = Modifier
                        .size(52.dp, 16.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 2.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                // Main icon (optically centered)
                when {
                    !isUnlocked -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier
                                .size(26.dp)
                                .offset(y = (-1).dp)
                        )
                    }
                    isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .offset(y = (-0.5).dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Active Lesson",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .offset(y = (-1.5).dp)
                        )
                    }
                }
            }
        }

        // Bottom-right badge
        if (isUnlocked) {
            Surface(
                shape = CircleShape,
                color = if (isCompleted) Color(0xFFDCFCE7) else Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, if (isCompleted) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Hoàn thành",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = "$completedSkillsCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// CONNECTING CYAN DASHED PATHS (Matching Mockup)
// =============================================================================
@Composable
private fun PathConnectorVertical(height: Int = 36) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        val centerX = size.width / 2f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = dashEffect
        )
    }
}

@Composable
private fun PathConnectorSingleToPair(height: Int = 36) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        val centerX = size.width / 2f
        val leftX = size.width * 0.32f
        val rightX = size.width * 0.68f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

        // Left branch
        val leftPath = Path().apply {
            moveTo(centerX, 0f)
            cubicTo(
                centerX, size.height * 0.45f,
                leftX, size.height * 0.55f,
                leftX, size.height
            )
        }
        drawPath(
            path = leftPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
        )

        // Right branch
        val rightPath = Path().apply {
            moveTo(centerX, 0f)
            cubicTo(
                centerX, size.height * 0.45f,
                rightX, size.height * 0.55f,
                rightX, size.height
            )
        }
        drawPath(
            path = rightPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
        )
    }
}

@Composable
private fun PathConnectorPairToSingle(height: Int = 36) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        val centerX = size.width / 2f
        val leftX = size.width * 0.32f
        val rightX = size.width * 0.68f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

        val leftPath = Path().apply {
            moveTo(leftX, 0f)
            cubicTo(
                leftX, size.height * 0.45f,
                centerX, size.height * 0.55f,
                centerX, size.height
            )
        }
        drawPath(
            path = leftPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
        )

        val rightPath = Path().apply {
            moveTo(rightX, 0f)
            cubicTo(
                rightX, size.height * 0.45f,
                centerX, size.height * 0.55f,
                centerX, size.height
            )
        }
        drawPath(
            path = rightPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashEffect)
        )
    }
}

// =============================================================================
// LESSON DETAIL POPUP DIALOG (Duolingo-style Cloud Card)
// =============================================================================
@Composable
private fun LessonDetailDialog(
    lesson: Lesson,
    progress: LessonProgress?,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    onDismiss: () -> Unit,
    onStartLesson: () -> Unit
) {
    val vocabDone = progress?.vocabularyCompleted == true
    val grammarDone = progress?.grammarCompleted == true
    val listeningDone = progress?.listeningCompleted == true
    val pronunDone = progress?.pronunciationCompleted == true
    val quizDone = progress?.quizCompleted == true
    val completedCount = countCompletedSkills(progress)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(1.5.dp, Color(0xFFE0F2FE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Order & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "BÀI ${lesson.order}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCompleted) Color(0xFF166534) else Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Lesson Title
                Text(
                    text = lesson.titleVi ?: lesson.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center
                )

                val desc = lesson.descriptionVi ?: lesson.description
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 5 Skills Status Row
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiến độ 5 kỹ năng: $completedCount/5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SkillStatusIcon(name = "Từ vựng", isDone = vocabDone, icon = Icons.Default.AutoStories)
                            SkillStatusIcon(name = "Ngữ pháp", isDone = grammarDone, icon = Icons.Default.School)
                            SkillStatusIcon(name = "Nghe", isDone = listeningDone, icon = Icons.AutoMirrored.Filled.VolumeUp)
                            SkillStatusIcon(name = "Phát âm", isDone = pronunDone, icon = Icons.Default.Mic)
                            SkillStatusIcon(name = "Quiz", isDone = quizDone, icon = Icons.Default.Psychology)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Action CTA Button
                if (!isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Hoàn thành bài trước để mở khóa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onStartLesson,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) LingoGreen else LingoBlue
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCompleted) "Ôn tập lại bài học (Đã nhận +${lesson.xpReward} XP)" else "Bắt đầu học (Hoàn thành nhận +${lesson.xpReward} XP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillStatusIcon(name: String, isDone: Boolean, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isDone) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else Color(0xFFCBD5E1)),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isDone) Icons.Default.Check else icon,
                    contentDescription = name,
                    tint = if (isDone) Color(0xFF166534) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDone) Color(0xFF166534) else Color(0xFF64748B)
        )
    }
}

private fun countCompletedSkills(progress: LessonProgress?): Int {
    if (progress == null) return 0
    var count = 0
    if (progress.vocabularyCompleted) count++
    if (progress.grammarCompleted) count++
    if (progress.listeningCompleted) count++
    if (progress.pronunciationCompleted) count++
    if (progress.quizCompleted) count++
    return count
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
        animationSpec = tween(400),
        label = "entrance_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(400, easing = EaseOutQuart),
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
