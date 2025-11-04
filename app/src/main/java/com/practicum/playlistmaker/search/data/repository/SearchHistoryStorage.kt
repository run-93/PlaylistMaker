package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import android.util.Log
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
        private const val MAX_HISTORY_SIZE = 10
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        Log.d("history", "$history")
        val existingIndex = history.indexOfFirst { it.trackId == track.trackId }

        if (existingIndex != -1) {
            history.removeAt(existingIndex)
        }

        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
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

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        searchPreferences.edit().putString(SEARCH_KEY, json).apply()
    }
}