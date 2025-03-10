package com.practicum.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val searchPref: SharedPreferences) {
    private val SEARCH_KEY = "key_for_search_history"
    // Добавление трека в историю
    fun addTrack(track: Track) {
        val history = getHistory()
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

        // Логирование для отладки
        println("Трек добавлен в историю: ${track.trackName}")
    }

    // Получение истории
    fun getHistory(): ArrayList<Track> {
        val json = searchPref.getString(SEARCH_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            Gson().fromJson(json, type)
        } else {
            arrayListOf()
        }
    }

    // Очистка истории
    fun clearHistory() {
        searchPref.edit().remove(SEARCH_KEY).apply()
        println("История очищена")
    }

    // Сохранение истории
    private fun saveHistory(history: ArrayList<Track>) {
        val json = Gson().toJson(history)
        searchPref.edit().putString(SEARCH_KEY, json).apply()
        println("История сохранена: $json")
    }

}
