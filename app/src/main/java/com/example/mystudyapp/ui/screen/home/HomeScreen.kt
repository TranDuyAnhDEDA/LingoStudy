package com.example.mystudyapp.ui.screen.home

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.mystudyapp.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.rememberWindowInfo
import com.example.mystudyapp.ui.component.shared.responsiveContentWidth
import com.example.mystudyapp.ui.screen.profile.LingoAvatarDisplay
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
    val windowInfo = rememberWindowInfo()
    var selectedNav by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadHomeData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_home_sky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                HomeBottomNav(
                    selectedIndex = selectedNav,
                    onItemSelected = {
                        selectedNav = it
                        when (it) {
                            0 -> onNavigate("home")
                            1 -> onNavigate("learn")
                            2 -> onNavigate("dictionary")
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .responsiveContentWidth(680.dp),
                        contentPadding = PaddingValues(bottom = 36.dp)
                    ) {
                        // Section A & B Combined — Hero Banner
                        item {
                            EntranceWrapper(delayMillis = 100) {
                                HeroBannerSection(
                                    displayName = uiState.displayName,
                                    avatarId = uiState.avatarId,
                                    customAvatarPath = uiState.customAvatarPath,
                                    onAvatarClick = { onNavigate("me") },
                                    onSettingsClick = { onNavigate("me") },
                                    onExploreClick = { onNavigate("learn") },
                                    isCompact = windowInfo.isCompact
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        // Section C — Current learning level
                        item {
                            EntranceWrapper(delayMillis = 250) {
                                CurrentLevelSection(
                                    levelName = uiState.levelName,
                                    levelOrder = uiState.levelOrder,
                                    currentXp = uiState.currentLevelXp,
                                    requiredXp = uiState.requiredXp,
                                    onClick = { onNavigate("learn") }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        // Section D — Four compact statistic cards
                        item {
                            EntranceWrapper(delayMillis = 400) {
                                StatisticsRowSection(
                                    currentXp = uiState.currentXp,
                                    streak = uiState.streak,
                                    completedLessons = uiState.completedLessons,
                                    todayStudySeconds = uiState.todayStudySeconds,
                                    todayStudyMinutes = uiState.todayStudyMinutes,
                                    isCompact = windowInfo.isCompact
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        // Section E — Continue learning card vs Getting Started Welcome card
                        item {
                            EntranceWrapper(delayMillis = 550) {
                                ContinueLearningSection(
                                    isNewUser = uiState.isNewUser,
                                    unfinishedLessonTitle = uiState.currentLessonTitle,
                                    unfinishedUnitTitle = uiState.currentUnitTitle,
                                    unitCompletedLessons = uiState.unitCompletedLessons,
                                    unitTotalLessons = uiState.unitTotalLessons,
                                    onResumeClick = {
                                        val route = uiState.resumeRoute ?: "unit/${uiState.currentUnitId}"
                                        onNavigate(route)
                                    }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(20.dp)) }

                        // Section F — Quick access
                        item {
                            EntranceWrapper(delayMillis = 700) {
                                QuickAccessSection(onNavigate = onNavigate)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SECTION A & B — HERO BANNER
// =============================================================================

@Composable
private fun HeroBannerSection(
    displayName: String,
    avatarId: String = "boy_hoodie",
    customAvatarPath: String? = null,
    onAvatarClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onExploreClick: () -> Unit,
    isCompact: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top Greeting Row with Avatar & Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isCompact) 12.dp else 16.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0F2FE),
                border = BorderStroke(2.5.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(if (isCompact) 44.dp else 52.dp)
                    .clickable { onAvatarClick() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LingoAvatarDisplay(
                        avatarId = avatarId,
                        customAvatarPath = customAvatarPath,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(if (isCompact) 8.dp else 12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAvatarClick() }
            ) {
                Text(
                    text = "Xin chào,",
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${displayName.ifBlank { "Bạn học viên" }}!",
                    fontSize = if (isCompact) 15.sp else 17.sp,
                    lineHeight = if (isCompact) 18.sp else 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 2
                )
            }

            Spacer(Modifier.width(6.dp))

            // Notification button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .size(if (isCompact) 38.dp else 42.dp)
                    .clickable { onSettingsClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Thông báo",
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Settings button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .size(if (isCompact) 38.dp else 42.dp)
                    .clickable { onSettingsClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Cài đặt",
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero Banner — Harmonious Atmospheric Blend with Living Animations
        val infiniteTransition = rememberInfiniteTransition(label = "hero_floating")
        val sparkleAlpha1 by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle_1"
        )
        val sparkleAlpha2 by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle_2"
        )

        Surface(
            onClick = onExploreClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 4.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.35f),
                        Color(0xFFBAE6FD).copy(alpha = 0.6f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 205.dp else 198.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFBAE6FD),
                                Color(0xFFE0F2FE)
                            )
                        )
                    )
            ) {
                // 1. Right Side: Full London Scene Illustration
                Image(
                    painter = painterResource(id = R.drawable.hero_london_scene),
                    contentDescription = "Mascot học tiếng Anh tại London",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )

                // 2. Cohesive Soft Atmospheric Horizontal Gradient Blend
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.00f to Color(0xFF1E40AF),
                                    0.38f to Color(0xFF1D4ED8),
                                    0.50f to Color(0xFF2563EB).copy(alpha = 0.88f),
                                    0.64f to Color(0xFF3B82F6).copy(alpha = 0.28f),
                                    0.74f to Color.Transparent,
                                    1.00f to Color.Transparent
                                )
                            )
                        )
                )

                // 3. Magical Animated Sparkles Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.width * 0.35f,
                        center = Offset(size.width * 0.15f, size.height * 0.2f)
                    )

                    drawSparkleStar(
                        center = Offset(size.width * 0.54f, size.height * 0.22f),
                        radius = 6.dp.toPx(),
                        color = Color(0xFFFDE047).copy(alpha = sparkleAlpha1)
                    )

                    drawSparkleStar(
                        center = Offset(size.width * 0.72f, size.height * 0.14f),
                        radius = 5.dp.toPx(),
                        color = Color.White.copy(alpha = sparkleAlpha2)
                    )

                    drawSparkleStar(
                        center = Offset(size.width * 0.62f, size.height * 0.76f),
                        radius = 5.5.dp.toPx(),
                        color = Color(0xFFFACC15).copy(alpha = sparkleAlpha1)
                    )
                }

                // 4. Left Column: Native Typography, Badges & Interactive CTA Button
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (isCompact) 0.62f else 0.52f)
                        .padding(
                            start = if (isCompact) 14.dp else 18.dp,
                            top = 14.dp,
                            bottom = 14.dp,
                            end = 4.dp
                        ),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Glassmorphic Badge: "Hành trình tiếng Anh"
                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "Hành trình tiếng Anh",
                            fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = if (isCompact) 8.dp else 9.dp,
                                vertical = 4.dp
                            )
                        )
                    }

                    // Headline
                    Text(
                        text = "Cùng LingoStudy\nchinh phục tiếng Anh!",
                        fontSize = if (isCompact) 15.sp else 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = if (isCompact) 19.sp else 22.sp
                    )

                    // Subtitle
                    Text(
                        text = "Rèn luyện 4 kỹ năng tiếng Anh mỗi ngày.",
                        fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                        color = Color.White.copy(alpha = 0.92f),
                        lineHeight = if (isCompact) 14.sp else 16.sp,
                        maxLines = 2
                    )

                    // Interactive CTA Button with Elevation & Vector Arrow
                    Surface(
                        onClick = onExploreClick,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isCompact) 11.dp else 14.dp,
                                vertical = if (isCompact) 6.5.dp else 8.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bắt đầu học ngay",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8),
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sparkle 4-point star generator for magical twinkling effect
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkleStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
}

// =============================================================================
// SECTION C — CURRENT LEARNING LEVEL
// =============================================================================

@Composable
private fun CurrentLevelSection(
    levelName: String,
    levelOrder: Int,
    currentXp: Int,
    requiredXp: Int,
    onClick: () -> Unit = {}
) {
    val displayLevelName = remember(levelName, levelOrder) {
        val trimmed = levelName.trim()
        when {
            trimmed.isBlank() -> "Beginner $levelOrder"
            trimmed.startsWith("Cấp ", ignoreCase = true) && trimmed.contains(":") -> {
                trimmed.substringAfter(":").trim().ifBlank { "Cấp $levelOrder" }
            }
            trimmed.contains(":") -> {
                trimmed.substringBefore(":").trim()
            }
            else -> trimmed
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Row 1: Icon + Level Info (weight 1f) + Badge "⭐ Cấp 1"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trình độ hiện tại",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayLevelName,
                            fontSize = if (displayLevelName.length > 14) 14.5.sp else 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A),
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Level Badge "Cấp 1" - Guaranteed 1 single line with softWrap = false
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Cấp $levelOrder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Row 2: Full width progress bar + XP text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val safeReqXp = requiredXp.coerceAtLeast(100)
                val progress = (currentXp.toFloat() / safeReqXp.toFloat()).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceAtLeast(0.04f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                )
                            )
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "$currentXp / $safeReqXp XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

// =============================================================================
// SECTION D — COMPACT STATISTIC CARDS
// =============================================================================

@Composable
private fun StatisticsRowSection(
    currentXp: Int,
    streak: Int,
    completedLessons: Int,
    todayStudySeconds: Long,
    todayStudyMinutes: Int,
    isCompact: Boolean = false
) {
    val displayStudyTime = when {
        todayStudyMinutes >= 60 -> "${todayStudyMinutes / 60}h ${todayStudyMinutes % 60}m"
        todayStudySeconds > 0 && todayStudyMinutes == 0 -> "${todayStudySeconds}s"
        else -> "${todayStudyMinutes}m"
    }

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCardMini(
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFD97706),
                    value = "$currentXp",
                    label = "Tổng XP",
                    bgColor = Color(0xFFFFFBEB),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                StatCardMini(
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = Color(0xFFDC2626),
                    value = "$streak",
                    label = "Ngày liên tiếp",
                    bgColor = Color(0xFFFEF2F2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCardMini(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = Color(0xFF7C3AED),
                    value = "$completedLessons",
                    label = "Bài đã học",
                    bgColor = Color(0xFFF3E8FF),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                StatCardMini(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFF059669),
                    value = displayStudyTime,
                    label = "Thời gian học",
                    bgColor = Color(0xFFECFDF5),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCardMini(
                icon = Icons.Default.Star,
                iconTint = Color(0xFFD97706),
                value = "$currentXp",
                label = "Tổng XP",
                bgColor = Color(0xFFFFFBEB),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCardMini(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFDC2626),
                value = "$streak",
                label = "Ngày liên tiếp",
                bgColor = Color(0xFFFEF2F2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCardMini(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFF7C3AED),
                value = "$completedLessons",
                label = "Bài đã học",
                bgColor = Color(0xFFF3E8FF),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCardMini(
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF059669),
                value = displayStudyTime,
                label = "Thời gian học",
                bgColor = Color(0xFFECFDF5),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StatCardMini(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Surface(
                color = bgColor,
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 13.sp,
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

// =============================================================================
// SECTION E — CONTINUE LEARNING CARD
// =============================================================================

@Composable
private fun ContinueLearningSection(
    isNewUser: Boolean,
    unfinishedLessonTitle: String,
    unfinishedUnitTitle: String,
    unitCompletedLessons: Int,
    unitTotalLessons: Int,
    onResumeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isNewUser) {
                            listOf(Color(0xFFFFFDF5), Color(0xFFFEF9C3).copy(alpha = 0.35f))
                        } else {
                            listOf(Color.White, Color(0xFFF0F7FF).copy(alpha = 0.75f))
                        }
                    )
                )
                .clickable { onResumeClick() }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Tag badge on left, lesson count badge on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isNewUser) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isNewUser) Icons.Default.AutoStories else Icons.Default.School,
                                contentDescription = null,
                                tint = if (isNewUser) Color(0xFF92400E) else Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isNewUser) "BẮT ĐẦU HÀNH TRÌNH" else "TIẾP TỤC HỌC • UNIT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isNewUser) Color(0xFF92400E) else Color(0xFF2563EB),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (!isNewUser) {
                        val total = if (unitTotalLessons > 0) unitTotalLessons else 3
                        val completed = unitCompletedLessons.coerceIn(0, total)
                        val percent = if (total > 0) (completed * 100) / total else 0

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$completed/$total bài ($percent%)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Unit Title
                Text(
                    text = if (isNewUser) "Chào mừng bạn đến với LingoStudy!" else unfinishedUnitTitle.ifBlank { "Unit 1: Chào hỏi & Giới thiệu" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 22.sp,
                    maxLines = 2
                )

                Spacer(Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = if (isNewUser) {
                        "Học bài học đầu tiên để tích lũy XP và xây dựng thói quen mỗi ngày."
                    } else if (unfinishedLessonTitle.isNotBlank()) {
                        "Đang học: $unfinishedLessonTitle"
                    } else {
                        "Tiếp tục bài học của bạn để hoàn thành Unit"
                    },
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 17.sp,
                    maxLines = 2
                )

                // Progress Bar (for returning user)
                if (!isNewUser) {
                    val total = if (unitTotalLessons > 0) unitTotalLessons else 3
                    val completed = unitCompletedLessons.coerceIn(0, total)
                    val fraction = completed.toFloat() / total.toFloat()

                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceAtLeast(0.04f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                                    )
                                )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNewUser) Color(0xFF2563EB) else Color(0xFF1D4ED8)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isNewUser) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isNewUser) "Bắt đầu Unit 1 ngay" else "Tiếp tục học Unit",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// SECTION F — QUICK ACCESS
// =============================================================================

@Composable
private fun QuickAccessSection(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Khám phá nhanh",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickAccessCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Học tập",
                desc = "Lộ trình học của bạn",
                bgColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f).clickable { onNavigate("learn") }
            )
            QuickAccessCard(
                icon = Icons.Default.Translate,
                title = "Từ điển",
                desc = "Tra cứu từ vựng",
                bgColor = Color(0xFFF5F3FF),
                iconColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f).clickable { onNavigate("dictionary") }
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickAccessCard(
                icon = Icons.Default.AutoStories,
                title = "Từ vựng",
                desc = "Ôn tập & ghi nhớ",
                bgColor = Color(0xFFECFDF5),
                iconColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f).clickable { onNavigate("learn") }
            )
            QuickAccessCard(
                icon = Icons.Default.Person,
                title = "Cài đặt",
                desc = "Tùy chỉnh ứng dụng",
                bgColor = Color(0xFFFFF1F2),
                iconColor = Color(0xFFF43F5E),
                modifier = Modifier.weight(1f).clickable { onNavigate("me") }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    desc: String,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
            )
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
        NavItemData("Trang chủ", Icons.Default.Home),
        NavItemData("Học tập", Icons.AutoMirrored.Filled.MenuBook),
        NavItemData("Từ điển", Icons.Default.Translate),
        NavItemData("Cá nhân", Icons.Default.Person)
    )

    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        containerColor = Color.White.copy(alpha = 0.97f),
        tonalElevation = 3.dp
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
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
