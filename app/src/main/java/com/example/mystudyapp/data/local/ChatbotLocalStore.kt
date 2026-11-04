package com.example.mystudyapp.data.local

import android.content.Context
import android.content.SharedPreferences

class ChatbotLocalStore(
    context: Context? = null,
    private val defaultApiKey: String? = DEFAULT_API_KEY
) {
    companion object {
        const val DEFAULT_API_KEY = "AQ.Ab8RN6JmkwlyvkkY-ja9TkTHeS0wP1gFmCZGN-Jq-UGrMeLbKQ"
    }

    private val prefs: SharedPreferences? = try {
        context?.applicationContext?.getSharedPreferences("lingo_chatbot_prefs", Context.MODE_PRIVATE)
            ?: context?.getSharedPreferences("lingo_chatbot_prefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    private var memoryApiKey: String? = null

    fun getApiKey(): String? {
        val stored = prefs?.getString("gemini_api_key", null)?.trim()
        if (stored != null) {
            return if (stored.isBlank()) null else stored
        }
        if (memoryApiKey != null) {
            return if (memoryApiKey!!.isBlank()) null else memoryApiKey
        }
        return defaultApiKey
    }

    fun saveApiKey(apiKey: String) {
        val clean = apiKey.trim()
        prefs?.edit()?.putString("gemini_api_key", clean)?.apply()
        memoryApiKey = clean
    }

    fun clearApiKey() {
        prefs?.edit()?.putString("gemini_api_key", "")?.apply()
        memoryApiKey = ""
    }
}
