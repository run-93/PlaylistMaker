package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository

class SearchHistoryStorage(
    private val searchPreferences: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    companion object {
        private const val SEARCH_KEY = "key_for_search_history"
    }

    override fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        searchPreferences.edit().putString(SEARCH_KEY, json).apply()
    }

    override fun getHistory(): List<Track> {
        val json = searchPreferences.getString(SEARCH_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun clearHistory() {
        searchPreferences.edit().remove(SEARCH_KEY).apply()
    }
}