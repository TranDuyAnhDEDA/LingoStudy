package com.example.mystudyapp.ui.screen.vocabulary

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.component.ActiveStudyTimeTracker
import com.example.mystudyapp.viewmodel.VocabularyUiModel
import com.example.mystudyapp.viewmodel.VocabularyViewModel
import com.example.mystudyapp.viewmodel.VocabularyViewModelFactory
import java.util.Locale

@Composable
fun VocabularyScreen(
    lessonId: String,
    onBackToLesson: () -> Unit = {},
    viewModel: VocabularyViewModel = viewModel(factory = VocabularyViewModelFactory(lessonId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(context) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    fun speakWord(word: String) {
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "vocab_tts_id")
    }

    ActiveStudyTimeTracker(lessonId = lessonId, activityName = "vocabulary")

    // Outer sky gradient background with decorative clouds and bottom bushes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBAE0FD),
                        Color(0xFFD6EEFF),
                        Color(0xFFEDF6FF),
                        Color(0xFFF3F9FF)
                    )
                )
            )
    ) {
        // Decorative floating clouds in the background
        FloatingCloudsBackground()

        // Decorative cartoon bushes in bottom corners
        CartoonBushesBackground(modifier = Modifier.align(Alignment.BottomCenter))

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1D4ED8))
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Có lỗi xảy ra.",
                                fontSize = 16.sp,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { onBackToLesson() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Quay lại bài học", color = Color.White)
                            }
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
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Chưa có từ vựng cho bài học này.",
                                fontSize = 16.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { onBackToLesson() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Quay lại bài học", color = Color.White)
                            }
                        }
                    }
                }
            } else if (uiState.isFinished) {
                // =============================================================
                // VOCABULARY COMPLETION VIEW
                // =============================================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(32.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Sparkling Star / Celebration Icon
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEBF5FF),
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Hoàn thành ${uiState.vocabularyList.size}/${uiState.vocabularyList.size} Từ vựng!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Bé đã xem đủ tất cả flashcard từ vựng của bài học này và ghi nhớ thật tốt!",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.restartReview() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEBF5FF),
                                        contentColor = Color(0xFF1D4ED8)
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Ôn lại",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onBackToLesson() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2563EB),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Về bài học",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // =============================================================
                // MAIN FLASHCARD VIEW
                // =============================================================
                val currentVocab = uiState.vocabularyList.getOrNull(uiState.currentIndex)
                if (currentVocab != null) {
                    var isFlipped by remember(currentVocab.id) { mutableStateOf(false) }

                    val rotationY by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        label = "flashcard_3d_flip"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. TOP HEADER BAR
                        FlashcardHeader(
                            unitLabel = uiState.unitLabel,
                            lessonTitle = uiState.lessonTitle,
                            currentIndex = uiState.currentIndex,
                            totalCount = uiState.vocabularyList.size,
                            onBack = onBackToLesson
                        )

                        Spacer(Modifier.height(8.dp))

                        // 2. FLASHCARD DECK (STACK OF CARDS + MAIN CARD)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .heightIn(min = 360.dp, max = 460.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Layered Deck Effect: Background Card 3 (furthest back, tilted left)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .fillMaxHeight(0.94f)
                                    .graphicsLayer {
                                        this.rotationZ = -5.5f
                                        this.translationY = 8f
                                    },
                                shape = RoundedCornerShape(32.dp),
                                color = Color(0x99FFFFFF),
                                border = BorderStroke(1.dp, Color(0xFFD6EEFF))
                            ) {}

                            // Layered Deck Effect: Background Card 2 (middle back, tilted right)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.94f)
                                    .fillMaxHeight(0.97f)
                                    .graphicsLayer {
                                        this.rotationZ = 5.5f
                                        this.translationY = 4f
                                    },
                                shape = RoundedCornerShape(32.dp),
                                color = Color(0xCCFFFFFF),
                                border = BorderStroke(1.dp, Color(0xFFD6EEFF))
                            ) {}

                            // Foreground Main Card with 3D Flip
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.96f)
                                    .fillMaxHeight()
                                    .graphicsLayer {
                                        this.rotationY = rotationY
                                        this.cameraDistance = 16f * density
                                    }
                                    .clickable { isFlipped = !isFlipped },
                                shape = RoundedCornerShape(32.dp),
                                color = Color.White,
                                shadowElevation = 8.dp,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                if (rotationY <= 90f) {
                                    // ----------------------------------------
                                    // FRONT FACE
                                    // ----------------------------------------
                                    FlashcardFrontFace(
                                        vocab = currentVocab,
                                        onPlayAudio = { speakWord(currentVocab.word) }
                                    )
                                } else {
                                    // ----------------------------------------
                                    // BACK FACE (Rotated 180deg to avoid mirroring)
                                    // ----------------------------------------
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { this.rotationY = 180f }
                                    ) {
                                        FlashcardBackFace(
                                            vocab = currentVocab,
                                            onPlayAudio = { speakWord(currentVocab.word) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 3. PROGRESS DOTS
                        ProgressDotsRow(
                            total = uiState.vocabularyList.size,
                            currentIndex = uiState.currentIndex
                        )

                        Spacer(Modifier.height(14.dp))

                        // 4. NAVIGATION BUTTONS (Thẻ trước & Thẻ tiếp theo)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Prev Button
                            val isFirst = uiState.currentIndex == 0
                            Button(
                                onClick = {
                                    if (!isFirst) {
                                        viewModel.previousVocabulary()
                                        isFlipped = false
                                    }
                                },
                                enabled = !isFirst,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDCEEFE),
                                    contentColor = Color(0xFF1D4ED8),
                                    disabledContainerColor = Color(0x66DCEEFE),
                                    disabledContentColor = Color(0x661D4ED8)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Thẻ trước",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Next Button
                            val isLast = uiState.currentIndex == uiState.vocabularyList.lastIndex
                            Button(
                                onClick = {
                                    viewModel.nextVocabulary()
                                    isFlipped = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLast) Color(0xFF2563EB) else Color(0xFFDCEEFE),
                                    contentColor = if (isLast) Color.White else Color(0xFF1D4ED8)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isLast) 2.dp else 0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isLast) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (isLast) "Hoàn thành" else "Thẻ tiếp theo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!isLast) {
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

                        Spacer(Modifier.height(10.dp))

                        // 5. FLIP CARD BUTTON (Lật thẻ xem nghĩa)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .clickable { isFlipped = !isFlipped }
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 22.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isFlipped) "Lật lại mặt trước" else "Lật thẻ xem nghĩa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top Header bar: Back Button + Unit Badge & Lesson Title + Flashcards Counter Badge
 */
@Composable
private fun FlashcardHeader(
    unitLabel: String,
    lessonTitle: String,
    currentIndex: Int,
    totalCount: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(42.dp)
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Center Unit Pill + Lesson Title
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD6EEFF),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = unitLabel.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }

            Text(
                text = lessonTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        // Right Counter Pill: [Cards Icon] 1/10
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFD6EEFF),
            modifier = Modifier.height(34.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${currentIndex + 1}/$totalCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D4ED8)
                )
            }
        }
    }
}

/**
 * Front Face of the Flashcard: Illustration Badge, Word, IPA, Speaker, Meaning Pill
 */
@Composable
private fun FlashcardFrontFace(
    vocab: VocabularyUiModel,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Illustration in Circle Badge with yellow rays
        Box(
            modifier = Modifier
                .size(175.dp),
            contentAlignment = Alignment.Center
        ) {
            // Yellow accent sparkle rays in top-left
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.22f
                val cy = size.height * 0.22f
                val rayColor = Color(0xFFFBBF24)

                // Ray 1
                drawLine(
                    color = rayColor,
                    start = Offset(cx - 18f, cy - 24f),
                    end = Offset(cx - 30f, cy - 36f),
                    strokeWidth = 6f
                )
                // Ray 2
                drawLine(
                    color = rayColor,
                    start = Offset(cx - 6f, cy - 28f),
                    end = Offset(cx - 10f, cy - 44f),
                    strokeWidth = 6f
                )
                // Ray 3
                drawLine(
                    color = rayColor,
                    start = Offset(cx - 28f, cy - 10f),
                    end = Offset(cx - 44f, cy - 14f),
                    strokeWidth = 6f
                )
            }

            // Circular plate
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F4FF),
                modifier = Modifier.size(150.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    VocabularyIllustrationView(
                        word = vocab.word,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
        }

        // Center Word & Phonetics
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = vocab.word.lowercase(),
                fontSize = if (vocab.word.length > 9) 30.sp else if (vocab.word.length > 6) 32.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15489C),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = vocab.phonetic,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
        }

        // Speaker Button
        Surface(
            shape = CircleShape,
            color = Color(0xFFE1F0FF),
            modifier = Modifier
                .size(48.dp)
                .clickable { onPlayAudio() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Meaning Pill
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFE3F2FD),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = vocab.meaningVi ?: vocab.meaning,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

/**
 * Back Face of the Flashcard: Word, IPA, Meaning, Example Sentence, Translation
 */
@Composable
private fun FlashcardBackFace(
    vocab: VocabularyUiModel,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Tag "MẶT SAU THẺ"
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE8F4FF),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "MẶT SAU • GIẢI NGHĨA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1D4ED8),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Word, IPA & Audio Speaker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = vocab.word.replaceFirstChar { it.uppercase() },
                fontSize = if (vocab.word.length > 9) 24.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15489C)
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vocab.phonetic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE1F0FF),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onPlayAudio() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Phát âm",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Meaning Highlight Pill
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFE8F4FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nghĩa tiếng Việt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = vocab.meaningVi ?: vocab.meaning,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Example Sentence Card
        if (vocab.example.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Ví dụ câu:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "\"${vocab.example}\"",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    val trans = vocab.exampleVi ?: vocab.translation
                    if (trans.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "→ $trans",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Flip back hint
        Text(
            text = "Chạm thẻ hoặc nhấn nút bên dưới để lật lại",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Progress Dots Row with animated active dot width
 */
@Composable
private fun ProgressDotsRow(
    total: Int,
    currentIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until total) {
            val isActive = (i == currentIndex)
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 18.dp else 7.dp,
                animationSpec = tween(durationMillis = 250),
                label = "progress_dot_width"
            )
            val dotColor = if (isActive) Color(0xFF2563EB) else Color(0xFFBFDBFE)

            Box(
                modifier = Modifier
                    .size(width = dotWidth, height = 7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dotColor)
            )
        }
    }
}

/**
 * Maps English vocabulary words to cute, crisp cartoon representations
 */
@Composable
private fun VocabularyIllustrationView(
    word: String,
    modifier: Modifier = Modifier
) {
    val clean = word.trim().lowercase()
    val emoji = when (clean) {
        "apple" -> "🍎"
        "ball" -> "⚽"
        "cat", "kitten" -> "🐱"
        "dog", "puppy" -> "🐶"
        "elephant" -> "🐘"
        "fish" -> "🐟"
        "giraffe" -> "🦒"
        "hat" -> "👒"
        "ice cream" -> "🍦"
        "juice" -> "🧃"
        "kite" -> "🪁"
        "lion" -> "🦁"
        "monkey" -> "🐵"
        "nest" -> "🪺"
        "orange" -> "🍊"
        "pencil", "pen" -> "✏️"
        "queen" -> "👑"
        "rabbit", "bunny" -> "🐰"
        "sun" -> "☀️"
        "tiger" -> "🐯"
        "umbrella" -> "☂️"
        "van", "car" -> "🚐"
        "window" -> "🪟"
        "zoo" -> "🦁"
        "one", "1" -> "1️⃣"
        "two", "2" -> "2️⃣"
        "three", "3" -> "3️⃣"
        "four", "4" -> "4️⃣"
        "five", "5" -> "5️⃣"
        "six", "6" -> "6️⃣"
        "seven", "7" -> "7️⃣"
        "eight", "8" -> "8️⃣"
        "nine", "9" -> "9️⃣"
        "ten", "10" -> "🔟"
        "star" -> "⭐"
        "heart" -> "❤️"
        "circle" -> "⭕"
        "square" -> "⏹️"
        "triangle" -> "🔺"
        "red" -> "🔴"
        "blue" -> "🔵"
        "yellow" -> "🟡"
        "green" -> "🟢"
        "pink" -> "🌸"
        "purple" -> "🟣"
        "black" -> "⚫"
        "white" -> "⚪"
        "brown" -> "🟤"
        "rainbow" -> "🌈"
        "draw", "paint" -> "🎨"
        "crayon", "color pencil" -> "🖍️"
        "picture" -> "🖼️"
        "family" -> "👨‍👩‍👧‍👦"
        "dad", "father" -> "👨"
        "mom", "mother" -> "👩"
        "baby" -> "👶"
        "brother", "big brother" -> "👦"
        "sister", "little sister" -> "👧"
        "grandpa", "grandfather" -> "👴"
        "grandma", "grandmother" -> "👵"
        "pet" -> "🐾"
        "house", "home" -> "🏡"
        "book", "read" -> "📖"
        "ruler" -> "📏"
        "eraser" -> "🧼"
        "schoolbag" -> "🎒"
        "desk", "table" -> "🪑"
        "chair" -> "🪑"
        "teacher" -> "👩‍🏫"
        "student" -> "🧑‍🎓"
        "classroom" -> "🏫"
        "board" -> "📋"
        "scissors", "cut" -> "✂️"
        "glue" -> "🧴"
        "paper" -> "📄"
        "bird" -> "🐦"
        "duck" -> "🦆"
        "chicken" -> "🐔"
        "pig" -> "🐷"
        "cow" -> "🐮"
        "sheep" -> "🐑"
        "horse" -> "🐴"
        "farm" -> "🚜"
        "bear" -> "🐻"
        "zebra" -> "🦓"
        "kangaroo" -> "🦘"
        "panda" -> "🐼"
        "flower" -> "🌸"
        "tree", "forest" -> "🌳"
        "river" -> "🌊"
        "cloud" -> "☁️"
        "rain" -> "🌧️"
        "moon" -> "🌙"
        "sky" -> "🌤️"
        "banana" -> "🍌"
        "mango" -> "🥭"
        "strawberry" -> "🍓"
        "grape" -> "🍇"
        "watermelon" -> "🍉"
        "fruit" -> "🍎"
        "milk" -> "🥛"
        "water" -> "💧"
        "bread" -> "🍞"
        "rice" -> "🍚"
        "egg" -> "🥚"
        "soup" -> "🥣"
        "candy" -> "🍬"
        "cookie" -> "🍪"
        "cake" -> "🎂"
        "pizza" -> "🍕"
        "snack" -> "🥨"
        "run" -> "🏃"
        "jump", "hop" -> "🦘"
        "walk" -> "🚶"
        "dance" -> "💃"
        "sing" -> "🎤"
        "swim" -> "🏊"
        "climb" -> "🧗"
        "fly" -> "🕊️"
        "sleep", "bed", "dream" -> "🛌"
        "clock", "time" -> "⏰"
        "night", "good night" -> "🌙"
        "smile", "happy" -> "😊"
        "sad" -> "😢"
        "hello", "hi", "wave", "bye", "goodbye" -> "👋"
        "thank you", "thanks", "please" -> "🙏"
        "friend", "care", "together" -> "🤝"
        "english", "speak" -> "🇬🇧"
        "smart", "superstar" -> "🌟"
        "win", "champion" -> "🏆"
        else -> "📚"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Soft floating cartoon clouds in the background
 */
@Composable
private fun FloatingCloudsBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cloudColor = Color.White.copy(alpha = 0.45f)

        // Cloud 1: Top left
        drawCircle(
            color = cloudColor,
            radius = w * 0.18f,
            center = Offset(w * 0.1f, h * 0.12f)
        )
        drawCircle(
            color = cloudColor,
            radius = w * 0.12f,
            center = Offset(w * 0.24f, h * 0.14f)
        )

        // Cloud 2: Top right
        drawCircle(
            color = cloudColor,
            radius = w * 0.22f,
            center = Offset(w * 0.95f, h * 0.08f)
        )
        drawCircle(
            color = cloudColor,
            radius = w * 0.15f,
            center = Offset(w * 0.8f, h * 0.13f)
        )

        // Cloud 3: Mid left
        drawCircle(
            color = cloudColor.copy(alpha = 0.3f),
            radius = w * 0.16f,
            center = Offset(w * -0.05f, h * 0.45f)
        )

        // Cloud 4: Mid right
        drawCircle(
            color = cloudColor.copy(alpha = 0.35f),
            radius = w * 0.18f,
            center = Offset(w * 1.05f, h * 0.52f)
        )
    }
}

/**
 * Cute rounded cartoon bushes in bottom corners as seen in the reference design
 */
@Composable
private fun CartoonBushesBackground(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val w = size.width
        val h = size.height

        val darkGreen = Color(0xFF38A169)
        val midGreen = Color(0xFF48BB78)
        val lightGreen = Color(0xFF68D391)

        // Bottom Left Foliage
        drawCircle(
            color = darkGreen,
            radius = 48.dp.toPx(),
            center = Offset(-10.dp.toPx(), h + 10.dp.toPx())
        )
        drawCircle(
            color = midGreen,
            radius = 42.dp.toPx(),
            center = Offset(32.dp.toPx(), h + 5.dp.toPx())
        )
        drawCircle(
            color = lightGreen,
            radius = 34.dp.toPx(),
            center = Offset(70.dp.toPx(), h + 12.dp.toPx())
        )

        // Bottom Right Foliage
        drawCircle(
            color = darkGreen,
            radius = 48.dp.toPx(),
            center = Offset(w + 10.dp.toPx(), h + 10.dp.toPx())
        )
        drawCircle(
            color = midGreen,
            radius = 42.dp.toPx(),
            center = Offset(w - 32.dp.toPx(), h + 5.dp.toPx())
        )
        drawCircle(
            color = lightGreen,
            radius = 34.dp.toPx(),
            center = Offset(w - 70.dp.toPx(), h + 12.dp.toPx())
        )
    }
}
