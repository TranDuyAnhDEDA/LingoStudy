package com.example.mystudyapp.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    PARROT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val quickReplies: List<String> = emptyList()
)

fun cleanChatText(rawText: String): String {
    if (rawText.isBlank()) return rawText
    var text = rawText

    // Remove markdown headers like ### or ## at the start of a line
    text = text.replace(Regex("(?m)^#{1,6}\\s*"), "")

    // Remove markdown dividers like --- or ___
    text = text.replace(Regex("(?m)^[-*_]{3,}\\s*$"), "")

    // Replace markdown bullets (* item or + item) at start of lines with a clean bullet •
    text = text.replace(Regex("(?m)^\\s*[*+]\\s+"), "• ")

    // Remove bold/italic stars: **bold** or *italic*
    text = text.replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
    text = text.replace(Regex("\\*([^*]+)\\*"), "$1")
    text = text.replace(Regex("_{2}([^_]+)_{2}"), "$1")

    // Clean up any remaining standalone asterisks
    text = text.replace("*", "")

    // Remove markdown backticks
    text = text.replace("`", "")

    // Replace slashes around pronunciations like /bʊk/ with [bʊk]
    text = text.replace(Regex("/([^/\\n\\r]+)/"), "[$1]")

    // Clean standalone slashes used as separators: " / " -> " - "
    text = text.replace(" / ", " - ")

    // Clean up excessive blank lines
    text = text.replace(Regex("\\n{3,}"), "\n\n")

    return text.trim()
}
