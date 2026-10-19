package com.example.mystudyapp.data.local

import android.content.Context
import android.content.SharedPreferences

class ChatbotLocalStore(
    context: Context? = null,
    private val defaultApiKey: String? = DEFAULT_API_KEY
) {
    companion object {
        const val DEFAULT_API_KEY = "AIzaSyD91I7kDPOwiITcgbq5k5RUCkBpU3riWYk"
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
            if (stored.isBlank()) return null
            if (!stored.startsWith("AQ.")) return stored
        }
        if (memoryApiKey != null) {
            if (memoryApiKey!!.isBlank()) return null
            if (!memoryApiKey!!.startsWith("AQ.")) return memoryApiKey
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
