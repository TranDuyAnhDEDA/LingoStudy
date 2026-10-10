package com.example.mystudyapp.ui.screen.learn

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.viewmodel.LevelUnitsViewModel
import com.example.mystudyapp.viewmodel.LevelUnitsViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun LevelUnitsScreen(
    levelId: String,
    onBackClick: () -> Unit = {},
    onSelectUnit: (String) -> Unit = {},
    viewModel: LevelUnitsViewModel = viewModel(factory = LevelUnitsViewModelFactory(levelId))
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
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
                        text = uiState.level?.name ?: "Level Journey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner: Adventure Map Header
                item {
                    EntranceWrapper(delayMillis = 100) {
                        val level = uiState.level
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFFECFDF5), Color.White)
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFA7F3D0),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "LEVEL ${level?.order ?: 1} · ADVENTURE MAP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = level?.name ?: "Learning Journey",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )

                                if (level?.description?.isNotBlank() == true) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = level.description,
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = LingoBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.units.size} Milestone Units Connected",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Curriculum Path",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                // Illustrated Winding Journey Map
                items(uiState.units.size) { index ->
                    val unit = uiState.units[index]

                    val topicIcon = when (index % 10) {
                        0 -> Icons.Default.School
                        1 -> Icons.Default.Explore
                        2 -> Icons.Default.People
                        3 -> Icons.Default.Schedule
                        4 -> Icons.Default.Restaurant
                        5 -> Icons.Default.ShoppingBag
                        6 -> Icons.Default.Home
                        7 -> Icons.Default.Place
                        8 -> Icons.Default.FlightTakeoff
                        else -> Icons.Default.Star
                    }

                    val cardBg = when (index % 4) {
                        0 -> Color(0xFFEFF6FF)
                        1 -> Color(0xFFECFDF5)
                        2 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFF3E8FF)
                    }

                    EntranceWrapper(delayMillis = 200 + (index * 80)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Left Node / Path Timeline Column
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(48.dp)
                            ) {
                                Surface(
                                    color = LingoBlue,
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${unit.order}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (index < uiState.units.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .fillMaxHeight()
                                            .background(Color(0xFFCBD5E1))
                                    )
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            // Right Milestone Card
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = if (index < uiState.units.size - 1) 18.dp else 0.dp)
                            ) {
                                Surface(
                                    color = cardBg,
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectUnit(unit.id) }
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.85f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "MILESTONE ${unit.order}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF1E3A8A),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }

                                            Surface(
                                                color = Color.White.copy(alpha = 0.85f),
                                                shape = CircleShape,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = topicIcon,
                                                        contentDescription = null,
                                                        tint = LingoBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        Text(
                                            text = unit.title,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E3A8A)
                                        )

                                        if (unit.description.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = unit.description,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155),
                                                lineHeight = 18.sp
                                            )
                                        }

                                        Spacer(Modifier.height(14.dp))

                                        Button(
                                            onClick = { onSelectUnit(unit.id) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(22.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LingoBlue)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Explore Unit Journey", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            }
                        }
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
        animationSpec = tween(500),
        label = "entrance_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(500, easing = EaseOutQuart),
        label = "entrance_offsetY"
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
