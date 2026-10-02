package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudyapp.ui.theme.LingoGray

@Composable
fun SkillCard(
    title: String,
    subtitle: String,
    emoji: String,
    progressPct: Int,
    progressColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Progress bar animation
    var currentProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progressPct) {
        currentProgress = progressPct / 100f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutQuart),
        label = "progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = LingoGray
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Progress & Arrow
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${progressPct}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(progressColor.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(progressColor)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
