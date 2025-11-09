package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.domain.api.ThemeInteractor
import com.practicum.playlistmaker.settings.domain.repository.ThemeRepository

class ThemeInteractorImpl(
    private val themeRepository: ThemeRepository
) : ThemeInteractor {

    override fun getCurrentTheme(): Boolean {
        return themeRepository.getThemeSettings().isDarkTheme
    }

    override fun switchTheme(darkThemeEnabled: Boolean) {
        themeRepository.saveThemeSettings(
            com.practicum.playlistmaker.settings.domain.models.ThemeSettings(
                isDarkTheme = darkThemeEnabled
            )
        )
    }
}