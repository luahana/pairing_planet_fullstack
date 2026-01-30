package com.cookstemma.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "search_history_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
        private const val SEPARATOR = "|||"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _searchHistory = MutableStateFlow<List<String>>(loadHistory())

    val searchHistory: Flow<List<String>> = _searchHistory.asStateFlow()

    private fun loadHistory(): List<String> {
        val stored = prefs.getString(KEY_HISTORY, "") ?: ""
        return if (stored.isEmpty()) {
            emptyList()
        } else {
            stored.split(SEPARATOR).take(MAX_HISTORY_SIZE)
        }
    }

    private fun saveHistory(history: List<String>) {
        prefs.edit()
            .putString(KEY_HISTORY, history.joinToString(SEPARATOR))
            .apply()
        _searchHistory.value = history
    }

    fun addSearch(query: String) {
        if (query.isBlank()) return
        
        val currentHistory = _searchHistory.value.toMutableList()
        
        // Remove existing entry if present
        currentHistory.remove(query)
        
        // Add to the front
        currentHistory.add(0, query)
        
        // Trim to max size
        val trimmed = currentHistory.take(MAX_HISTORY_SIZE)
        
        saveHistory(trimmed)
    }

    fun removeSearch(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query)
        saveHistory(currentHistory)
    }

    fun clearAllSearches() {
        saveHistory(emptyList())
    }
}
