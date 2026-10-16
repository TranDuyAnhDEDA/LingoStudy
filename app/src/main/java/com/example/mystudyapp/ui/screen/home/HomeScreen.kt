package com.example.mystudyapp.ui.screen.home

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AutoStories
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
                    // Section A & B Combined — Hero Banner
                    item {
                        EntranceWrapper(delayMillis = 100) {
                            HeroBannerSection(
                                displayName = uiState.displayName,
                                onExploreClick = { onNavigate("learn") }
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
                                currentXp = uiState.currentXp,
                                requiredXp = uiState.requiredXp
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
                                todayStudyMinutes = uiState.todayStudyMinutes
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
                                unfinishedProgressPct = uiState.unfinishedLessonProgressPct,
                                onResumeClick = {
                                    val route = uiState.resumeRoute ?: "lesson/${uiState.currentLessonId}"
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

// =============================================================================
// SECTION A & B — HERO BANNER
// =============================================================================

@Composable
private fun HeroBannerSection(
    displayName: String,
    onExploreClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top Greeting Row with Avatar & Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(2.dp, Color(0xFFE0F2FE)),
                shadowElevation = 2.dp,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    MascotView(
                        modifier = Modifier.size(42.dp),
                        expression = MascotExpression.HAPPY,
                        enableAnimation = false
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Xin chào,",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${displayName.ifBlank { "Bạn học viên" }}! 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = "Học tiếng Anh mỗi ngày, tiến bộ từng chút một!",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Notification button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Thông báo",
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-9).dp, y = 9.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Settings button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Cài đặt",
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Hero Graphic Banner (Seamless full-bleed illustration from mockup)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1364f / 540f)
                .clickable { onExploreClick() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_banner_full),
                contentDescription = "Hành trình tiếng Anh - Cùng LingoStudy chinh phục tiếng Anh!",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

// =============================================================================
// SECTION C — CURRENT LEARNING LEVEL
// =============================================================================

@Composable
private fun CurrentLevelSection(
    levelName: String,
    levelOrder: Int,
    currentXp: Int,
    requiredXp: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFEFF6FF),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🌟", fontSize = 24.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Trình độ hiện tại",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = levelName.ifBlank { "Beginner 1" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "›",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currentXp / ${requiredXp.coerceAtLeast(100)} XP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.height(5.dp))
                val progress = (currentXp.toFloat() / requiredXp.coerceAtLeast(100).toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceAtLeast(0.08f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                )
                            )
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 11.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Cấp $levelOrder",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706)
                    )
                }
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
    todayStudyMinutes: Int
) {
    val displayStudyTime = when {
        todayStudyMinutes >= 60 -> "${todayStudyMinutes / 60}h ${todayStudyMinutes % 60}m"
        todayStudySeconds > 0 && todayStudyMinutes == 0 -> "${todayStudySeconds}s"
        else -> "${todayStudyMinutes}m"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCardMini(
            icon = "⭐",
            value = "$currentXp",
            label = "Tổng XP",
            bgColor = Color(0xFFFFFBEB),
            modifier = Modifier.weight(1f)
        )
        StatCardMini(
            icon = "🔥",
            value = "$streak",
            label = "Ngày học liên tiếp",
            bgColor = Color(0xFFFEF2F2),
            modifier = Modifier.weight(1f)
        )
        StatCardMini(
            icon = "📚",
            value = "$completedLessons",
            label = "Bài học đã hoàn thành",
            bgColor = Color(0xFFF3E8FF),
            modifier = Modifier.weight(1f)
        )
        StatCardMini(
            icon = "⏱️",
            value = displayStudyTime,
            label = "Tổng thời gian học",
            bgColor = Color(0xFFECFDF5),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCardMini(
    icon: String,
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
        Column(modifier = Modifier.padding(11.dp)) {
            Surface(
                color = bgColor,
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 13.sp,
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
    unfinishedProgressPct: Int,
    onResumeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isNewUser) {
                            listOf(Color(0xFFFFFDF5), Color(0xFFFEF9C3).copy(alpha = 0.4f))
                        } else {
                            listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF).copy(alpha = 0.6f))
                        }
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.68f)) {
                    Surface(
                        color = if (isNewUser) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isNewUser) "BẮT ĐẦU HÀNH TRÌNH 🚀" else "Tiếp tục học",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isNewUser) Color(0xFF92400E) else Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (isNewUser) "Chào mừng bạn đến với LingoStudy!" else unfinishedLessonTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = if (isNewUser) "Học bài học đầu tiên để tích lũy XP và xây dựng thói quen mỗi ngày." else unfinishedUnitTitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(12.dp))

                    if (!isNewUser) {
                        val fraction = unfinishedProgressPct / 100f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceAtLeast(0.05f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                            )
                                        )
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${(fraction * 5).toInt()} / 5 kỹ năng",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    Button(
                        onClick = onResumeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(21.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNewUser) Color(0xFF2563EB) else Color(0xFF1D4ED8)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isNewUser) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isNewUser) "Bắt đầu bài 1 ngay" else "Tiếp tục học ›",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(0.32f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Speech bubble "Hello!"
                    Surface(
                        color = Color(0xFF38BDF8),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = "Hello! 👋",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    MascotView(
                        modifier = Modifier.size(88.dp),
                        expression = if (isNewUser) MascotExpression.EXCITED else MascotExpression.HAPPY,
                        enableAnimation = true
                    )
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

                Text(
                    text = "›",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
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
