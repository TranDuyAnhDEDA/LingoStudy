package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
import com.example.mystudyapp.ui.theme.CategoryGrammarBg
import com.example.mystudyapp.ui.theme.CategoryListeningBg
import com.example.mystudyapp.ui.theme.CategoryPronunBg
import com.example.mystudyapp.ui.theme.CategoryVocabBg
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.ui.theme.LingoPink
import com.example.mystudyapp.ui.theme.LingoPurple
import com.example.mystudyapp.ui.theme.LingoYellow
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
import com.example.mystudyapp.viewmodel.LearnViewModel
import com.example.mystudyapp.viewmodel.LearnViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun LearnScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: LearnViewModel = viewModel(factory = LearnViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNav by remember { mutableIntStateOf(1) }

    val currentLessonId = uiState.units
        .flatMap { it.lessons }
        .firstOrNull { it.isCurrentLesson }?.lessonId
        ?: uiState.units.firstOrNull()?.lessons?.firstOrNull()?.lessonId
        ?: "lesson_3_1_1"

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
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { LearnHeader(uiState = uiState) }
                
                item { Spacer(Modifier.height(16.dp)) }
                
                item { 
                    EntranceWrapper(delayMillis = 150) {
                        LearningPathCard(
                            uiState = uiState,
                            onViewAllClick = { onNavigate("lesson/$currentLessonId") }
                        )
                    }
                }
                
                item { Spacer(Modifier.height(24.dp)) }
                
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EntranceWrapper(delayMillis = 300) {
                            SkillCard(
                                title = "Vocabulary",
                                subtitle = "Build your word power",
                                emoji = "\uD83D\uDCD6",
                                progressPct = uiState.vocabularyProgress,
                                progressColor = LingoPink,
                                backgroundColor = CategoryVocabBg,
                                onClick = { onNavigate("vocabulary/$currentLessonId") }
                            )
                        }
                        EntranceWrapper(delayMillis = 400) {
                            SkillCard(
                                title = "Grammar",
                                subtitle = "Master the rules",
                                emoji = "\uD83D\uDCDD",
                                progressPct = uiState.grammarProgress,
                                progressColor = LingoYellow,
                                backgroundColor = CategoryGrammarBg,
                                onClick = { onNavigate("grammar/$currentLessonId") }
                            )
                        }
                        EntranceWrapper(delayMillis = 500) {
                            SkillCard(
                                title = "Listening",
                                subtitle = "Hear and understand",
                                emoji = "\uD83C\uDFA7",
                                progressPct = uiState.listeningProgress,
                                progressColor = LingoPurple,
                                backgroundColor = CategoryListeningBg,
                                onClick = { onNavigate("listening/$currentLessonId") }
                            )
                        }
                        EntranceWrapper(delayMillis = 600) {
                            SkillCard(
                                title = "Pronunciation",
                                subtitle = "Speak with confidence",
                                emoji = "\uD83C\uDFA4",
                                progressPct = uiState.pronunciationProgress,
                                progressColor = LingoGreen,
                                backgroundColor = CategoryPronunBg,
                                onClick = { onNavigate("pronunciation/$currentLessonId") }
                            )
                        }
                    }
                }
                
                item { Spacer(Modifier.height(24.dp)) }
                
                item { 
                    EntranceWrapper(delayMillis = 700) {
                        DailyPracticeCard(onStartClick = { onNavigate("lesson/$currentLessonId") }) 
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
        animationSpec = tween(600),
        label = "alpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(600, easing = EaseOutQuart),
        label = "offsetY"
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
