package com.practicum.playlistmaker.settings.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.settings.domain.models.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.ThemeRepository

class ThemeRepositoryImpl(
    private val context: Context
) : ThemeRepository {

    companion object {
        private const val THEME_PREFERENCES = "theme_preferences"
        private const val SWITCH_KEY = "key_for_theme"
    }

    private val themePrefs: SharedPreferences by lazy {
        context.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
    }

    override fun getThemeSettings(): ThemeSettings {
        val isDarkTheme = themePrefs.getBoolean(SWITCH_KEY, false)
        return ThemeSettings(isDarkTheme = isDarkTheme)
    }

    override fun saveThemeSettings(themeSettings: ThemeSettings) {
        themePrefs.edit()
            .putBoolean(SWITCH_KEY, themeSettings.isDarkTheme)
            .apply()
    }
}