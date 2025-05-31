package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryStorage (private val searchPreferences: SharedPreferences,
                            private val gson: Gson
    ) : SearchHistoryRepository {

        companion object {
            private const val SEARCH_KEY = "key_for_search_history"
            private const val MAX_HISTORY_SIZE = 10
        }

    override fun addTrack(track: Track) {
        val history = getHistory()
        Log.d("history", "$history")
        val existingIndex = history.indexOfFirst { it.trackId == track.trackId }

        if (existingIndex != -1) {
            history.removeAt(existingIndex) // Удаляем старую запись, если трек уже есть в истории
        }

        history.add(0, track) // Добавляем новый трек в начало списка


        // Удаляем лишние треки, если их больше 10
        if (history.size > 10) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    // Получение истории
    override fun getHistory(): ArrayList<Track> {
        val json = searchPreferences.getString(SEARCH_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            Gson().fromJson(json, type)
        } else {
            arrayListOf()
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