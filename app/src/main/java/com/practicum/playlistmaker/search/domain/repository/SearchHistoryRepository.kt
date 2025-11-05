package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.domain.models.Track

interface SearchHistoryRepository {
    fun saveHistory(history: List<Track>) // Сохранение готовой истории
    fun getHistory(): List<Track> // Получение истории
    fun clearHistory() // Удаление истории
}