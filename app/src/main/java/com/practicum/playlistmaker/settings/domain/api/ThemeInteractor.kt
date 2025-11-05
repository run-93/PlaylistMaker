package com.practicum.playlistmaker.settings.domain.api

interface ThemeInteractor {
    fun getCurrentTheme(): Boolean
    fun switchTheme(darkThemeEnabled: Boolean)
}