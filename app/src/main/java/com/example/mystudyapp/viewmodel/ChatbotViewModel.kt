package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.ChatMessage
import com.example.mystudyapp.data.model.MessageSender
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.ChatbotRepository
import com.example.mystudyapp.data.repository.ChatbotRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val apiKey: String? = null,
    val showApiKeyDialog: Boolean = false,
    val quickSuggestions: List<String> = listOf(
        "🔍 Tra từ vựng",
        "📖 Giải thích ngữ pháp",
        "🎧 Luyện nghe",
        "💡 Hôm nay nên học gì?"
    )
)

class ChatbotViewModel(
    private val chatbotRepository: ChatbotRepository = ChatbotRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    init {
        initializeChat()
    }

    fun initializeChat() {
        val user = authRepository.getCurrentUser()
        val apiKey = chatbotRepository.getApiKey()
        val greeting = chatbotRepository.getInitialGreeting(user?.displayName)

        _uiState.update { current ->
            if (current.messages.isEmpty()) {
                current.copy(
                    apiKey = apiKey,
                    messages = listOf(
                        ChatMessage(
                            text = greeting,
                            sender = MessageSender.PARROT
                        )
                    )
                )
            } else {
                current.copy(apiKey = apiKey)
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        val user = authRepository.getCurrentUser()
        val uid = user?.uid.orEmpty()

        val userMessage = ChatMessage(
            text = trimmed,
            sender = MessageSender.USER
        )

        val loadingMessage = ChatMessage(
            id = "loading_${UUID.randomUUID()}",
            text = "Vẹt đang suy nghĩ...",
            sender = MessageSender.PARROT,
            isLoading = true
        )

        val priorHistory = _uiState.value.messages.filter { !it.isLoading }

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMessage + loadingMessage,
                isGenerating = true
            )
        }

        viewModelScope.launch {
            val responseResult = chatbotRepository.askParrot(
                history = priorHistory,
                userMessage = trimmed,
                uid = uid
            )

            val parrotReply = responseResult.getOrElse {
                "🦜 Vẹt tạm thời gặp chút gián đoạn kết nối rồi. Bạn thử hỏi lại Vẹt nhé!"
            }

            val finalReplyMessage = ChatMessage(
                text = parrotReply,
                sender = MessageSender.PARROT
            )

            _uiState.update { current ->
                val filtered = current.messages.filter { !it.isLoading }
                current.copy(
                    messages = filtered + finalReplyMessage,
                    isGenerating = false
                )
            }
        }
    }

    fun sendQuickPrompt(prompt: String) {
        sendMessage(prompt)
    }

    fun saveApiKey(newKey: String) {
        val clean = newKey.trim()
        chatbotRepository.saveApiKey(clean)
        _uiState.update {
            it.copy(
                apiKey = clean.ifBlank { null },
                showApiKeyDialog = false
            )
        }
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun clearChat() {
        val user = authRepository.getCurrentUser()
        val greeting = chatbotRepository.getInitialGreeting(user?.displayName)
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        text = greeting,
                        sender = MessageSender.PARROT
                    )
                ),
                isGenerating = false
            )
        }
    }
}

class ChatbotViewModelFactory(
    private val chatbotRepository: ChatbotRepository = ChatbotRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatbotViewModel(chatbotRepository, authRepository) as T
    }
}
