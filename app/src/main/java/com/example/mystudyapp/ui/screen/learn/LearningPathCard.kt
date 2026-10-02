package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGrayLight
import com.example.mystudyapp.viewmodel.LearnUiState

@Composable
fun LearningPathCard(
    uiState: LearnUiState = LearnUiState(),
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progressRatio = if (uiState.requiredXp > 0) {
        uiState.currentXp.toFloat() / uiState.requiredXp
    } else 0f

    var currentProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progressRatio) {
        currentProgress = progressRatio
    }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutQuart),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "\uD83D\uDDFE", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Your Learning Path",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(Modifier.weight(1f))
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onViewAllClick() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    color = LingoBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LingoBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(LingoBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83D\uDDFA\uFE0F", fontSize = 24.sp)
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (uiState.currentLevelId.isNotBlank()) "Level 3" else "Level 1",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = uiState.levelName.ifBlank { "Explorer" },
                                fontSize = 13.sp,
                                color = LingoGray
                            )
                        }
                        Text(
                            text = "${uiState.currentXp} / ${uiState.requiredXp} XP",
                            fontSize = 12.sp,
                            color = LingoBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(10.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LingoGrayLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(LingoBlue)
                        )
                    }
                }
            }
        }
    }
}
