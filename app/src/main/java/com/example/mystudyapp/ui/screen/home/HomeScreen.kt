package com.example.mystudyapp.ui.screen.home

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // Section A — Hero introduction
                item {
                    EntranceWrapper(delayMillis = 100) {
                        HeroSection(
                            displayName = uiState.displayName,
                            onExploreClick = { onNavigate("learn") }
                        )
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }

                // Section B — Why LingoStudy? (Bento Grid)
                item {
                    EntranceWrapper(delayMillis = 250) {
                        WhyLingoStudySection()
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }

                // Section C — Your learning companion
                item {
                    EntranceWrapper(delayMillis = 400) {
                        LearningCompanionSection()
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }

                // Section D — How LingoStudy works (Illustrated Journey Timeline)
                item {
                    EntranceWrapper(delayMillis = 550) {
                        HowItWorksSection()
                    }
                }

                item { Spacer(Modifier.height(36.dp)) }

                // Section E — Final call to action
                item {
                    EntranceWrapper(delayMillis = 700) {
                        FinalCtaSection(onStartClick = { onNavigate("learn") })
                    }
                }
            }
        }
    }
}

// =============================================================================
// SECTION A — HERO INTRODUCTION
// =============================================================================

@Composable
private fun HeroSection(
    displayName: String,
    onExploreClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE),
                            Color(0xFFEFF6FF),
                            Color.White
                        )
                    )
                )
                .padding(22.dp)
        ) {
            // Top Row: Refined Brand Signature Pill & Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xFFDBEAFE),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "LingoStudy · English Companion",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingoBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = "Hello, ${displayName.ifBlank { "Learner" }}! 👋",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B82F6)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Headline Block
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Learn English.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 34.sp
                )
                Text(
                    text = "Grow with Confidence.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB),
                    lineHeight = 34.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Integrated Mascot Showcase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft Aura Glow behind mascot
                Surface(
                    modifier = Modifier.size(170.dp),
                    shape = CircleShape,
                    color = Color(0xFFDBEAFE).copy(alpha = 0.5f)
                ) {}

                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape,
                    color = Color(0xFFBFDBFE).copy(alpha = 0.4f)
                ) {}

                // Mascot Asset
                MascotView(
                    modifier = Modifier.size(175.dp),
                    expression = MascotExpression.HAPPY,
                    enableAnimation = true
                )

                // Decorative Sparkles / Floating Badges
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 12.dp, y = 10.dp)
                        .rotate(-4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Interactive",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-12).dp, y = (-10).dp)
                        .rotate(4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎯", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Real Situations",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Refined Description
            Text(
                text = "LingoStudy is your interactive language companion built to guide you from everyday vocabulary to confident communication.",
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Primary CTA Button
            Button(
                onClick = onExploreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Explore LingoStudy",
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

// =============================================================================
// SECTION B — WHY LINGOSTUDY? (EDITORIAL BENTO GRID)
// =============================================================================

@Composable
private fun WhyLingoStudySection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Why LingoStudy?",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Designed for clear, practical, and effective language acquisition.",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Bento Item 1: Large Featured Card (Full Width)
            Surface(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = Color(0xFFBAE6FD),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🎯 Core Purpose",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(text = "🗣️", fontSize = 24.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Practical Everyday Situations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Learn English designed for real scenarios—airports, hotels, asking for directions, and daily conversations.",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                }
            }

            // Bento Row 2: 2 Smaller Cards Side-by-Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card A: Organized Learning Path
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "🗺️", fontSize = 28.sp)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Organized Path",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Follow a step-by-step curriculum with progressive levels.",
                            fontSize = 12.sp,
                            color = Color(0xFF047857),
                            lineHeight = 17.sp
                        )
                    }
                }

                // Card B: Interactive Lessons & Quizzes
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "🧩", fontSize = 28.sp)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Interactive Study",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B21B6)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Practice flashcards, listening, and quizzes with instant feedback.",
                            fontSize = 12.sp,
                            color = Color(0xFF6D28D9),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Bento Item 3: Wider Bottom Card
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFDE68A),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "📈", fontSize = 24.sp)
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Continuous Progress Tracking",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = "Monitor your growth over time with XP rewards, streaks, and completed milestones.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// SECTION C — YOUR LEARNING COMPANION
// =============================================================================

@Composable
private fun LearningCompanionSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Your Learning Companion",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(16.dp))

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
                            colors = listOf(Color(0xFFFFFBEB), Color.White)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MascotView(
                        modifier = Modifier.size(110.dp),
                        expression = MascotExpression.EXCITED,
                        enableAnimation = true
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Always by your side 🦜",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Meet your study guide!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "LingoStudy's friendly companion encourages you through every lesson, keeping language practice engaging and rewarding.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Surface(
                    color = Color(0xFFFEF8EE),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 \"Small daily practice leads to big progress over time!\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =============================================================================
// SECTION D — HOW LINGOSTUDY WORKS (ILLUSTRATED JOURNEY TIMELINE)
// =============================================================================

@Composable
private fun HowItWorksSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "How LingoStudy Works",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "A simple, structured 4-step journey to English fluency.",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(Modifier.height(18.dp))

        val steps = listOf(
            StepJourneyData(
                stage = "STAGE 01 · START",
                title = "Discover a Learning Path",
                description = "Choose your level and follow a curated sequence of real-world units and lessons.",
                nodeColor = LingoBlue,
                badgeBg = Color(0xFFDBEAFE),
                badgeText = Color(0xFF1E40AF)
            ),
            StepJourneyData(
                stage = "STAGE 02 · STUDY",
                title = "Explore Structured Lessons",
                description = "Learn topic-focused content covering essential words, grammar rules, and expressions.",
                nodeColor = Color(0xFF4F46E5),
                badgeBg = Color(0xFFE0E7FF),
                badgeText = Color(0xFF3730A3)
            ),
            StepJourneyData(
                stage = "STAGE 03 · PRACTICE",
                title = "Practice Interactive Activities",
                description = "Master vocabulary flashcards, listening transcripts, pronunciation exercises, and quizzes.",
                nodeColor = Color(0xFF7C3AED),
                badgeBg = Color(0xFFEDE9FE),
                badgeText = Color(0xFF5B21B6)
            ),
            StepJourneyData(
                stage = "STAGE 04 · MASTERY",
                title = "Review & Track Growth",
                description = "Earn XP, complete modules, and watch your language skills grow day by day.",
                nodeColor = LingoGreen,
                badgeBg = Color(0xFFDCFCE7),
                badgeText = Color(0xFF166534)
            )
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Left Timeline Node Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        // Number Circle Node
                        Surface(
                            color = step.nodeColor,
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Connecting Line (if not last item)
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFCBD5E1))
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Right Content Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (index < steps.size - 1) 20.dp else 0.dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Surface(
                                    color = step.badgeBg,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = step.stage,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = step.badgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = step.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = step.description,
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569),
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

private data class StepJourneyData(
    val stage: String,
    val title: String,
    val description: String,
    val nodeColor: Color,
    val badgeBg: Color,
    val badgeText: Color
)

// =============================================================================
// SECTION E — FINAL CALL TO ACTION
// =============================================================================

@Composable
private fun FinalCtaSection(onStartClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color(0xFF1E3A8A),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your English journey starts here.",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Start exploring structured lessons today and build your skills step by step.",
                fontSize = 14.sp,
                color = Color(0xFF93C5FD),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Start Learning",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// ENTRANCE ANIMATION WRAPPER
// =============================================================================

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

// =============================================================================
// BOTTOM NAVIGATION
// =============================================================================

private data class NavItemData(val label: String, val icon: ImageVector)

@Composable
private fun HomeBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val navItems = listOf(
        NavItemData("Home", Icons.Default.Home),
        NavItemData("Learn", Icons.AutoMirrored.Filled.MenuBook),
        NavItemData("Practice", Icons.Default.SportsEsports),
        NavItemData("Me", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LingoBlue,
                    selectedTextColor = LingoBlue,
                    indicatorColor = Color(0xFFE0EAFF),
                    unselectedIconColor = LingoGray,
                    unselectedTextColor = LingoGray
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Product Showcase Home Screen")
@Composable
private fun HomeScreenPreview() {
    MyStudyAppTheme {
        HomeScreen()
    }
}
