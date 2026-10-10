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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.ProgramOverviewViewModel
import com.example.mystudyapp.viewmodel.ProgramOverviewViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun ProgramOverviewScreen(
    programId: String,
    onBackClick: () -> Unit = {},
    onSelectLevel: (String) -> Unit = {},
    viewModel: ProgramOverviewViewModel = viewModel(factory = ProgramOverviewViewModelFactory(programId))
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
                        text = uiState.program?.title ?: "Program Overview",
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Editorial Landing Hero
                item {
                    EntranceWrapper(delayMillis = 100) {
                        val program = uiState.program
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
                                            colors = listOf(Color(0xFFE0F2FE), Color(0xFFEFF6FF), Color.White)
                                        )
                                    )
                                    .padding(22.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFDBEAFE),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = "ACADEMIC PROGRAM · ${program?.targetAudience.orEmpty().ifBlank { "All Learners" }}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = program?.title ?: "Program Overview",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E3A8A),
                                            lineHeight = 30.sp
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        Text(
                                            text = program?.description ?: "",
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 19.sp
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    MascotView(
                                        modifier = Modifier.size(110.dp),
                                        expression = MascotExpression.HAPPY,
                                        enableAnimation = true
                                    )
                                }

                                if (program?.learningObjectives?.isNotEmpty() == true) {
                                    Spacer(Modifier.height(16.dp))

                                    Surface(
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Core Learning Objectives:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E3A8A)
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            program.learningObjectives.forEach { objective ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = LingoGreen,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = objective,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF334155)
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

                // Section 2: Levels Section Header
                item {
                    EntranceWrapper(delayMillis = 200) {
                        Text(
                            text = "Curriculum Levels",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                // Section 3: Level Cards or Empty State
                if (uiState.levels.isEmpty()) {
                    item {
                        EntranceWrapper(delayMillis = 300) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MascotView(
                                        modifier = Modifier.size(100.dp),
                                        expression = MascotExpression.THINKING
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = "No levels available yet",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = "Learning levels for this program are being prepared. Please check back later!",
                                        fontSize = 13.sp,
                                        color = LingoGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = onBackClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue),
                                        shape = RoundedCornerShape(22.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Text("Back to Program Selection", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.levels.size) { index ->
                        val level = uiState.levels[index]
                        EntranceWrapper(delayMillis = 300 + (index * 100)) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectLevel(level.id) }
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "LEVEL ${level.order}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF047857),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "${level.xpRequired} XP Goal",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = level.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E3A8A)
                                    )

                                    if (level.description.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = level.description,
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = { onSelectLevel(level.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        shape = RoundedCornerShape(23.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Explore Level Curriculum", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.width(8.dp))
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
