package com.practicum.playlistmaker.settings.domain.repository

import com.practicum.playlistmaker.settings.domain.models.ThemeSettings

interface ThemeRepository {
    fun getThemeSettings(): ThemeSettings
    fun saveThemeSettings(themeSettings: ThemeSettings)
}