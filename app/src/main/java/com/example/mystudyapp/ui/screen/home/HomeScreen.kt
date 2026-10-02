package com.example.mystudyapp.ui.screen.home

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
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
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
import com.example.mystudyapp.viewmodel.HomeUiState
import com.example.mystudyapp.viewmodel.HomeViewModel
import com.example.mystudyapp.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNav by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        bottomBar = {
            HomeBottomNav(
                selectedIndex = selectedNav,
                onItemSelected = { 
                    selectedNav = it
                    when (it) {
                        0 -> onNavigate("home")
                        1 -> onNavigate("learn")
                        2 -> onNavigate("practice")
                        3 -> onNavigate("me")
                    }
                }
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
        } else if (uiState.errorMessage != null && !uiState.isAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Authentication error",
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
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { HeroSection(uiState = uiState) }

                item {
                    ContinueLearningCard(uiState = uiState, onNavigate = onNavigate, delayMillis = 200)
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    LearningCategoriesSection(uiState = uiState, onNavigate = onNavigate)
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    ProgressSection(uiState = uiState, delayMillis = 800)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// =============================================================================
// Hero Section
// =============================================================================

@Composable
private fun HeroSection(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFCBE7FF), Color(0xFFE2F3FF))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(30.dp))

            // Greeting & Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Greeting column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "\u2600\uFE0F", fontSize = 22.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Good morning,",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                    Text(
                        text = "${uiState.displayName.ifBlank { "Learner" }}!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 36.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Small steps every day,\nbig progress ahead! \uD83D\uDC99",
                        fontSize = 13.sp,
                        color = Color(0xFF3B82F6),
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Stat chips
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(emoji = "\uD83D\uDD25", value = uiState.streak.toString(), label = "Day streak")
                    LevelChip(
                        levelName = uiState.levelName,
                        currentXp = uiState.currentXp,
                        requiredXp = uiState.requiredXp
                    )
                }
            }
        }

        // Mascot
        MascotView(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(310.dp)
                .offset(x = 20.dp, y = 10.dp),
            expression = MascotExpression.HAPPY,
            enableAnimation = true
        )

        // Speech bubble
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 30.dp)
        ) {
            SpeechBubble(text = "Ready to\nlearn English?\n\uD83D\uDE42", delayMillis = 400)
        }
    }
}

@Composable
private fun StatChip(emoji: String, value: String, label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 16.sp
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun LevelChip(levelName: String, currentXp: Int, requiredXp: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "\u2B50", fontSize = 20.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = levelName.ifBlank { "Explorer" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 16.sp
                )
                Text(
                    text = "$currentXp / $requiredXp XP",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

// ─── Animated Speech Bubble ───────────────────────────────────────────────────
@Composable
private fun SpeechBubble(text: String, delayMillis: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "bubble_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "bubble_scale"
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd    = 24.dp,
            bottomStart = 24.dp,
            bottomEnd   = 4.dp
        ),
        shadowElevation = 4.dp,
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
            this.transformOrigin = TransformOrigin(1f, 1f)
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// =============================================================================
// Animated Continue Learning Card
// =============================================================================

@Composable
private fun ContinueLearningCard(
    uiState: HomeUiState,
    onNavigate: (String) -> Unit = {},
    delayMillis: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "card_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutQuart),
        label = "card_offsetY"
    )

    val currentLessonId = uiState.currentLessonId.ifBlank { "lesson_3_1_1" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offsetY
            }
            .clickable { onNavigate("lesson/$currentLessonId") },
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue Learning",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LingoBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(text = "\u2728", fontSize = 20.sp)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFDCEEFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "\u2708\uFE0F\uD83D\uDDFC", fontSize = 26.sp, lineHeight = 28.sp)
                        Spacer(Modifier.height(4.dp))
                        Surface(color = LingoBlue, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "Unit 1",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.currentLessonId.isNotBlank()) "Current Lesson" else "Lesson 1",
                        fontSize = 13.sp,
                        color = LingoGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = uiState.currentLessonTitle.ifBlank { "At the Airport" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Vocabulary \u00B7 ${if (uiState.currentLessonEstimatedMinutes > 0) uiState.currentLessonEstimatedMinutes else 8} min",
                        fontSize = 13.sp,
                        color = LingoGray
                    )
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(LingoBlue)
                        .clickable { onNavigate("lesson/$currentLessonId") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Continue lesson",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val progressRatio = if (uiState.totalLessons > 0) {
                uiState.completedLessons.toFloat() / uiState.totalLessons
            } else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LingoGreen,
                    trackColor = LingoGrayLight,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${uiState.completedLessons} / ${uiState.totalLessons}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LingoGray
                )
            }
        }
    }
}

// =============================================================================
// Animated Learning Categories Section
// =============================================================================

private data class CategoryInfo(
    val emoji: String,
    val name: String,
    val pct: Int,
    val barColor: Color,
    val bg: Color,
    val route: String
)

@Composable
private fun LearningCategoriesSection(
    uiState: HomeUiState,
    onNavigate: (String) -> Unit = {}
) {
    val currentLessonId = uiState.currentLessonId.ifBlank { "lesson_3_1_1" }
    val categories = listOf(
        CategoryInfo("\uD83D\uDCD6", "Vocabulary",    uiState.vocabularyProgress,    LingoPink,   CategoryVocabBg, "vocabulary/$currentLessonId"),
        CategoryInfo("\u270F\uFE0F",  "Grammar",       uiState.grammarProgress,       LingoYellow, CategoryGrammarBg, "grammar/$currentLessonId"),
        CategoryInfo("\uD83C\uDFA7", "Listening",     uiState.listeningProgress,     LingoPurple, CategoryListeningBg, "listening/$currentLessonId"),
        CategoryInfo("\uD83C\uDFA4", "Pronunciation", uiState.pronunciationProgress, LingoGreen,  CategoryPronunBg, "pronunciation/$currentLessonId")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEachIndexed { index, info ->
            CategoryCard(
                info = info,
                delayMillis = 400 + (index * 100),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(info.route) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    info: CategoryInfo,
    delayMillis: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "cat_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutQuart),
        label = "cat_offsetY"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offsetY
            }
            .clickable { onClick() },
        color = info.bg,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = info.emoji, fontSize = 28.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = info.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(Modifier.height(14.dp))
            
            Text(
                text = "${info.pct}%", 
                fontSize = 13.sp, 
                fontWeight = FontWeight.Bold, 
                color = info.barColor
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { info.pct / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp),
                    color = info.barColor,
                    trackColor = info.barColor.copy(alpha = 0.20f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = info.barColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// =============================================================================
// Animated Progress Section
// =============================================================================

@Composable
private fun ProgressSection(uiState: HomeUiState, delayMillis: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "prog_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutQuart),
        label = "prog_offsetY"
    )

    val overallPct = if (uiState.totalLessons > 0) {
        ((uiState.completedLessons.toFloat() / uiState.totalLessons) * 100).toInt()
    } else 0

    val overallRatio = if (uiState.totalLessons > 0) {
        uiState.completedLessons.toFloat() / uiState.totalLessons
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offsetY
            }
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "\uD83D\uDCCA", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Your Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 8.dp),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProgressStat(emoji = "\u2B50",        value = uiState.currentXp.toString(), label = "Total XP")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = LingoGrayLight)
                    ProgressStat(emoji = "\uD83D\uDD25", value = uiState.streak.toString(),    label = "Day streak")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = LingoGrayLight)
                    ProgressStat(emoji = "\uD83D\uDCD6", value = uiState.completedLessons.toString(),  label = "Lessons")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = LingoGrayLight)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { overallRatio },
                                modifier = Modifier.size(44.dp),
                                color = LingoBlue,
                                trackColor = LingoGrayLight,
                                strokeWidth = 5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(text = "$overallPct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LingoBlue)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(text = "Overall", fontSize = 9.sp, color = LingoGray)
                    }
                }
            }
        }

        // Tilted motivational card from the reference
        Surface(
            color = Color(0xFFFFFDE7),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 0.dp, y = 25.dp)
                .rotate(6f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "\u2B50", fontSize = 16.sp)
                Text(
                    text = "You're\ndoing great!\n\uD83D\uDE42",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LingoBlue,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ProgressStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
        Text(text = label, fontSize = 9.sp, color = LingoGray, textAlign = TextAlign.Center, lineHeight = 11.sp)
    }
}

// =============================================================================
// Bottom Navigation
// =============================================================================

private data class NavItemData(val label: String, val icon: ImageVector)

@Composable
private fun HomeBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val navItems = listOf(
        NavItemData("Home",     Icons.Default.Home),
        NavItemData("Learn",    Icons.AutoMirrored.Filled.MenuBook),
        NavItemData("Practice", Icons.Default.SportsEsports),
        NavItemData("Me",       Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected  = selectedIndex == index,
                onClick   = { onItemSelected(index) },
                icon      = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label     = {
                    Text(
                        text       = item.label,
                        fontSize   = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = LingoBlue,
                    selectedTextColor   = LingoBlue,
                    indicatorColor      = Color(0xFFE0EAFF),
                    unselectedIconColor = LingoGray,
                    unselectedTextColor = LingoGray
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Screen")
@Composable
private fun HomeScreenPreview() {
    MyStudyAppTheme {
        HomeScreen()
    }
}
