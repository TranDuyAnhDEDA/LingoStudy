package com.example.mystudyapp.ui.component.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudyapp.R
import kotlin.math.roundToInt

@Composable
fun ParrotFloatingMascot(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_mascot_anim")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    val idleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_scale"
    )

    // Dragging state and offsets
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(true) }

    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.12f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "mascot_drag_scale"
    )

    // Screen dimensions & bounds clamping
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val mascotSizePx = with(density) { 72.dp.toPx() }
    val endPaddingPx = with(density) { 12.dp.toPx() }
    val bottomPaddingPx = with(density) { 80.dp.toPx() }
    val marginPx = with(density) { 8.dp.toPx() }
    val topMarginPx = with(density) { 56.dp.toPx() }

    val minOffsetX = -(screenWidthPx - mascotSizePx - endPaddingPx - marginPx).coerceAtLeast(0f)
    val maxOffsetX = (endPaddingPx - marginPx).coerceAtLeast(0f)
    val minOffsetY = -(screenHeightPx - mascotSizePx - bottomPaddingPx - topMarginPx).coerceAtLeast(0f)
    val maxOffsetY = 15f // Small leeway downward, but stay above bottom navigation bar

    val clampedX = if (minOffsetX <= maxOffsetX) offsetX.coerceIn(minOffsetX, maxOffsetX) else offsetX
    val clampedY = if (minOffsetY <= maxOffsetY) offsetY.coerceIn(minOffsetY, maxOffsetY) else offsetY

    // Show tooltip bubble only when mascot is on the right side and not dragging
    val canShowTooltip = showTooltip && !isDragging && (clampedX > -(screenWidthPx / 3f))

    Row(
        modifier = modifier
            .offset {
                IntOffset(
                    clampedX.roundToInt(),
                    (clampedY + if (isDragging) 0f else floatOffsetY).roundToInt()
                )
            }
            .padding(end = 12.dp, bottom = 80.dp)
            .pointerInput(onClick) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var isDrag = false
                    val touchSlop = viewConfiguration.touchSlop
                    var totalDistance = 0f

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (change.changedToUp()) {
                                if (!isDrag) {
                                    onClick()
                                }
                                break
                            }

                            val dragAmount = change.position - change.previousPosition
                            totalDistance += dragAmount.getDistance()

                            if (!isDrag && totalDistance > touchSlop) {
                                isDrag = true
                                isDragging = true
                                showTooltip = false
                            }

                            if (isDrag) {
                                change.consume()
                                val newX = offsetX + dragAmount.x
                                val newY = offsetY + dragAmount.y
                                offsetX = if (minOffsetX <= maxOffsetX) newX.coerceIn(minOffsetX, maxOffsetX) else newX
                                offsetY = if (minOffsetY <= maxOffsetY) newY.coerceIn(minOffsetY, maxOffsetY) else newY
                            }
                        }
                    } finally {
                        isDragging = false
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Helpful Tooltip Bubble (Hỏi Vẹt)
        AnimatedVisibility(
            visible = canShowTooltip,
            enter = fadeIn() + slideInHorizontally { it / 2 },
            exit = fadeOut()
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 4.dp,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Hỏi Vẹt",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }

        // Floating Parrot Mascot
        val effectiveScale = idleScale * dragScale
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer(
                    scaleX = effectiveScale,
                    scaleY = effectiveScale,
                    shadowElevation = if (isDragging) 8f else 0f
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.mascot_parrot),
                contentDescription = "Vẹt gia sư AI",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(68.dp)
            )

            // Sparkle AI badge indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}
