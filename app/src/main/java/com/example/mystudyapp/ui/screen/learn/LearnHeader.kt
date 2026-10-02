package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.LingoStatChip
import com.example.mystudyapp.viewmodel.LearnUiState
import kotlinx.coroutines.delay

@Composable
fun LearnHeader(
    uiState: LearnUiState = LearnUiState(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFCBE7FF), Color(0xFFE2F3FF))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
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
                    text = "Learn",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )
                
                Spacer(Modifier.weight(1f))
                
                LingoStatChip(
                    emoji = "\uD83D\uDD25",
                    value = uiState.streak.toString(),
                    label = "Day streak"
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Choose a skill and\nkeep moving forward!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3B82F6),
                lineHeight = 22.sp
            )
        }

        var mascotVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            mascotVisible = true
        }
        val mascotOffsetY by animateFloatAsState(
            targetValue = if (mascotVisible) 10f else 60f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
            label = "mascot_y"
        )
        val mascotAlpha by animateFloatAsState(
            targetValue = if (mascotVisible) 1f else 0f,
            animationSpec = tween(500),
            label = "mascot_alpha"
        )

        MascotView(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(210.dp)
                .offset(x = 10.dp, y = mascotOffsetY.dp)
                .graphicsLayer { alpha = mascotAlpha },
            expression = MascotExpression.HAPPY,
            enableAnimation = true
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 32.dp, y = (-20).dp)
        ) {
            LearnSpeechBubble(
                text = "Let's learn\nsomething new\ntoday! \uD83D\uDE42",
                delayMillis = 400
            )
        }
    }
}

@Composable
private fun LearnSpeechBubble(text: String, delayMillis: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "bubble_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "bubble_scale"
    )

    Surface(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
            this.transformOrigin = TransformOrigin(1f, 1f)
        },
        color = Color.White,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomEnd = 4.dp,
            bottomStart = 24.dp
        ),
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
