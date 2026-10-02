package com.example.mystudyapp.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mystudyapp.R
import com.example.mystudyapp.ui.theme.MyStudyAppTheme

/**
 * Reusable mascot composable for the LingoFlow parrot.
 * 
 * Includes subtle, non-blocking animations for floating and breathing (scaling).
 */
@Composable
fun MascotView(
    modifier: Modifier = Modifier,
    expression: MascotExpression = MascotExpression.HAPPY,
    enableAnimation: Boolean = true,
    animationDurationMs: Int = 1800,
    contentDescription: String = "LingoFlow parrot mascot"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")
    
    val animatedOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMs,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float_y"
    )

    // Subtle breathing animation (scale up slightly)
    // Duration is slightly different to create an organic, out-of-phase feel
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f, 
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMs + 300,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_scale"
    )

    val offsetY = if (enableAnimation) animatedOffsetY else 0f
    val scale = if (enableAnimation) animatedScale else 1f

    Image(
        painter = painterResource(id = mascotDrawableFor(expression)),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .graphicsLayer { 
                translationY = offsetY 
                scaleX = scale
                scaleY = scale
            }
    )
}

@DrawableRes
private fun mascotDrawableFor(expression: MascotExpression): Int = when (expression) {
    MascotExpression.HAPPY    -> R.drawable.mascot_parrot
    MascotExpression.THINKING -> R.drawable.mascot_parrot
    MascotExpression.EXCITED  -> R.drawable.mascot_parrot
    MascotExpression.NEUTRAL  -> R.drawable.mascot_parrot
}

@Preview(name = "Mascot – HAPPY (animated)", showBackground = true, backgroundColor = 0xFFEEF4FF)
@Composable
private fun MascotHappyPreview() {
    MyStudyAppTheme {
        Box(contentAlignment = Alignment.Center) {
            MascotView(modifier = Modifier.size(200.dp))
        }
    }
}
