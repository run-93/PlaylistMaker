package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun addTrack(track: Track) // Добавление трека в историю
    fun getHistory(): List<Track> // Получение истории
    fun clearHistory() // Удаление истории
}