package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
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

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
        bottomBar = {
            LingoBottomNav(
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
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // Header
                item {
                    EntranceWrapper(delayMillis = 100) {
                        LearnHeaderSection()
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Section Title
                item {
                    EntranceWrapper(delayMillis = 200) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Learning Programs",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Select a program tailored to your goals and skill level.",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                // Program Cards
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.programs.forEachIndexed { index, program ->
                            EntranceWrapper(delayMillis = 300 + (index * 120)) {
                                ProgramCard(
                                    program = program,
                                    onSelectProgram = { onNavigate("program/${program.id}") }
                                )
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEFF6FF), Color.White)
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFDBEAFE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Curriculum Guide 📚",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingoBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose Your Program",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Start at the right level and build your English skills step by step.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            MascotView(
                modifier = Modifier.size(90.dp),
                expression = MascotExpression.HAPPY,
                enableAnimation = true
            )
        }
    }
}

@Composable
private fun ProgramCard(
    program: ProgramUiModel,
    onSelectProgram: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectProgram() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = if (program.isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (program.isAvailable) "Active Curriculum · ${program.levelCount} Level" else "Curriculum Coming Soon",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (program.isAvailable) Color(0xFF166534) else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = program.iconEmoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = program.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = program.description,
                fontSize = 13.sp,
                color = Color(0xFF475569),
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSelectProgram,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (program.isAvailable) LingoBlue else Color(0xFFF1F5F9),
                    contentColor = if (program.isAvailable) Color.White else LingoGray
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (program.isAvailable) "Explore Program" else "View Program Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
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
