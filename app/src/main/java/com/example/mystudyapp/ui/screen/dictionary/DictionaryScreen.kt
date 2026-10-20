package com.example.mystudyapp.ui.screen.dictionary

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
import com.example.mystudyapp.ui.component.shared.rememberWindowInfo
import com.example.mystudyapp.ui.component.shared.responsiveContentWidth
import com.example.mystudyapp.viewmodel.DictionaryUiState
import com.example.mystudyapp.viewmodel.DictionaryViewModel
import com.example.mystudyapp.viewmodel.DictionaryViewModelFactory
import com.example.mystudyapp.viewmodel.PopularWordItem
import java.util.Locale

@Composable
fun DictionaryScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: DictionaryViewModel = viewModel(factory = DictionaryViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val windowInfo = rememberWindowInfo()

    // Preload default popular word "apple" so user immediately sees rich UI matching reference
    LaunchedEffect(Unit) {
        if (uiState.result == null && uiState.searchQuery.isEmpty()) {
            viewModel.searchWord("apple")
        }
    }

    // TextToSpeech Engine initialization with lifecycle management
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.US
            }
        }
        tts = ttsInstance
        onDispose {
            try {
                ttsInstance?.stop()
                ttsInstance?.shutdown()
            } catch (e: Exception) {
                // Ignore shutdown exceptions
            }
        }
    }

    // Audio pronounce handler using either remote URL or on-device TTS
    val playPronounce: (String, String?) -> Unit = { word, audioUrl ->
        if (!audioUrl.isNullOrBlank()) {
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(audioUrl)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { it.release() }
                    setOnErrorListener { _, _, _ ->
                        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "dict_tts")
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "dict_tts")
            }
        } else {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "dict_tts")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            LingoBottomNav(
                selectedIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigate("home")
                        1 -> onNavigate("learn")
                        2 -> onNavigate("dictionary")
                        3 -> onNavigate("me")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD6ECFD),
                            Color(0xFFE8F4FE),
                            Color(0xFFF6FAFD),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        ) {
            // Atmospheric soft floating clouds background
            AtmosphericCloudsCanvas()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .responsiveContentWidth(680.dp)
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = if (windowInfo.isCompact) 12.dp else 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(if (windowInfo.isCompact) 12.dp else 16.dp)
                ) {
                    // 1. Header (Back button, Title "Từ điển", Subtitle)
                    item {
                        DictionaryTopHeader(
                            isCompact = windowInfo.isCompact,
                            onBack = { onNavigate("home") }
                        )
                    }

                    // 2. Search Bar
                    item {
                        DictionarySearchBar(
                            query = uiState.searchQuery,
                            isLoading = uiState.isLoading,
                            onQueryChange = { viewModel.onQueryChanged(it) },
                            onClear = { viewModel.clearQuery() },
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.searchWord()
                            }
                        )
                    }

                    // 3. Section "Từ vựng phổ biến" (8 chips: apple, house, cat, dog, sun, tree, book, school)
                    item {
                        PopularVocabularySection(
                            items = uiState.popularWords,
                            isCompact = windowInfo.isCompact,
                            onWordClick = { word ->
                                keyboardController?.hide()
                                viewModel.onQueryChanged(word)
                                viewModel.searchWord(word)
                            }
                        )
                    }

                    // 4. Loading State
                    if (uiState.isLoading) {
                        item {
                            Surface(
                                color = Color.White.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(26.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Đang tra cứu từ \"${uiState.activeQuery}\"...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Error State
                    else if (uiState.errorMessage != null) {
                        item {
                            Surface(
                                color = Color(0xFFFFF1F2),
                                shape = RoundedCornerShape(26.dp),
                                border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Không tìm thấy từ \"${uiState.activeQuery}\"",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFBE123C),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Hãy kiểm tra lại chính tả hoặc chọn một từ phổ biến bên trên để tra cứu nhé!",
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 6. Search Result Card (Exact reference design 1)
                    val result = uiState.result
                    if (result != null && !uiState.isLoading) {
                        item {
                            SearchResultCard(
                                result = result,
                                isCompact = windowInfo.isCompact,
                                onPlayPronounce = { playPronounce(result.word, result.audioUrl) }
                            )
                        }
                    }

                    // 7. Section "Lịch sử tra cứu"
                    item {
                        SearchHistorySection(
                            history = uiState.searchHistory,
                            onWordClick = { word ->
                                keyboardController?.hide()
                                viewModel.onQueryChanged(word)
                                viewModel.searchWord(word)
                            },
                            onRemoveWord = { word ->
                                viewModel.removeHistoryItem(word)
                            },
                            onViewAll = {
                                viewModel.setShowAllHistoryDialog(true)
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Full History Modal Dialog
    if (uiState.showAllHistoryDialog) {
        AllHistoryDialog(
            history = uiState.searchHistory,
            onDismiss = { viewModel.setShowAllHistoryDialog(false) },
            onWordClick = { word ->
                viewModel.setShowAllHistoryDialog(false)
                keyboardController?.hide()
                viewModel.onQueryChanged(word)
                viewModel.searchWord(word)
            },
            onRemoveWord = { word ->
                viewModel.removeHistoryItem(word)
            },
            onClearAll = {
                viewModel.clearAllHistory()
            }
        )
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

@Composable
private fun AtmosphericCloudsCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width

        // Soft cloud puff 1 (Top right)
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = w * 0.32f,
            center = Offset(w * 0.90f, 60.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = w * 0.22f,
            center = Offset(w * 0.76f, 100.dp.toPx())
        )

        // Soft cloud puff 2 (Middle left)
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = w * 0.28f,
            center = Offset(w * 0.05f, 280.dp.toPx())
        )

        // Soft cloud puff 3 (Lower right)
        drawCircle(
            color = Color.White.copy(alpha = 0.28f),
            radius = w * 0.35f,
            center = Offset(w * 0.95f, 480.dp.toPx())
        )
    }
}

@Composable
private fun DictionaryTopHeader(
    isCompact: Boolean = false,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular back button
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(if (isCompact) 38.dp else 42.dp)
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                )
            }
        }

        Spacer(Modifier.width(if (isCompact) 10.dp else 14.dp))

        Column {
            Text(
                text = "Từ điển",
                fontSize = if (isCompact) 20.sp else 23.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E3A8A)
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "Tra cứu từ vựng nhanh chóng",
                fontSize = if (isCompact) 12.sp else 13.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DictionarySearchBar(
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Nhập từ cần tra cứu",
                        fontSize = 15.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A8A)
                    ),
                    cursorBrush = SolidColor(Color(0xFF2563EB)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF2563EB),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            } else if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Xóa",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularVocabularySection(
    items: List<PopularWordItem>,
    isCompact: Boolean = false,
    onWordClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFEA580C),
                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Từ vựng phổ biến",
                fontSize = if (isCompact) 15.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
        }

        Spacer(Modifier.height(8.dp))

        // 2 rows of 4 columns
        val row1 = items.take(4)
        val row2 = items.drop(4).take(4)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row1.forEach { item ->
                    PopularWordChip(
                        item = item,
                        isCompact = isCompact,
                        modifier = Modifier.weight(1f),
                        onClick = { onWordClick(item.word) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row2.forEach { item ->
                    PopularWordChip(
                        item = item,
                        isCompact = isCompact,
                        modifier = Modifier.weight(1f),
                        onClick = { onWordClick(item.word) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularWordChip(
    item: PopularWordItem,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .height(if (isCompact) 38.dp else 42.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isCompact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            DictionaryIllustration(
                word = item.illustrationType,
                modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
            )
            Spacer(Modifier.width(if (isCompact) 3.dp else 5.dp))
            Text(
                text = item.word,
                fontSize = if (isCompact) 11.5.sp else 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: DictionaryResult,
    isCompact: Boolean = false,
    onPlayPronounce: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 14.dp else 18.dp)) {
            // Card Header: Badge
            Surface(
                color = Color(0xFFDBEAFE),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Kết quả tra cứu",
                    fontSize = if (isCompact) 11.sp else 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(if (isCompact) 10.dp else 14.dp))

            // Main Info: Illustration + Word Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Squircle Container
                val squircleSize = if (isCompact) 80.dp else 105.dp
                val illSize = if (isCompact) 56.dp else 76.dp
                Surface(
                    shape = RoundedCornerShape(if (isCompact) 18.dp else 22.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    modifier = Modifier.size(squircleSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DictionaryIllustration(
                            word = result.word,
                            modifier = Modifier.size(illSize)
                        )
                    }
                }

                Spacer(Modifier.width(if (isCompact) 10.dp else 14.dp))

                // Right Column Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = result.word,
                                fontSize = if (isCompact) 20.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A),
                                maxLines = 2,
                                lineHeight = if (isCompact) 23.sp else 27.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        // Audio button
                        val audioSize = if (isCompact) 34.dp else 38.dp
                        val audioIconSize = if (isCompact) 17.dp else 19.dp
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier
                                .size(audioSize)
                                .clickable { onPlayPronounce() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Phát âm",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(audioIconSize)
                                )
                            }
                        }
                    }

                    if (!result.ipa.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = result.ipa,
                            fontSize = if (isCompact) 13.5.sp else 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Part of speech badge
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = result.displayPartOfSpeech,
                            fontSize = if (isCompact) 10.5.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Primary Vietnamese meaning
                    Text(
                        text = result.displayMeaningVi,
                        fontSize = if (isCompact) 16.sp else 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A),
                        lineHeight = if (isCompact) 20.sp else 23.sp,
                        maxLines = 3
                    )
                }
            }

            val exampleEn = result.displayExampleEn
            val exampleVi = result.displayExampleVi

            if (!exampleEn.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                // Example Section
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Ví dụ:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Highlight searched word in example sentence
                val targetWord = result.word.lowercase()
                val exampleText = exampleEn
                val lowerExample = exampleText.lowercase()
                val wordIndex = lowerExample.indexOf(targetWord)

                val annotated = buildAnnotatedString {
                    if (wordIndex >= 0) {
                        append(exampleText.substring(0, wordIndex))
                        withStyle(style = SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold)) {
                            append(exampleText.substring(wordIndex, wordIndex + targetWord.length))
                        }
                        append(exampleText.substring(wordIndex + targetWord.length))
                    } else {
                        append(exampleText)
                    }
                }

                Text(
                    text = annotated,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                if (!exampleVi.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = exampleVi,
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<String>,
    onWordClick: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Lịch sử tra cứu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )
            }

            if (history.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAll() }
                ) {
                    Text(
                        text = "Xem tất cả",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (history.isEmpty()) {
            Text(
                text = "Chưa có lịch sử tra cứu nào.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(history.take(6)) { word ->
                    HistoryChip(
                        word = word,
                        onClick = { onWordClick(word) },
                        onRemove = { onRemoveWord(word) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryChip(
    word: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = word,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun AllHistoryDialog(
    history: List<String>,
    onDismiss: () -> Unit,
    onWordClick: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Lịch sử tra cứu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }
        },
        text = {
            if (history.isEmpty()) {
                Text(
                    text = "Lịch sử tra cứu đang trống.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { word ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWordClick(word) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = word,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveWord(word) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Xóa tất cả", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
            }
        }
    )
}
