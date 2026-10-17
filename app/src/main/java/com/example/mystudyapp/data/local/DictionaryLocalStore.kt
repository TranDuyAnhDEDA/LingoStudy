package com.example.mystudyapp.data.local

import android.content.Context
import android.content.SharedPreferences

class DictionaryLocalStore(context: Context? = null) {
    private val prefs: SharedPreferences? = try {
        context?.applicationContext?.getSharedPreferences("lingo_dictionary_prefs", Context.MODE_PRIVATE)
            ?: context?.getSharedPreferences("lingo_dictionary_prefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    // In-memory fallbacks for unit testing or when Context is absent
    private val memoryHistory = mutableListOf<String>()
    private val memoryFavorites = mutableSetOf<String>()

    fun getSearchHistory(): List<String> {
        val raw = prefs?.getString("search_history", null)
        return if (raw != null) {
            raw.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
        } else {
            memoryHistory.toList()
        }
    }

    fun addSearchHistory(word: String) {
        val trimmed = word.trim().lowercase()
        if (trimmed.isBlank()) return
        val current = getSearchHistory().toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val trimmedList = current.take(20)

        prefs?.edit()?.putString("search_history", trimmedList.joinToString(","))?.apply()
        memoryHistory.clear()
        memoryHistory.addAll(trimmedList)
    }

    fun removeSearchHistory(word: String) {
        val trimmed = word.trim().lowercase()
        val current = getSearchHistory().toMutableList()
        current.remove(trimmed)

        prefs?.edit()?.putString("search_history", current.joinToString(","))?.apply()
        memoryHistory.clear()
        memoryHistory.addAll(current)
    }

    fun clearSearchHistory() {
        prefs?.edit()?.remove("search_history")?.apply()
        memoryHistory.clear()
    }

    fun getFavoriteWords(): Set<String> {
        val set = prefs?.getStringSet("favorite_words", null)
        return set ?: memoryFavorites.toSet()
    }

    fun isFavorite(word: String): Boolean {
        val trimmed = word.trim().lowercase()
        return getFavoriteWords().contains(trimmed)
    }

    fun toggleFavorite(word: String): Boolean {
        val trimmed = word.trim().lowercase()
        if (trimmed.isBlank()) return false
        val current = getFavoriteWords().toMutableSet()
        val isNowFav = if (current.contains(trimmed)) {
            current.remove(trimmed)
            false
        } else {
            current.add(trimmed)
            true
        }
        prefs?.edit()?.putStringSet("favorite_words", current)?.apply()
        memoryFavorites.clear()
        memoryFavorites.addAll(current)
        return isNowFav
    }
}
