package com.example.mystudyapp.ui.component.speech

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.mystudyapp.speech.PronunciationScoreResult
import com.example.mystudyapp.speech.PronunciationScorer
import com.example.mystudyapp.speech.SpeechRecognitionState
import com.example.mystudyapp.speech.SpeechRecognizerHelper
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGreen

@Composable
fun PronunciationPracticeModal(
    targetWord: String,
    phonetic: String = "",
    meaningVi: String = "",
    onPlayAudio: () -> Unit = {},
    onScoreEarned: (score: Int, result: PronunciationScoreResult) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val speechHelper = remember { SpeechRecognizerHelper(context) }
    val speechState by speechHelper.state.collectAsState()

    var scoreResult by remember { mutableStateOf<PronunciationScoreResult?>(null) }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedMessage = null
            speechHelper.startListening()
        } else {
            permissionDeniedMessage = "Ứng dụng cần quyền Micro để nhận diện và chấm điểm giọng nói của bạn."
        }
    }

    fun requestAndStart() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            permissionDeniedMessage = null
            speechHelper.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val matches = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val evaluated = PronunciationScorer.evaluate(targetWord, matches)
                scoreResult = evaluated
                onScoreEarned(evaluated.score, evaluated)
            }
        }
    }

    fun launchSystemSpeechRecognizer() {
        try {
            speechHelper.reset()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Đọc to từ tiếng Anh: $targetWord")
            }
            systemSpeechLauncher.launch(intent)
        } catch (e: Exception) {
            requestAndStart()
        }
    }

    LaunchedEffect(speechState) {
        val current = speechState
        if (current is SpeechRecognitionState.Success) {
            val evaluated = PronunciationScorer.evaluate(targetWord, current.allCandidates)
            scoreResult = evaluated
            onScoreEarned(evaluated.score, evaluated)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    Dialog(
        onDismissRequest = {
            speechHelper.reset()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        speechHelper.reset()
                        onDismiss()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 460.dp)
                    .padding(vertical = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept click so taps inside don't dismiss
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // 1. TOP HEADER (Tag & Close button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "CHẤM ĐIỂM PHÁT ÂM AI",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            speechHelper.reset()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 2. TARGET WORD CARD BANNER
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = targetWord,
                            fontSize = if (targetWord.length > 10) 24.sp else 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )

                        if (phonetic.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFCCFBF1)
                            ) {
                                Text(
                                    text = phonetic,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F766E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (meaningVi.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = meaningVi,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Audio button to hear native speaker
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.clickable { onPlayAudio() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFF1D4ED8),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Nghe mẫu chuẩn",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 3. MAIN INTERACTIVE VOICE STAGE (Constrained minHeight to stabilize dialog)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (permissionDeniedMessage != null) {
                        PermissionDeniedView(
                            message = permissionDeniedMessage!!,
                            onRetry = { requestAndStart() }
                        )
                    } else if (scoreResult != null) {
                        // RESULT / SCORE VIEW
                        PronunciationResultView(
                            result = scoreResult!!,
                            onRetry = {
                                scoreResult = null
                                requestAndStart()
                            },
                            onPlayAudio = onPlayAudio,
                            onDone = {
                                speechHelper.reset()
                                onDismiss()
                            }
                        )
                    } else {
                        // RECORDING STATE VIEW
                        when (val state = speechState) {
                            is SpeechRecognitionState.Idle -> {
                                IdleMicView(
                                    onStartRecording = { requestAndStart() },
                                    onUseSystemSpeech = { launchSystemSpeechRecognizer() }
                                )
                            }
                            is SpeechRecognitionState.Ready,
                            is SpeechRecognitionState.Listening -> {
                                val listeningState = state as? SpeechRecognitionState.Listening
                                val volume = listeningState?.volumeLevel ?: 0.1f
                                val partial = listeningState?.partialText ?: ""
                                ActiveListeningMicView(
                                    volumeLevel = volume,
                                    partialText = partial,
                                    onStop = { speechHelper.stopListening() }
                                )
                            }
                            is SpeechRecognitionState.Processing -> {
                                ProcessingView()
                            }
                            is SpeechRecognitionState.Error -> {
                                ErrorView(
                                    message = state.message,
                                    onRetry = {
                                        speechHelper.reset()
                                        requestAndStart()
                                    },
                                    onUseSystemSpeech = { launchSystemSpeechRecognizer() }
                                )
                            }
                            is SpeechRecognitionState.Success -> {
                                // Handled by scoreResult above
                                ProcessingView()
                            }
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * State: Idle - Inviting user to tap microphone
 */
@Composable
private fun IdleMicView(
    onStartRecording: () -> Unit,
    onUseSystemSpeech: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .clickable { onStartRecording() },
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Bắt đầu thu âm",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Chạm vào Micro để đọc",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Nói to, rõ ràng vào micro để AI chấm điểm nhé!",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        // Secondary fallback button for system speech
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.clickable { onUseSystemSpeech() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Hoặc dùng nhận diện Google Hệ thống",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}

/**
 * State: Active Listening - Pulsing waves & live sound level visualizer
 */
@Composable
private fun ActiveListeningMicView(
    volumeLevel: Float,
    partialText: String = "",
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listening_wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        // Fixed size container for recording button & ripples (prevents layout boundary re-measurements)
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple wave 1: GPU transform (scale & alpha) with zero layout re-measurement
            val scale1 = waveAnim * (1f + volumeLevel * 0.35f)
            val alpha1 = ((1.45f - waveAnim) * 0.45f).coerceIn(0.05f, 0.4f)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = scale1
                        scaleY = scale1
                        alpha = alpha1
                    }
                    .background(Color(0xFFEF4444), CircleShape)
            )

            // Ripple wave 2
            val scale2 = (1f + volumeLevel * 0.25f)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = scale2
                        scaleY = scale2
                        alpha = 0.18f
                    }
                    .background(Color(0xFFEF4444), CircleShape)
            )

            // Center Recording Mic Button
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onStop() },
                shape = CircleShape,
                color = Color(0xFFEF4444),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Dừng thu âm",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Live Audio Equalizer Bars (Fixed 42.dp height prevents modal vertical shaking)
        Row(
            modifier = Modifier.height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val v = volumeLevel.coerceIn(0.1f, 1f)
            listOf(0.4f, 0.8f, 1.2f, 0.9f, 0.5f).forEach { factor ->
                val barHeight = (10.dp + (26.dp * (v * factor).coerceIn(0f, 1f)))
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFEF4444))
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Đang lắng nghe... Hãy nói đi nào!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Chạm nút đỏ để dừng sớm hoặc dừng nói khi xong",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(Modifier.height(10.dp))

        // Fixed height slot for partial text recognition prevents jumping when text appears
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (partialText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đang nhận diện: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "\"$partialText\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

/**
 * State: Processing - AI analyzing audio
 */
@Composable
private fun ProcessingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(54.dp),
            color = Color(0xFF2563EB),
            strokeWidth = 5.dp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "AI đang chấm điểm phát âm...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Đang phân tích độ chuẩn xác từng âm tiết",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

/**
 * State: Result - Gorgeous Circular Score, Stars, Feedback & Word Analysis
 */
@Composable
private fun PronunciationResultView(
    result: PronunciationScoreResult,
    onRetry: () -> Unit,
    onPlayAudio: () -> Unit,
    onDone: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = result.score.toFloat() / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "score_progress"
    )

    val gradeColor = when {
        result.score >= 90 -> Color(0xFF10B981) // Green
        result.score >= 75 -> Color(0xFF2563EB) // Blue
        result.score >= 50 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444)               // Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Score Gauge Circle
        Box(
            modifier = Modifier.size(116.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(104.dp)) {
                // Background Track
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                // Animated Progress Track
                drawArc(
                    color = gradeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${result.score}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = gradeColor
                )
                Text(
                    text = "Độ chuẩn",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // 3 Stars Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                val isEarned = (i <= result.stars)
                Icon(
                    imageVector = if (isEarned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isEarned) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Feedback Text
        Text(
            text = result.feedbackVi,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = gradeColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        // Detailed "Bạn vừa nói:" card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Máy nghe thấy bạn nói:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "\"${result.spokenText.ifBlank { "(không rõ)" }}\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )

                Spacer(Modifier.height(10.dp))

                // Letter matching visualizer
                Text(
                    text = "Độ khớp các ký tự:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    result.characterMatches.forEach { match ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (match.isMatch) Color(0xFFDCFCE7) else Color(0xFFFFE4E6),
                            border = BorderStroke(
                                1.dp,
                                if (match.isMatch) Color(0xFF86EFAC) else Color(0xFFFDA4AF)
                            )
                        ) {
                            Text(
                                text = match.char.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (match.isMatch) Color(0xFF166534) else Color(0xFF9F1239),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // XP Bonus chip if score >= 75%
        if (result.score >= 75) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "+5 XP Thưởng phát âm chuẩn!",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Action Buttons Row: [Thử lại] & [Xong / Tiếp tục]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4ED8)),
                border = BorderStroke(1.5.dp, Color(0xFF93C5FD))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Đọc lại", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Xong", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * State: Error
 */
@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onUseSystemSpeech: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFE4E6),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFFE11D48),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Thử lại", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onUseSystemSpeech,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFF3B82F6))
            ) {
                Text("Google Hệ thống", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }
    }
}

/**
 * State: Permission Denied guide
 */
@Composable
private fun PermissionDeniedView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFEF3C7),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Yêu cầu quyền Micro",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Cấp quyền Micro", fontWeight = FontWeight.Bold)
        }
    }
}
