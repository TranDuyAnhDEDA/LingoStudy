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
        // Outer box: clicking outside dismisses the chat modal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(
                    start = if (windowInfo.isTablet) 0.dp else 12.dp,
                    end = 12.dp,
                    bottom = if (windowInfo.isCompact) 68.dp else 76.dp
                )
                .imePadding(),
            contentAlignment = if (windowInfo.isTablet) Alignment.BottomEnd else Alignment.BottomCenter
        ) {
            // Responsive chat popup card: wide on phones, constrained on tablet
            Surface(
                modifier = Modifier
                    .fillMaxWidth(if (windowInfo.isTablet) 0.5f else 0.96f)
                    .widthIn(max = 420.dp)
                    .fillMaxHeight(if (windowInfo.isShortScreen) 0.70f else if (windowInfo.isCompact) 0.55f else 0.48f)
                    .heightIn(min = 340.dp, max = 560.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept click so taps inside chat card don't dismiss
                    ),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.5.dp, Color(0xFFBAE6FD).copy(alpha = 0.85f))
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
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot Parrot Image inside soft glowing badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_parrot),
                        contentDescription = "Vẹt AI",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trợ lý LingoParrot",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 1,
                    softWrap = false
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "Trực tuyến • Sẵn sàng hỗ trợ",
                        fontSize = 8.8.sp,
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
                    .size(24.dp)
                    .clickable { onCloseClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(15.dp)
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
                    .size(22.dp)
                    .padding(top = 1.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_parrot),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(Modifier.width(5.dp))
        }

        if (message.isLoading) {
            TypingIndicatorBubble()
        } else {
            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 3.dp)
                } else {
                    RoundedCornerShape(topStart = 3.dp, topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 15.dp)
                },
                color = if (isUser) Color(0xFF2563EB) else Color.White.copy(alpha = 0.98f),
                border = if (!isUser) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = if (isUser) 195.dp else 225.dp)
            ) {
                Text(
                    text = cleanText,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    color = if (isUser) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
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
        shape = RoundedCornerShape(3.dp, 14.dp, 14.dp, 14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            Text(
                text = "Đang suy nghĩ",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .graphicsLayer(alpha = alpha1)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .graphicsLayer(alpha = alpha2)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        shadowElevation = 0.5.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1D4ED8),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
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
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Nhập câu hỏi của bạn",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 34.dp, max = 56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                singleLine = false,
                maxLines = 2
            )

            Spacer(Modifier.width(5.dp))

            Surface(
                shape = CircleShape,
                color = if (text.isNotBlank() && !isGenerating) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                modifier = Modifier
                    .size(32.dp)
                    .clickable(enabled = text.isNotBlank() && !isGenerating) { onSend() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (text.isNotBlank() && !isGenerating) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
