package com.example.mystudyapp.ui.screen.profile

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.viewmodel.DailyChartBar
import com.example.mystudyapp.viewmodel.LearningHistoryItem
import com.example.mystudyapp.viewmodel.ProfileUiState
import com.example.mystudyapp.viewmodel.ProfileViewModel
import com.example.mystudyapp.viewmodel.ProfileViewModelFactory
import java.util.Locale

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dialog control states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAvatarPickerDialog by remember { mutableStateOf(false) }
    var showLearningHistoryDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // TTS engine for favorite words pronunciation
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.US
            }
        }
        tts = ttsInstance
        onDispose {
            try {
                ttsInstance?.stop()
                ttsInstance?.shutdown()
            } catch (e: Exception) {
                // Ignore shutdown error
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProfileData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            LingoBottomNav(
                selectedIndex = 3,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigate("home")
                        1 -> onNavigate("learn")
                        2 -> onNavigate("dictionary")
                        3 -> onNavigate("me")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD6ECFD),
                            Color(0xFFE8F4FE),
                            Color(0xFFF8FAFC),
                            Color.White
                        )
                    )
                )
        ) {
            // Subtle translucent clouds
            CloudsBackgroundCanvas()

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: Top Header (Avatar, Name, Email, Pill Tag, Settings Button)
                    item {
                        ProfileHeader(
                            uiState = uiState,
                            onAvatarClick = { showAvatarPickerDialog = true },
                            onEditNameClick = { showEditProfileDialog = true },
                            onSettingsClick = { showSettingsDialog = true }
                        )
                    }

                    // SECTION 2: 4-Column Statistics Card
                    item {
                        ProfileStatsCard(uiState = uiState)
                    }

                    // SECTION 2.5: Cột thống kê thời gian học mỗi ngày (Daily Study Time Chart)
                    item {
                        ProfileStudyTimeChart(
                            chartBars = uiState.last7DaysChart,
                            todayMinutes = uiState.todayStudyMinutes,
                            totalMinutes = uiState.totalStudyMinutes,
                            dailyGoalMinutes = uiState.profile?.dailyGoalMinutes ?: 30
                        )
                    }

                    // SECTION 3: Main Menu Section (Card with 6 rows)
                    item {
                        ProfileMenuList(
                            onEditProfile = { showEditProfileDialog = true },
                            onLearningHistory = { showLearningHistoryDialog = true },
                            onFavorites = { showFavoritesDialog = true },
                            onNotifications = { showNotificationsDialog = true },
                            onHelpSupport = { showHelpDialog = true },
                            onLogout = { showLogoutConfirmDialog = true }
                        )
                    }

                    // SECTION 4: Bottom Decorative Banner with Cute Leaves
                    item {
                        BottomBannerDecoration()
                    }
                }
            }
        }
    }

    // MODAL 1: Chỉnh sửa thông tin cá nhân
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.profile?.displayName.orEmpty().ifBlank { "Nguyễn Minh Anh" },
            currentPhone = uiState.profile?.phoneNumber.orEmpty(),
            currentGoal = uiState.profile?.dailyGoalMinutes ?: 30,
            currentLevel = uiState.currentLevelName,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, phone, goal, level ->
                viewModel.updateProfileDetails(name, phone, goal, level)
                showEditProfileDialog = false
            }
        )
    }

    // MODAL 2: Chọn ảnh đại diện (Avatar Picker & Upload from Gallery)
    if (showAvatarPickerDialog) {
        AvatarPickerDialog(
            currentAvatarId = uiState.profile?.avatarId ?: "boy_hoodie",
            currentCustomPath = uiState.profile?.customAvatarPath,
            onDismiss = { showAvatarPickerDialog = false },
            onSelectAvatar = { newAvatarId, newCustomPath ->
                viewModel.updateAvatar(newAvatarId, newCustomPath)
                showAvatarPickerDialog = false
            }
        )
    }

    // MODAL 3: Lịch sử học tập
    if (showLearningHistoryDialog) {
        LearningHistoryDialog(
            historyList = uiState.learningHistory,
            onDismiss = { showLearningHistoryDialog = false },
            onContinueLesson = { lessonId ->
                showLearningHistoryDialog = false
                onNavigate("lesson/$lessonId")
            }
        )
    }

    // MODAL 4: Từ vựng yêu thích
    if (showFavoritesDialog) {
        FavoriteWordsDialog(
            favorites = uiState.favoriteWords,
            onDismiss = { showFavoritesDialog = false },
            onPlayAudio = { word ->
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "profile_fav_tts")
            },
            onSearchWordInDict = { word ->
                showFavoritesDialog = false
                onNavigate("dictionary")
            }
        )
    }

    // MODAL 5: Cài đặt thông báo
    if (showNotificationsDialog) {
        NotificationSettingsDialog(
            notificationsEnabled = uiState.notificationsEnabled,
            reminderTime = uiState.reminderTime,
            onToggle = { viewModel.toggleNotifications(it) },
            onSetTime = { viewModel.setReminderTime(it) },
            onDismiss = { showNotificationsDialog = false }
        )
    }

    // MODAL 6: Trợ giúp & Hỗ trợ
    if (showHelpDialog) {
        HelpSupportDialog(onDismiss = { showHelpDialog = false })
    }

    // MODAL 7: Xác nhận Đăng xuất
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Đăng xuất tài khoản",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng LingoStudy không?",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Hủy", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // MODAL 8: Cài đặt nhanh (Gear icon)
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onEditProfile = {
                showSettingsDialog = false
                showEditProfileDialog = true
            },
            onNotifications = {
                showSettingsDialog = false
                showNotificationsDialog = true
            }
        )
    }
}

// -------------------------------------------------------------
// SECTION 1: Profile Header
// -------------------------------------------------------------
@Composable
private fun ProfileHeader(
    uiState: ProfileUiState,
    onAvatarClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val profile = uiState.profile
    val displayName = profile?.displayName.orEmpty().ifBlank { "Nguyễn Minh Anh" }
    val email = profile?.email.orEmpty().ifBlank { "minhanh@gmail.com" }
    val avatarId = profile?.avatarId ?: "boy_hoodie"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Avatar with Camera Badge (matches 1:1 reference)
        LingoAvatarWithBadge(
            avatarId = avatarId,
            customAvatarPath = profile?.customAvatarPath,
            size = 84.dp,
            onEditClick = onAvatarClick
        )

        Spacer(Modifier.width(14.dp))

        // Name, Email, Pill Badge
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEditNameClick() }
            ) {
                Text(
                    text = displayName,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa tên",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = email,
                fontSize = 13.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Blue Tag Badge: "★ Học viên tích cực"
            Surface(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = uiState.currentLevelName.ifBlank { "Học viên tích cực" },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }

        // Circular Settings Button (Gear icon)
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: 4-Column Statistics Card
// -------------------------------------------------------------
@Composable
private fun ProfileStatsCard(uiState: ProfileUiState) {
    val profile = uiState.profile
    val progress = uiState.progress

    val xpVal = uiState.currentXp
    val streakVal = profile?.streak ?: 2
    val completedLessonsVal = progress?.completedLessons ?: profile?.totalLessonsCompleted ?: 1
    val studyTimeMins = uiState.totalStudyMinutes

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Col 1: Tổng XP
            StatColumn(
                icon = Icons.Default.Star,
                iconTint = Color(0xFFD97706),
                iconBg = Color(0xFFFEF3C7),
                value = "$xpVal",
                label = "Tổng XP",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFF1F5F9))

            // Col 2: Ngày học liên tiếp
            StatColumn(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFEF4444),
                iconBg = Color(0xFFFEE2E2),
                value = "$streakVal",
                label = "Ngày liên tiếp",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFF1F5F9))

            // Col 3: Bài học đã hoàn thành
            StatColumn(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                value = "$completedLessonsVal",
                label = "Bài hoàn thành",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFF1F5F9))

            // Col 4: Thời gian học
            StatColumn(
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF10B981),
                iconBg = Color(0xFFDCFCE7),
                value = formatStudyDuration(studyTimeMins),
                label = "Thời gian học",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatColumn(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.5.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Format minutes into a user-friendly time string:
 * - Dưới 60 phút: hiển thị dạng m (e.g. "15m", "45m")
 * - Từ 60 phút trở lên: chuyển sang dạng h + m (e.g. "1h", "1h 15m", "1h 53m")
 */
internal fun formatStudyDuration(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

/**
 * Phiên bản ngắn gọn cho không gian hẹp (trên đầu cột biểu đồ):
 * - e.g. "15m", "1h", "1h15m"
 */
internal fun formatStudyDurationShort(minutes: Int): String {
    if (minutes <= 0) return "0"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h${m}m"
    }
}

// -------------------------------------------------------------
// SECTION 2.5: Cột thống kê thời gian học mỗi ngày (Daily Study Time Chart)
// -------------------------------------------------------------
@Composable
private fun ProfileStudyTimeChart(
    chartBars: List<DailyChartBar>,
    todayMinutes: Int,
    totalMinutes: Int,
    dailyGoalMinutes: Int = 30
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Icon + Title & Subtitle + Today Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Thời gian học mỗi ngày",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Thời gian sử dụng ứng dụng 7 ngày qua",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Pill Tag: Hôm nay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Hôm nay: ${formatStudyDuration(todayMinutes)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3-Metric Summary Mini Row (Tổng 7 ngày, Trung bình, Mục tiêu)
            val sum7Days = chartBars.sumOf { it.minutes }
            val display7Days = if (sum7Days > 0) sum7Days else totalMinutes
            val avgMins = if (chartBars.isNotEmpty()) (display7Days / chartBars.size) else 0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Tổng 7 ngày
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatStudyDuration(display7Days),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "Tổng 7 ngày",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE2E8F0))

                // Metric 2: Trung bình/ngày
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatStudyDuration(avgMins),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Trung bình/ngày",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE2E8F0))

                // Metric 3: Mục tiêu ngày
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatStudyDuration(dailyGoalMinutes),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        text = "Mục tiêu ngày",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Bar Chart Columns (7 vertical bars)
            val maxMins = maxOf(dailyGoalMinutes, chartBars.maxOfOrNull { it.minutes } ?: 30, 30)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                chartBars.forEach { bar ->
                    val fraction = (bar.minutes.toFloat() / maxMins.toFloat()).coerceIn(0.04f, 1f)
                    val isGoalMet = bar.minutes >= dailyGoalMinutes && bar.minutes > 0

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Minute label on top of bar
                        Text(
                            text = if (bar.minutes > 0) formatStudyDurationShort(bar.minutes) else "0",
                            fontSize = 10.sp,
                            fontWeight = if (bar.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                bar.isToday -> Color(0xFF2563EB)
                                isGoalMet -> Color(0xFF10B981)
                                bar.minutes > 0 -> Color(0xFF475569)
                                else -> Color(0xFFCBD5E1)
                            }
                        )

                        Spacer(Modifier.height(6.dp))

                        // Vertical bar container
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (bar.isToday) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                .then(
                                    if (bar.isToday) Modifier.border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(12.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Target Goal guideline indicator (at dailyGoalMinutes height)
                            val goalFraction = (dailyGoalMinutes.toFloat() / maxMins.toFloat()).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .offset(y = -(95.dp * goalFraction))
                                    .height(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )

                            // Filled bar with rounded corners and gradient
                            if (bar.minutes > 0) {
                                val barBrush = when {
                                    isGoalMet -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF34D399), Color(0xFF10B981))
                                    )
                                    bar.isToday -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                                    )
                                    else -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF93C5FD), Color(0xFF3B82F6))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(barBrush)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Day label (T2, T3, T4, T5, T6, T7, CN)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (bar.isToday) Color(0xFF2563EB) else Color.Transparent
                        ) {
                            Text(
                                text = bar.dayLabel,
                                fontSize = 11.5.sp,
                                fontWeight = if (bar.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (bar.isToday) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Date label (e.g. 13/10)
                        if (bar.dateDisplay.isNotBlank()) {
                            Text(
                                text = bar.dateDisplay,
                                fontSize = 9.sp,
                                color = if (bar.isToday) Color(0xFF2563EB) else Color(0xFF94A3B8),
                                fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Goal Progress Note Banner
            val todayProgressPct = ((todayMinutes.toFloat() / dailyGoalMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (todayMinutes >= dailyGoalMinutes) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (todayMinutes >= dailyGoalMinutes) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (todayMinutes >= dailyGoalMinutes) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (todayMinutes >= dailyGoalMinutes) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (todayMinutes >= dailyGoalMinutes) {
                            "Đã hoàn thành mục tiêu học hôm nay (${formatStudyDuration(todayMinutes)}/${formatStudyDuration(dailyGoalMinutes)}) 🎉"
                        } else {
                            "Còn ${formatStudyDuration(dailyGoalMinutes - todayMinutes)} nữa để đạt mục tiêu hôm nay (${formatStudyDuration(todayMinutes)}/${formatStudyDuration(dailyGoalMinutes)})"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (todayMinutes >= dailyGoalMinutes) Color(0xFF166534) else Color(0xFF475569),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$todayProgressPct%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (todayMinutes >= dailyGoalMinutes) Color(0xFF10B981) else Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: Main Menu Section (Card with 6 rows)
// -------------------------------------------------------------
@Composable
private fun ProfileMenuList(
    onEditProfile: () -> Unit,
    onLearningHistory: () -> Unit,
    onFavorites: () -> Unit,
    onNotifications: () -> Unit,
    onHelpSupport: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Thông tin cá nhân
            ProfileMenuItem(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF2563EB),
                iconBg = Color(0xFFE0F2FE),
                title = "Thông tin cá nhân",
                subtitle = "Xem và chỉnh sửa thông tin tài khoản",
                onClick = onEditProfile
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(start = 68.dp))

            // 2. Lịch sử học tập
            ProfileMenuItem(
                icon = Icons.Default.AutoStories,
                iconTint = Color(0xFF9333EA),
                iconBg = Color(0xFFF3E8FF),
                title = "Lịch sử học tập",
                subtitle = "Xem tiến độ và kết quả học tập",
                onClick = onLearningHistory
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(start = 68.dp))

            // 3. Từ vựng yêu thích
            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                iconTint = Color(0xFFEC4899),
                iconBg = Color(0xFFFCE7F3),
                title = "Từ vựng yêu thích",
                subtitle = "Danh sách từ vựng đã lưu",
                onClick = onFavorites
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(start = 68.dp))

            // 4. Thông báo
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                iconTint = Color(0xFF0D9488),
                iconBg = Color(0xFFCCFBF1),
                title = "Thông báo",
                subtitle = "Quản lý thông báo của ứng dụng",
                onClick = onNotifications
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(start = 68.dp))

            // 5. Trợ giúp & Hỗ trợ
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                iconTint = Color(0xFF3B82F6),
                iconBg = Color(0xFFE0F2FE),
                title = "Trợ giúp & Hỗ trợ",
                subtitle = "Câu hỏi thường gặp, liên hệ hỗ trợ",
                onClick = onHelpSupport
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(start = 68.dp))

            // 6. Đăng xuất
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                iconTint = Color(0xFFEF4444),
                iconBg = Color(0xFFFEE2E2),
                title = "Đăng xuất",
                subtitle = "Thoát khỏi tài khoản hiện tại",
                onClick = onLogout,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDestructive) Color(0xFF1E293B) else Color(0xFF1E293B)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = Color(0xFF64748B)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

// -------------------------------------------------------------
// SECTION 4: Bottom Banner Decoration
// -------------------------------------------------------------
@Composable
private fun BottomBannerDecoration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative foliage on left and right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left green bush
            Canvas(modifier = Modifier.size(48.dp, 36.dp)) {
                drawCircle(Color(0xFF86EFAC).copy(alpha = 0.5f), radius = 22f, center = Offset(10f, 30f))
                drawCircle(Color(0xFF4ADE80).copy(alpha = 0.6f), radius = 26f, center = Offset(30f, 24f))
                drawCircle(Color(0xFF22C55E).copy(alpha = 0.7f), radius = 20f, center = Offset(45f, 34f))
            }

            // Right green bush
            Canvas(modifier = Modifier.size(48.dp, 36.dp)) {
                drawCircle(Color(0xFF22C55E).copy(alpha = 0.7f), radius = 20f, center = Offset(5f, 34f))
                drawCircle(Color(0xFF4ADE80).copy(alpha = 0.6f), radius = 26f, center = Offset(20f, 24f))
                drawCircle(Color(0xFF86EFAC).copy(alpha = 0.5f), radius = 22f, center = Offset(40f, 30f))
            }
        }

        // Cute tilted handwritten slogan
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.rotate(-2.5f)
        ) {
            Text(
                text = "Học tiếng Anh",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "cùng ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = "LingoStudy!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB)
                )
            }

            // Blue curved smile underline
            Canvas(modifier = Modifier.size(110.dp, 6.dp)) {
                val p = Path().apply {
                    moveTo(4f, 2f)
                    cubicTo(35f, 6f, 75f, 6f, 106f, 2f)
                }
                drawPath(p, color = Color(0xFF3B82F6), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f))
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 1: Sửa thông tin cá nhân (Edit Profile Details)
// -------------------------------------------------------------
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhone: String,
    currentGoal: Int,
    currentLevel: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, goal: Int, level: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var phoneInput by remember { mutableStateOf(currentPhone) }
    var selectedGoal by remember { mutableIntStateOf(currentGoal) }
    var selectedLevel by remember { mutableStateOf(currentLevel) }

    val levelOptions = listOf(
        "Beginner 1: Nền tảng & Giao tiếp",
        "Beginner 2: Mở rộng vốn từ",
        "Kids English: Khám phá thế giới",
        "Tiếng Anh Giao tiếp Tự tin"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chỉnh sửa thông tin",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Field 1: Tên hiển thị
                Text("Tên hiển thị", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Field 2: Số điện thoại
                Text("Số điện thoại (tùy chọn)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    placeholder = { Text("Ví dụ: 0912345678", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                // Field 3: Mục tiêu học tập mỗi ngày
                Text("Mục tiêu học mỗi ngày", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val isSelected = selectedGoal == mins
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGoal = mins }
                        ) {
                            Text(
                                text = "${mins}p",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Field 4: Cấp độ mong muốn
                Text("Lộ trình học tập", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    levelOptions.forEach { lvl ->
                        val isSel = selectedLevel == lvl
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSel) Color(0xFF3B82F6) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLevel = lvl }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = lvl,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color(0xFF1E3A8A) else Color(0xFF475569)
                                )
                                if (isSel) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("Hủy", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSave(nameInput.trim(), phoneInput.trim(), selectedGoal, selectedLevel)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                    ) {
                        Text("Lưu thay đổi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 2: Chọn Avatar (Avatar Picker Dialog & Gallery Upload)
// -------------------------------------------------------------
@Composable
private fun AvatarPickerDialog(
    currentAvatarId: String,
    currentCustomPath: String?,
    onDismiss: () -> Unit,
    onSelectAvatar: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf(currentAvatarId) }
    var selectedCustomPath by remember { mutableStateOf(currentCustomPath) }

    // Launcher for picking an image from system gallery / file picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = AvatarManager.copyUriToInternalStorage(context, "user", it)
            if (savedPath != null) {
                selectedCustomPath = savedPath
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ảnh đại diện",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Tải ảnh từ máy hoặc chọn nhân vật hoạt hình",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Big Preview of selected avatar (Custom photo or Cartoon avatar)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .border(3.dp, Color(0xFF2563EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    LingoAvatarDisplay(
                        avatarId = selectedId,
                        customAvatarPath = selectedCustomPath,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (selectedCustomPath != null) {
                    Text(
                        text = "Ảnh từ thư viện của bạn",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "Đã chọn ảnh tùy chỉnh từ thiết bị",
                        fontSize = 12.sp,
                        color = Color(0xFF059669)
                    )
                } else {
                    val selectedItem = AVAILABLE_AVATARS.firstOrNull { it.id == selectedId }
                    Text(
                        text = selectedItem?.name ?: "Minh Anh",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = selectedItem?.description ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Button: Tải ảnh từ thư viện thiết bị
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, if (selectedCustomPath != null) Color(0xFF2563EB) else Color(0xFF93C5FD)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedCustomPath != null) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (selectedCustomPath != null) "Chọn ảnh khác từ thư viện" else "Tải ảnh từ thư viện thiết bị",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                // Divider: HOẶC CHỌN NHÂN VẬT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    Text(
                        text = "  HOẶC CHỌN NHÂN VẬT  ",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                }

                // Avatar Grid (3 columns x 2 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    items(AVAILABLE_AVATARS) { item ->
                        val isSelected = selectedCustomPath == null && item.id == selectedId
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedId = item.id
                                    selectedCustomPath = null
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2FE))
                                ) {
                                    LingoAvatarCanvas(avatarId = item.id, modifier = Modifier.fillMaxSize())
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2563EB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = item.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF475569),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Confirm button
                Button(
                    onClick = { onSelectAvatar(selectedId, selectedCustomPath) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Lưu ảnh đại diện", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 3: Lịch sử học tập (Learning History Dialog)
// -------------------------------------------------------------
@Composable
private fun LearningHistoryDialog(
    historyList: List<LearningHistoryItem>,
    onDismiss: () -> Unit,
    onContinueLesson: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color(0xFFF8FAFC),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lịch sử học tập",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Tiến trình và các bài học đã tham gia",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Summary Pill
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tổng bài học", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${historyList.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                        }
                        VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE2E8F0))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hoàn thành", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${historyList.count { it.isCompleted }}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A))
                        }
                        VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE2E8F0))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Đang học dở", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${historyList.count { !it.isCompleted }}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // History List
                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa bắt đầu bài học nào.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryItemCard(item = item, onClick = { onContinueLesson(item.lessonId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: LearningHistoryItem,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unit Label
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.unitTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Completion status
                if (item.isCompleted) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Hoàn thành", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Đang học ${item.progressPct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(8.dp))

            // Module pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModuleBadge(label = "Từ vựng", isDone = item.vocabDone)
                ModuleBadge(label = "Ngữ pháp", isDone = item.grammarDone)
                ModuleBadge(label = "Luyện nghe", isDone = item.listeningDone)
                ModuleBadge(label = "Trắc nghiệm", isDone = item.quizDone)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.completedAtText,
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8)
                )

                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.isCompleted) "Học lại ›" else "Tiếp tục học ›",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBadge(label: String, isDone: Boolean) {
    Surface(
        color = if (isDone) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = if (isDone) "$label ✓" else label,
            fontSize = 10.sp,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isDone) Color(0xFF059669) else Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// -------------------------------------------------------------
// DIALOG 4: Từ vựng yêu thích (Favorite Words Dialog)
// -------------------------------------------------------------
@Composable
private fun FavoriteWordsDialog(
    favorites: List<String>,
    onDismiss: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onSearchWordInDict: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Từ vựng yêu thích",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                val displayList = favorites.ifEmpty { listOf("apple", "house", "cat", "school") }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayList) { word ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onPlayAudio(word) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDBEAFE))
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Phát âm",
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = word,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = when (word.lowercase()) {
                                                "apple" -> "quả táo (danh từ)"
                                                "house" -> "ngôi nhà (danh từ)"
                                                "cat" -> "con mèo (danh từ)"
                                                "school" -> "trường học (danh từ)"
                                                else -> "Từ đã lưu trong từ điển"
                                            },
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                TextButton(onClick = { onSearchWordInDict(word) }) {
                                    Text("Tra cứu", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 5: Cài đặt thông báo (Notifications Dialog)
// -------------------------------------------------------------
@Composable
private fun NotificationSettingsDialog(
    notificationsEnabled: Boolean,
    reminderTime: String,
    onToggle: (Boolean) -> Unit,
    onSetTime: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(notificationsEnabled) }
    var time by remember { mutableStateOf(reminderTime) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cài đặt thông báo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nhắc nhở học hàng ngày", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Nhận thông báo nhắc học để duy trì streak", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            onToggle(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text("Giờ nhắc nhở", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("08:00", "12:30", "20:00", "21:30").forEach { t ->
                        val isSel = time == t
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    time = t
                                    onSetTime(t)
                                }
                        ) {
                            Text(
                                text = t,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 6: Trợ giúp & Hỗ trợ (Help & Support Dialog)
// -------------------------------------------------------------
@Composable
private fun HelpSupportDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trợ giúp & Hỗ trợ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text("Hỗ trợ kỹ thuật LingoStudy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(4.dp))
                Text("• Email: support@lingostudy.edu.vn", fontSize = 13.sp, color = Color(0xFF475569))
                Text("• Hotline: 1900 8888 (8:00 - 21:00)", fontSize = 13.sp, color = Color(0xFF475569))
                Text("• Website: https://lingostudy.app", fontSize = 13.sp, color = Color(0xFF2563EB))

                Spacer(Modifier.height(14.dp))

                Text("Câu hỏi thường gặp (FAQ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                Spacer(Modifier.height(4.dp))
                Text("Q: Làm sao để duy trì chuỗi ngày học Streak?", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Text("A: Bạn chỉ cần hoàn thành ít nhất 1 hoạt động học tập hoặc tích lũy tối thiểu 5 phút mỗi ngày.", fontSize = 12.sp, color = Color(0xFF64748B))

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Đã hiểu", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG 8: Cài đặt nhanh (Settings Dialog)
// -------------------------------------------------------------
@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onNotifications: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cài đặt ứng dụng", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditProfile() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thông tin tài khoản", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNotifications() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cài đặt thông báo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ngôn ngữ giao diện", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    Text("Tiếng Việt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Phiên bản", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    Text("LingoStudy v2.0", fontSize = 13.sp, color = Color(0xFF64748B))
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Decorative Background Clouds Canvas
// -------------------------------------------------------------
@Composable
private fun CloudsBackgroundCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Cloud 1 - Top right
        drawCircle(Color.White.copy(alpha = 0.45f), radius = w * 0.28f, center = Offset(w * 0.90f, h * 0.12f))
        drawCircle(Color.White.copy(alpha = 0.35f), radius = w * 0.20f, center = Offset(w * 0.70f, h * 0.08f))

        // Cloud 2 - Top left
        drawCircle(Color.White.copy(alpha = 0.40f), radius = w * 0.22f, center = Offset(w * 0.05f, h * 0.16f))
        drawCircle(Color.White.copy(alpha = 0.30f), radius = w * 0.16f, center = Offset(w * 0.22f, h * 0.18f))

        // Cloud 3 - Mid background
        drawCircle(Color.White.copy(alpha = 0.35f), radius = w * 0.35f, center = Offset(w * 0.50f, h * 0.42f))

        // Cloud 4 - Bottom subtle glow
        drawCircle(Color.White.copy(alpha = 0.50f), radius = w * 0.40f, center = Offset(w * 0.15f, h * 0.88f))
        drawCircle(Color.White.copy(alpha = 0.50f), radius = w * 0.40f, center = Offset(w * 0.85f, h * 0.88f))
    }
}
