package com.practicum.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val searchPreferences: SharedPreferences) {
    val SEARCH_KEY = "key_for_search_history"
    // Добавление трека в историю
    fun addTrack(track: Track) {
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
    fun getHistory(): ArrayList<Track> {
        val json = searchPreferences.getString(SEARCH_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            Gson().fromJson(json, type)
        } else {
            arrayListOf()
        }
    }

    // Очистка истории
    fun clearHistory() {
        searchPreferences.edit().remove(SEARCH_KEY).apply()

    }

    // Сохранение истории
    private fun saveHistory(history: ArrayList<Track>) {
        val json = Gson().toJson(history)
        searchPreferences.edit().putString(SEARCH_KEY, json).apply()

    }


}
