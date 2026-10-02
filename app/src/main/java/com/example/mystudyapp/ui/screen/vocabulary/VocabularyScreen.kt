package com.example.mystudyapp.ui.screen.vocabulary

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
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.mystudyapp.viewmodel.VocabularyUiModel
import com.example.mystudyapp.viewmodel.VocabularyViewModel
import com.example.mystudyapp.viewmodel.VocabularyViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VocabularyScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: VocabularyViewModel = viewModel(factory = VocabularyViewModelFactory(lessonId))
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
                            text = uiState.lessonTitle.ifBlank { "Vocabulary" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.weight(1f)
                        )

                        if (!uiState.isLoading && uiState.vocabularyList.isNotEmpty() && !uiState.isFinished) {
                            val currentPos = uiState.currentIndex + 1
                            val totalPos = uiState.vocabularyList.size
                            Text(
                                text = "$currentPos / $totalPos",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LingoBlue
                            )
                        }
                    }

                    if (!uiState.isLoading && uiState.vocabularyList.isNotEmpty() && !uiState.isFinished) {
                        Spacer(Modifier.height(10.dp))
                        val progressRatio = (uiState.currentIndex + 1).toFloat() / uiState.vocabularyList.size
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
            if (!uiState.isLoading && uiState.vocabularyList.isNotEmpty() && !uiState.isFinished) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val isLast = uiState.currentIndex >= uiState.vocabularyList.size - 1
                        Button(
                            onClick = { viewModel.nextVocabulary() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isLast) "Finish Vocabulary" else "Next",
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
        } else if (uiState.vocabularyList.isEmpty()) {
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
                        text = "No vocabulary available for this lesson.",
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
            // Completion Screen View
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
                            text = "Vocabulary Complete!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "${uiState.completedCount} / ${uiState.vocabularyList.size} words completed",
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
            // Active Vocabulary Card View
            val currentVocab = uiState.vocabularyList.getOrNull(uiState.currentIndex)
            if (currentVocab != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentVocab,
                        transitionSpec = {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        },
                        label = "vocab_card_transition"
                    ) { vocab ->
                        VocabularyCard(vocab = vocab)
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyCard(vocab: VocabularyUiModel) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (vocab.partOfSpeech.isNotBlank()) {
                Surface(
                    color = Color(0xFFDCEEFF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = vocab.partOfSpeech,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingoBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = vocab.word,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = vocab.meaning,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (vocab.example.isNotBlank()) {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFF))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Example:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "\"${vocab.example}\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E3A8A)
                        )
                        if (vocab.translation.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = vocab.translation,
                                fontSize = 13.sp,
                                color = LingoGray
                            )
                        }
                    }
                }
            }
        }
    }
}
