package com.example.mystudyapp.ui.component.chatbot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mystudyapp.R
import com.example.mystudyapp.data.model.ChatMessage
import com.example.mystudyapp.data.model.MessageSender
import com.example.mystudyapp.data.model.cleanChatText
import com.example.mystudyapp.ui.component.shared.rememberWindowInfo
import com.example.mystudyapp.viewmodel.ChatbotViewModel

@Composable
fun ParrotChatModal(
    viewModel: ChatbotViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowInfo = rememberWindowInfo()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Outer box: clicking outside dismisses the chat modal with soft dimmed backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = if (windowInfo.isTablet) 24.dp else 10.dp)
                .padding(bottom = 12.dp, top = 20.dp)
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Large chat card: ~85% screen height and wide responsive bounds
            Surface(
                modifier = Modifier
                    .fillMaxWidth(if (windowInfo.isTablet) 0.75f else 1f)
                    .widthIn(max = 580.dp)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept click so taps inside chat card don't dismiss
                    ),
                color = Color.White,
                shadowElevation = 20.dp,
                border = BorderStroke(1.5.dp, Color(0xFFBAE6FD).copy(alpha = 0.9f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Sky & Cloud background matching HomeScreen & DictionaryScreen
                    Image(
                        painter = painterResource(id = R.drawable.bg_home_sky),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Atmospheric soft floating clouds canvas
                    ChatAtmosphericClouds()

                    // Translucent frost layer for maximum readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.35f))
                    )

                    // Foreground chat structure
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header: Close button only (no history button, no gear icon, no API)
                        ChatHeader(
                            onCloseClick = onDismiss
                        )

                        // Messages List floating above cloud background
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.messages, key = { it.id }) { message ->
                                ChatMessageItem(message = message)
                            }
                        }

                        // Quick Prompt Suggestions Row
                        if (uiState.quickSuggestions.isNotEmpty() && !uiState.isGenerating) {
                            Surface(
                                color = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(uiState.quickSuggestions) { prompt ->
                                        QuickPromptChip(
                                            text = prompt,
                                            onClick = { viewModel.sendQuickPrompt(prompt) }
                                        )
                                    }
                                }
                            }
                        }

                        // Input Bar
                        ChatInputBar(
                            text = inputText,
                            onTextChange = { inputText = it },
                            isGenerating = uiState.isGenerating,
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatAtmosphericClouds() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-right soft fluffy cloud cluster
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = w * 0.30f,
            center = Offset(w * 0.90f, h * 0.18f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.40f),
            radius = w * 0.20f,
            center = Offset(w * 0.70f, h * 0.12f)
        )

        // Mid-left soft cloud cluster
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = w * 0.26f,
            center = Offset(w * 0.05f, h * 0.48f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = w * 0.18f,
            center = Offset(w * 0.22f, h * 0.54f)
        )

        // Lower-right cloud puff
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = w * 0.32f,
            center = Offset(w * 0.95f, h * 0.80f)
        )
    }
}

@Composable
private fun ChatHeader(
    onCloseClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.98f),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot Parrot Image inside soft glowing badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_parrot),
                        contentDescription = "Vẹt AI",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trợ lý LingoParrot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 1,
                    softWrap = false
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Trực tuyến • Sẵn sàng hỗ trợ 24/7",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF15803D)
                    )
                }
            }

            // Close button ONLY (✕)
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCloseClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val cleanText = cleanChatText(message.text)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_parrot),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        if (message.isLoading) {
            TypingIndicatorBubble()
        } else {
            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                },
                color = if (isUser) Color(0xFF2563EB) else Color.White.copy(alpha = 0.98f),
                border = if (!isUser) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = if (isUser) 290.dp else 330.dp)
            ) {
                Text(
                    text = cleanText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.5.sp,
                    color = if (isUser) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val alpha1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
        label = "dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse),
        label = "dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse),
        label = "dot3"
    )

    Surface(
        shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Vẹt đang suy nghĩ",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer(alpha = alpha1)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer(alpha = alpha2)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer(alpha = alpha3)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
        }
    }
}

@Composable
private fun QuickPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1D4ED8),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isGenerating: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Nhập câu hỏi của bạn với Vẹt...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 80.dp),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                singleLine = false,
                maxLines = 3
            )

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = if (text.isNotBlank() && !isGenerating) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(enabled = text.isNotBlank() && !isGenerating) { onSend() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (text.isNotBlank() && !isGenerating) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
