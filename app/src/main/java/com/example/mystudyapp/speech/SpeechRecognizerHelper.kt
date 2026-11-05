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
import java.util.Locale

sealed interface SpeechRecognitionState {
    object Idle : SpeechRecognitionState
    object Ready : SpeechRecognitionState
    data class Listening(val volumeLevel: Float = 0f) : SpeechRecognitionState // 0f to 1f normalized
    object Processing : SpeechRecognitionState
    data class Success(val spokenText: String, val allCandidates: List<String>) : SpeechRecognitionState
    data class Error(val message: String, val isPermissionDenied: Boolean = false) : SpeechRecognitionState
}

class SpeechRecognizerHelper(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

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
            // rmsdB typically ranges from -2f to 10f
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            if (_state.value is SpeechRecognitionState.Listening) {
                _state.value = SpeechRecognitionState.Listening(volumeLevel = normalized)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _state.value = SpeechRecognitionState.Processing
        }

        override fun onError(errorCode: Int) {
            val (message, isPerm) = mapErrorCodeToMessage(errorCode)
            _state.value = SpeechRecognitionState.Error(message, isPerm)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
            if (matches.isNotEmpty()) {
                val primary = matches[0]
                _state.value = SpeechRecognitionState.Success(spokenText = primary, allCandidates = matches)
            } else {
                _state.value = SpeechRecognitionState.Error("Chưa nhận diện được giọng nói. Hãy thử đọc to hơn nhé!")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank() && _state.value is SpeechRecognitionState.Listening) {
                // Keep listening with active audio feedback
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _state.value = SpeechRecognitionState.Error(
                        "Thiết bị chưa hỗ trợ nhận diện giọng nói Google Speech."
                    )
                    return@post
                }

                destroy() // clean up previous instance

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer
                recognizer.setRecognitionListener(recognitionListener)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.US.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, Locale.US.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
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
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                // Ignore
            }
            _state.value = SpeechRecognitionState.Idle
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
            speechRecognizer = null
        }
    }

    private fun mapErrorCodeToMessage(error: Int): Pair<String, Boolean> {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Lỗi thu âm từ Micro. Vui lòng kiểm tra lại thiết bị." to false
            SpeechRecognizer.ERROR_CLIENT -> "Đã dừng nhận diện giọng nói." to false
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa cấp quyền Micro để luyện phát âm." to true
            SpeechRecognizer.ERROR_NETWORK -> "Lỗi kết nối mạng khi nhận diện giọng nói." to false
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Hết thời gian kết nối mạng." to false
            SpeechRecognizer.ERROR_NO_MATCH -> "Chưa nghe rõ từ bạn vừa đọc. Hãy thử nói to và rõ hơn nhé!" to false
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Micro đang bận. Đang thử lại..." to false
            SpeechRecognizer.ERROR_SERVER -> "Lỗi từ dịch vụ nhận diện giọng nói. Hãy thử lại." to false
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nghe thấy giọng nói. Hãy nhấn micro và đọc nhé!" to false
            else -> "Chưa nhận diện được. Bạn hãy thử phát âm lại nhé!" to false
        }
    }
}
