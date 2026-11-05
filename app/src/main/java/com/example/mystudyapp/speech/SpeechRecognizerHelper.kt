package com.example.mystudyapp.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SpeechRecognitionState {
    object Idle : SpeechRecognitionState
    object Ready : SpeechRecognitionState
    data class Listening(val volumeLevel: Float = 0f, val partialText: String = "") : SpeechRecognitionState
    object Processing : SpeechRecognitionState
    data class Success(val spokenText: String, val allCandidates: List<String>) : SpeechRecognitionState
    data class Error(val message: String, val isPermissionDenied: Boolean = false) : SpeechRecognitionState
}

class SpeechRecognizerHelper(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private val cachedMatches = mutableListOf<String>()

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechRecognitionState.Listening(volumeLevel = 0.1f)
        }

        override fun onBeginningOfSpeech() {
            _state.value = SpeechRecognitionState.Listening(volumeLevel = 0.3f)
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            val currentState = _state.value
            if (currentState is SpeechRecognitionState.Listening) {
                _state.value = currentState.copy(volumeLevel = normalized)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _state.value = SpeechRecognitionState.Processing
        }

        override fun onError(errorCode: Int) {
            // If we already captured partial speech results before error/timeout, use them!
            if (cachedMatches.isNotEmpty()) {
                val primary = cachedMatches[0]
                _state.value = SpeechRecognitionState.Success(
                    spokenText = primary,
                    allCandidates = cachedMatches.toList()
                )
                return
            }

            val (message, isPerm) = mapErrorCodeToMessage(errorCode)
            _state.value = SpeechRecognitionState.Error(message, isPerm)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
            val finalMatches = if (matches.isNotEmpty()) matches else cachedMatches

            if (finalMatches.isNotEmpty()) {
                val primary = finalMatches[0]
                _state.value = SpeechRecognitionState.Success(
                    spokenText = primary,
                    allCandidates = finalMatches.toList()
                )
            } else {
                _state.value = SpeechRecognitionState.Error("Chưa nhận diện được giọng nói. Hãy nói to và gần micro hơn nhé!")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!partial.isNullOrEmpty()) {
                cachedMatches.clear()
                cachedMatches.addAll(partial)
                val text = partial[0]
                val currentState = _state.value
                val vol = if (currentState is SpeechRecognitionState.Listening) currentState.volumeLevel else 0.4f
                _state.value = SpeechRecognitionState.Listening(volumeLevel = vol, partialText = text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _state.value = SpeechRecognitionState.Error(
                        "Thiết bị chưa có dịch vụ Google Speech. Bạn có thể thử nút Nhận diện hệ thống bên dưới."
                    )
                    return@post
                }

                // Synchronously destroy previous instance (DO NOT post to looper)
                destroyInternal()
                cachedMatches.clear()

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer
                recognizer.setRecognitionListener(recognitionListener)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en"))
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
                }

                _state.value = SpeechRecognitionState.Ready
                recognizer.startListening(intent)
            } catch (e: Exception) {
                _state.value = SpeechRecognitionState.Error("Không thể khởi động micro: ${e.localizedMessage ?: "Lỗi không xác định"}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                // If we already have partial matches, immediately succeed!
                if (cachedMatches.isNotEmpty()) {
                    val primary = cachedMatches[0]
                    _state.value = SpeechRecognitionState.Success(
                        spokenText = primary,
                        allCandidates = cachedMatches.toList()
                    )
                    destroyInternal()
                    return@post
                }

                speechRecognizer?.stopListening()
                if (_state.value is SpeechRecognitionState.Listening) {
                    _state.value = SpeechRecognitionState.Processing
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun reset() {
        mainHandler.post {
            destroyInternal()
            cachedMatches.clear()
            _state.value = SpeechRecognitionState.Idle
        }
    }

    fun destroy() {
        mainHandler.post {
            destroyInternal()
            cachedMatches.clear()
        }
    }

    private fun destroyInternal() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore
        }
        speechRecognizer = null
    }

    private fun mapErrorCodeToMessage(error: Int): Pair<String, Boolean> {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Lỗi thu âm từ Micro. Vui lòng kiểm tra lại thiết bị." to false
            SpeechRecognizer.ERROR_CLIENT -> "Chưa nhận được âm thanh. Hãy nhấn Micro và nói lại nhé!" to false
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa cấp quyền Micro để luyện phát âm." to true
            SpeechRecognizer.ERROR_NETWORK -> "Lỗi kết nối mạng nhận diện giọng nói." to false
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Hết thời gian kết nối mạng." to false
            SpeechRecognizer.ERROR_NO_MATCH -> "Chưa nghe rõ từ bạn vừa đọc. Hãy nói to, rõ ràng và gần micro hơn nhé!" to false
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Micro đang bận. Đang thử lại..." to false
            SpeechRecognizer.ERROR_SERVER -> "Lỗi dịch vụ Google Speech. Bạn có thể dùng nút Google Hệ thống bên dưới." to false
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nghe thấy giọng nói. Hãy nhấn micro và đọc nhé!" to false
            else -> "Chưa nhận diện được. Bạn hãy thử phát âm lại nhé!" to false
        }
    }
}
