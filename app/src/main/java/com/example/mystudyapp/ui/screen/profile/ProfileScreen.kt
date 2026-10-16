package com.example.mystudyapp.ui.screen.profile

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGrayLight
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.ui.theme.LingoPink
import com.example.mystudyapp.ui.theme.LingoPurple
import com.example.mystudyapp.ui.theme.LingoYellow
import com.example.mystudyapp.viewmodel.DailyChartBar
import com.example.mystudyapp.viewmodel.ProfileViewModel
import com.example.mystudyapp.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
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
            val profile = uiState.profile
            val progress = uiState.progress

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section A: Profile Header Card
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFEFF6FF), Color.White)
                                    )
                                )
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    color = Color(0xFFDBEAFE),
                                    shape = CircleShape,
                                    modifier = Modifier.size(100.dp)
                                ) {}

                                MascotView(
                                    modifier = Modifier.size(110.dp),
                                    expression = MascotExpression.HAPPY,
                                    enableAnimation = true
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = profile?.displayName.orEmpty().ifBlank { "Learner" },
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = LingoBlue,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { showEditNameDialog = true }
                                )
                            }

                            if (!profile?.email.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = profile?.email ?: "",
                                    fontSize = 13.sp,
                                    color = LingoGray
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Level Badge & XP Progress Bar
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.currentLevelName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF047857)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "${uiState.currentXp} / ${uiState.requiredXp} XP",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    val progressFrac = (uiState.currentXp.toFloat() / uiState.requiredXp.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progressFrac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = LingoGreen,
                                        trackColor = Color(0xFFA7F3D0),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                // Section E: Continue Learning Card (Resume Learning)
                if (uiState.unfinishedLessonId != null) {
                    item {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBFDBFE)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = LingoBlue,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "RESUME LEARNING",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = "${uiState.unfinishedProgressPct}% Completed",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = uiState.unfinishedLessonTitle ?: "At the Airport",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )

                                Text(
                                    text = uiState.unfinishedUnitTitle ?: "Travel and Tourism",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569)
                                )

                                Spacer(Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val route = uiState.resumeRoute ?: "lesson/${uiState.unfinishedLessonId}"
                                        onNavigate(route)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Continue Learning", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section B: Overall Statistics Row
                item {
                    Text(
                        text = "Learning Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(emoji = "⭐", value = "${uiState.currentXp}", label = "Total XP", modifier = Modifier.weight(1f))
                        ProfileStatCard(emoji = "🔥", value = "${profile?.streak ?: 0}", label = "Day Streak", modifier = Modifier.weight(1f))
                        ProfileStatCard(emoji = "📚", value = "${progress?.completedLessons ?: 0}", label = "Lessons", modifier = Modifier.weight(1f))
                        ProfileStatCard(emoji = "⏱️", value = "${uiState.totalStudyMinutes}m", label = "Study Time", modifier = Modifier.weight(1f))
                    }
                }

                // Section D: 7-Day Study Time Chart
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = LingoBlue, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("7-Day Study Activity", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                }

                                Text("Today: ${uiState.todayStudyMinutes}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LingoGreen)
                            }

                            Spacer(Modifier.height(16.dp))

                            if (uiState.last7DaysChart.all { it.minutes == 0 }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No study activity recorded in the last 7 days.", fontSize = 13.sp, color = LingoGray, textAlign = TextAlign.Center)
                                    Text("Start a lesson to track your daily study time!", fontSize = 12.sp, color = LingoBlue, textAlign = TextAlign.Center)
                                }
                            } else {
                                StudyTimeBarChart(bars = uiState.last7DaysChart)
                            }
                        }
                    }
                }

                // Section C: Skill Mastery Overview
                item {
                    Text(
                        text = "Skill Mastery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SkillProgressBar(label = "Vocabulary", pct = uiState.vocabMasteryPct, color = LingoPink)
                            SkillProgressBar(label = "Grammar", pct = uiState.grammarMasteryPct, color = LingoYellow)
                            SkillProgressBar(label = "Listening", pct = uiState.listeningMasteryPct, color = LingoPurple)
                            SkillProgressBar(label = "Pronunciation", pct = uiState.pronunMasteryPct, color = LingoGreen)
                            SkillProgressBar(label = "Quiz", pct = uiState.quizMasteryPct, color = LingoBlue)
                        }
                    }
                }

                // Section F: Account Actions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showEditNameDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Edit Display Name", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var newNameInput by remember { mutableStateOf(uiState.profile?.displayName ?: "") }

        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text("Edit Display Name", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            },
            text = {
                Column {
                    Text("Enter your new display name below:", fontSize = 13.sp, color = LingoGray)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LingoBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDisplayName(newNameInput)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = LingoGray)
                }
            }
        )
    }
}

@Composable
private fun StudyTimeBarChart(bars: List<DailyChartBar>) {
    val maxMins = (bars.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val heightFrac = (bar.minutes.toFloat() / maxMins).coerceIn(0.1f, 1f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(36.dp)
            ) {
                Text(
                    text = "${bar.minutes}m",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bar.minutes > 0) LingoBlue else LingoGray
                )

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height((80 * heightFrac).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            brush = if (bar.minutes > 0) {
                                Brush.verticalGradient(listOf(Color(0xFF60A5FA), LingoBlue))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                            }
                        )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = bar.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = LingoGray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SkillProgressBar(label: String, pct: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = "$pct%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = LingoGrayLight,
            strokeCap = StrokeCap.Round
        )
    }
}
