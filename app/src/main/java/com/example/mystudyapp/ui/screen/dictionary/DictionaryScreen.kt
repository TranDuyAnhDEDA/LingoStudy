package com.example.mystudyapp.ui.screen.dictionary

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.data.model.EnglishMeaningGroup
import com.example.mystudyapp.data.model.VietnameseMeaningGroup
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.LingoBottomNav
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray
import com.example.mystudyapp.ui.theme.LingoGreen
import com.example.mystudyapp.viewmodel.DictionaryViewModel
import com.example.mystudyapp.viewmodel.DictionaryViewModelFactory

@Composable
fun DictionaryScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: DictionaryViewModel = viewModel(factory = DictionaryViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = Color(0xFFF8FAFF),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dictionary Header
            DictionaryHeader()

            // Search Bar
            SearchBarSection(
                query = uiState.searchQuery,
                isLoading = uiState.isLoading,
                onQueryChange = { viewModel.onQueryChanged(it) },
                onClear = { viewModel.clearQuery() },
                onSearch = {
                    keyboardController?.hide()
                    viewModel.searchWord()
                }
            )

            // Content Area
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LingoBlue)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Searching '${uiState.activeQuery}'...",
                            fontSize = 14.sp,
                            color = Color(0xFF1E3A8A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorResultCard(
                        query = uiState.activeQuery,
                        errorMessage = uiState.errorMessage ?: "Word not found."
                    )
                }
            } else if (uiState.result == null) {
                // Initial Intro State
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        IntroCard(
                            sampleWords = uiState.sampleWords,
                            onSampleClick = { word ->
                                viewModel.onQueryChanged(word)
                                viewModel.searchWord(word)
                            }
                        )
                    }
                }
            } else {
                // Result View
                val result = uiState.result
                if (result != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WordHeaderCard(result = result)
                        }

                        if (result.vietnameseMeanings.isNotEmpty()) {
                            item {
                                VietnameseMeaningsSection(meanings = result.vietnameseMeanings)
                            }
                        }

                        if (result.englishMeanings.isNotEmpty()) {
                            item {
                                EnglishMeaningsSection(meanings = result.englishMeanings)
                            }
                        }

                        if (result.synonyms.isNotEmpty() || result.antonyms.isNotEmpty()) {
                            item {
                                RelationsSection(synonyms = result.synonyms, antonyms = result.antonyms)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DictionaryHeader() {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = LingoBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "English - Vietnamese",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Dictionary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )

                Text(
                    text = "Discover new words every day",
                    fontSize = 13.sp,
                    color = LingoGray
                )
            }

            MascotView(
                modifier = Modifier.size(76.dp),
                expression = MascotExpression.HAPPY,
                enableAnimation = true
            )
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search English word...", fontSize = 14.sp, color = LingoGray) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LingoBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFF),
                    unfocusedContainerColor = Color(0xFFF8FAFF)
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = LingoBlue
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = LingoGray
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )

            Spacer(Modifier.width(10.dp))

            Button(
                onClick = onSearch,
                enabled = query.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LingoBlue),
                modifier = Modifier.height(52.dp)
            ) {
                Text("Search", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntroCard(
    sampleWords: List<String>,
    onSampleClick: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEFF6FF), Color.White)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MascotView(
                modifier = Modifier.size(120.dp),
                expression = MascotExpression.HAPPY,
                enableAnimation = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Search an English word to discover its meaning",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Get real-time Vietnamese translations, English definitions, audio pronunciations, and example sentences.",
                fontSize = 13.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Try searching for:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sampleWords.forEach { word ->
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { onSampleClick(word) }
                    ) {
                        Text(
                            text = "🔍 $word",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LingoBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordHeaderCard(result: DictionaryResult) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.word,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A)
                    )

                    if (!result.ipa.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = result.ipa,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (!result.audioUrl.isNullOrBlank()) {
                    AudioPlayerButton(audioUrl = result.audioUrl)
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerButton(audioUrl: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(audioUrl) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore release exception
            }
        }
    }

    Surface(
        color = if (isPlaying) LingoGreen else LingoBlue,
        shape = CircleShape,
        modifier = Modifier
            .size(48.dp)
            .clickable(enabled = !isBuffering) {
                try {
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        isBuffering = true
                        mediaPlayer.reset()
                        mediaPlayer.setAudioAttributes(
                            AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        mediaPlayer.setDataSource(audioUrl)
                        mediaPlayer.setOnPreparedListener { mp ->
                            isBuffering = false
                            isPlaying = true
                            mp.start()
                        }
                        mediaPlayer.setOnCompletionListener {
                            isPlaying = false
                        }
                        mediaPlayer.setOnErrorListener { _, _, _ ->
                            isBuffering = false
                            isPlaying = false
                            true
                        }
                        mediaPlayer.prepareAsync()
                    }
                } catch (e: Exception) {
                    isBuffering = false
                    isPlaying = false
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isBuffering) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Pronounce",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun VietnameseMeaningsSection(meanings: List<VietnameseMeaningGroup>) {
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "VIETNAMESE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Nghĩa tiếng Việt",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = LingoBlue
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    meanings.forEach { group ->
                        Surface(
                            color = Color(0xFFF8FAFF),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = group.partOfSpeech,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                group.definitions.forEach { def ->
                                    Text(
                                        text = "• $def",
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        lineHeight = 18.sp
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

@Composable
private fun EnglishMeaningsSection(meanings: List<EnglishMeaningGroup>) {
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ENGLISH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LingoBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "English Definitions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = LingoBlue
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    meanings.forEach { group ->
                        Surface(
                            color = Color(0xFFF8FAFF),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    color = Color(0xFFDBEAFE),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = group.partOfSpeech.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LingoBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                group.definitions.forEach { item ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "• ${item.definition}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 18.sp
                                        )
                                        if (!item.example.isNullOrBlank()) {
                                            Text(
                                                text = "   Example: \"${item.example}\"",
                                                fontSize = 12.sp,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(top = 2.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelationsSection(synonyms: List<String>, antonyms: List<String>) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (synonyms.isNotEmpty()) {
                Text(
                    text = "Synonyms (Đồng nghĩa)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    synonyms.take(10).forEach { syn ->
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = syn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = LingoBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (synonyms.isNotEmpty() && antonyms.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
            }

            if (antonyms.isNotEmpty()) {
                Text(
                    text = "Antonyms (Trái nghĩa)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    antonyms.take(10).forEach { ant ->
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = ant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorResultCard(query: String, errorMessage: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MascotView(
                modifier = Modifier.size(100.dp),
                expression = MascotExpression.THINKING
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "No definitions found for '$query'",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = errorMessage,
                fontSize = 13.sp,
                color = LingoGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
