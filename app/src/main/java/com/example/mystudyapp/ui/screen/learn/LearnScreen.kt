package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.R
import com.example.mystudyapp.ui.component.shared.NavItemData
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
import com.example.mystudyapp.viewmodel.LearnViewModel
import com.example.mystudyapp.viewmodel.LearnViewModelFactory
import com.example.mystudyapp.viewmodel.ProgramUiModel
import kotlinx.coroutines.delay

@Composable
fun LearnScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: LearnViewModel = viewModel(factory = LearnViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNav by remember { mutableIntStateOf(1) }

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
                LearnBottomNav(
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
                        .padding(innerPadding)
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
                ) {
                    // Header Banner
                    item {
                        EntranceWrapper(delayMillis = 80) {
                            LearnHeaderSection()
                        }
                    }

                    item { Spacer(Modifier.height(18.dp)) }

                    // Section Title
                    item {
                        EntranceWrapper(delayMillis = 180) {
                            LearnSectionHeader()
                        }
                    }

                    item { Spacer(Modifier.height(14.dp)) }

                    // Program Cards
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.programs.forEachIndexed { index, program ->
                                EntranceWrapper(delayMillis = 260 + (index * 100)) {
                                    ProgramCard(
                                        program = program,
                                        onSelectProgram = {
                                            if (program.isAvailable) {
                                                onNavigate("program/${program.id}")
                                            }
                                        }
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
private fun LearnHeaderSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = Color.White.copy(alpha = 0.93f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE).copy(alpha = 0.95f),
                            Color(0xFFEFF8FF).copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            // Right Illustration: Stack of books + paper airplane + clouds (fully transparent background)
            Image(
                painter = painterResource(id = R.drawable.ic_learn_header_books),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(135.dp),
                contentScale = ContentScale.FillHeight
            )

            // Left Text Content
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .padding(start = 18.dp, top = 18.dp, bottom = 18.dp, end = 4.dp)
            ) {
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "LỘ TRÌNH HỌC TẬP",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB),
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Chọn chương trình phù hợp",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 27.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Bắt đầu từ trình độ của bạn và từng bước chinh phục tiếng Anh.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.5.sp
                )
            }
        }
    }
}

@Composable
private fun LearnSectionHeader() {
    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Các chương trình học",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Chọn lộ trình phù hợp với mục tiêu của bạn.",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun ProgramCard(
    program: ProgramUiModel,
    onSelectProgram: () -> Unit
) {
    val (avatarRes, illRes) = remember(program.id, program.title) {
        when {
            program.id.contains("kids", ignoreCase = true) || program.title.contains("trẻ em", ignoreCase = true) -> {
                Pair(R.drawable.ic_learn_prog_kids, R.drawable.ic_learn_ill_kids)
            }
            program.id.contains("adult", ignoreCase = true) || program.id.contains("business", ignoreCase = true) || program.title.contains("đi làm", ignoreCase = true) -> {
                Pair(R.drawable.ic_learn_prog_work, R.drawable.ic_learn_ill_work)
            }
            else -> {
                Pair(R.drawable.ic_learn_prog_beginner, R.drawable.ic_learn_ill_beginner)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = program.isAvailable) { onSelectProgram() },
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status Badge
            StatusBadge(isAvailable = program.isAvailable)

            Spacer(Modifier.height(12.dp))

            // Main Info Row: Avatar + Title/Description + Right Illustration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Squircle Avatar (Smooth transparent rounded container)
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = program.title,
                    modifier = Modifier.size(66.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.width(14.dp))

                // Middle Text & Stats
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = program.description,
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )

                    if (program.isAvailable) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${program.levelCount} cấp độ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "30 bài học",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(6.dp))

                // Right Illustration (Chevron + Cloud Pod / Leaves / Teddy / City - Transparent background)
                Image(
                    painter = painterResource(id = illRes),
                    contentDescription = null,
                    modifier = Modifier.size(width = 74.dp, height = 70.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = { if (program.isAvailable) onSelectProgram() },
                enabled = program.isAvailable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEFF5FF),
                    disabledContentColor = Color(0xFF64748B)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (program.isAvailable) 2.dp else 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (program.isAvailable) "Khám phá chương trình" else "Sắp ra mắt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (program.isAvailable) {
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

@Composable
private fun StatusBadge(isAvailable: Boolean) {
    Surface(
        color = if (isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isAvailable) Color(0xFF15803D) else Color(0xFFB45309),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = if (isAvailable) "ĐANG MỞ" else "SẮP RA MẮT",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAvailable) Color(0xFF15803D) else Color(0xFFB45309),
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
private fun LearnBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LearnScreenPreview() {
    MyStudyAppTheme {
        LearnScreen()
    }
}
