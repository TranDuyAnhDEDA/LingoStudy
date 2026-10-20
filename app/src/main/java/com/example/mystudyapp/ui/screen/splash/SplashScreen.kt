package com.example.mystudyapp.ui.screen.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mystudyapp.R
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Màn hình Splash / Intro chào mừng khi người dùng mở ứng dụng (hiển thị ~2 giây).
 * - Tích hợp hiệu ứng chuyển động điện ảnh (Cinematic Zoom & Fade-in).
 * - 3 chấm loading chạy luân phiên mượt mà theo dạng sóng (wave loading animation).
 */
@Composable
fun SplashScreen(
    durationMillis: Long = 2200L,
    onFinish: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Hiệu ứng mờ dần vào (Fade-in 0.4 -> 1.0)
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    // Hiệu ứng chuyển động phóng to chậm rãi kiểu Cinematic Ken Burns (0.98 -> 1.05)
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.05f else 0.98f,
        animationSpec = tween(durationMillis = durationMillis.toInt(), easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(durationMillis)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81C3FD),
                        Color(0xFF9DD4FE),
                        Color(0xFFE2F2FE),
                        Color.White
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Chạm vào màn hình để lướt nhanh vào app
                onFinish()
            },
        contentAlignment = Alignment.Center
    ) {
        // Khung nội dung ảnh và 3 chấm loading cùng zoom đồng bộ (in-sync Ken Burns motion)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scaleAnim,
                    scaleY = scaleAnim,
                    alpha = alphaAnim
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splashscreen),
                contentDescription = "LingoStudy Splash Screen",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 3 chấm chạy luân phiên như đang loading tại đúng vị trí đám mây dưới
            LoadingDotsWave(
                modifier = Modifier.align(BiasAlignment(0f, 0.76f))
            )
        }
    }
}

/**
 * Hiệu ứng 3 chấm chạy luân phiên dạng sóng (Wave Loading Animation).
 * Từng chấm sẽ nảy nhẹ lên, phóng to và đổi màu xanh đậm luân phiên liên tục.
 */
@Composable
private fun LoadingDotsWave(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsWave")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotsProgress"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0..2).forEach { index ->
            var diff = progress - index
            if (diff < 0f) diff += 3f

            val wave = if (diff in 0f..1f) {
                sin(diff * Math.PI.toFloat())
            } else {
                0f
            }

            val scale = 1.0f + (0.35f * wave)
            val offsetY = (-4.5f * wave).dp
            val alpha = 0.45f + (0.55f * wave)
            val dotColor = if (wave > 0.35f) Color(0xFF1D4ED8) else Color(0xFF93C5FD)

            Box(
                modifier = Modifier
                    .offset(y = offsetY)
                    .size(7.5.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
