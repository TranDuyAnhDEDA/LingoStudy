package com.example.mystudyapp.ui.screen.grammar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGrayLight
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.GrammarUiModel
import com.example.mystudyapp.viewmodel.GrammarViewModel
import com.example.mystudyapp.viewmodel.GrammarViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GrammarScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: GrammarViewModel = viewModel(factory = GrammarViewModelFactory(lessonId))
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onBackToLesson() }
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
                            text = uiState.lessonTitle.ifBlank { "Grammar" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.weight(1f)
                        )

                        if (!uiState.isLoading && uiState.grammarList.isNotEmpty() && !uiState.isFinished) {
                            val currentPos = uiState.currentIndex + 1
                            val totalPos = uiState.grammarList.size
                            Text(
                                text = "$currentPos / $totalPos",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LingoBlue
                            )
                        }
                    }

                    if (!uiState.isLoading && uiState.grammarList.isNotEmpty() && !uiState.isFinished) {
                        Spacer(Modifier.height(10.dp))
                        val progressRatio = (uiState.currentIndex + 1).toFloat() / uiState.grammarList.size
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = LingoBlue,
                            trackColor = LingoGrayLight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.grammarList.isNotEmpty() && !uiState.isFinished) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val isLast = uiState.currentIndex >= uiState.grammarList.size - 1
                        Button(
                            onClick = { viewModel.nextGrammar() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isLast) "Finish Grammar" else "Next",
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
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "An error occurred.",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Back to Lesson")
                    }
                }
            }
        } else if (uiState.grammarList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "No grammar topics available for this lesson.",
                        fontSize = 16.sp,
                        color = LingoGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onBackToLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                    ) {
                        Text("Back to Lesson")
                    }
                }
            }
        } else if (uiState.isFinished) {
            // Completion View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MascotView(
                            modifier = Modifier.size(160.dp),
                            expression = MascotExpression.EXCITED,
                            enableAnimation = true
                        )

                        Spacer(Modifier.height(16.dp))

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LingoGreen,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Grammar Complete!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "${uiState.completedCount} / ${uiState.grammarList.size} topics completed",
                            fontSize = 15.sp,
                            color = LingoGray
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { onBackToLesson() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Text(
                                text = "Back to Lesson",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Active Grammar Card View
            val currentGrammar = uiState.grammarList.getOrNull(uiState.currentIndex)
            if (currentGrammar != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentGrammar,
                        transitionSpec = {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        },
                        label = "grammar_card_transition"
                    ) { grammar ->
                        GrammarCard(grammar = grammar)
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarCard(grammar: GrammarUiModel) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Grammar Topic",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = grammar.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = grammar.explanation,
                fontSize = 15.sp,
                color = Color(0xFF334155),
                lineHeight = 22.sp
            )

            if (grammar.examples.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFF))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Examples:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue
                        )
                        Spacer(Modifier.height(8.dp))
                        grammar.examples.forEach { example ->
                            Text(
                                text = "• $example",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
