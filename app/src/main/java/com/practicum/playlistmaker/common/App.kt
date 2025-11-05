package com.practicum.playlistmaker.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.settings.domain.api.ThemeInteractor

class App : Application() {

    private lateinit var themeInteractor: ThemeInteractor

    override fun onCreate() {
        super.onCreate()

        // Инициализируем Creator
        Creator.init(this)

        // Получаем интерактор темы
        themeInteractor = Creator.provideThemeInteractor()

        // Применяем сохраненную тему
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val isDarkTheme = themeInteractor.getCurrentTheme()
        switchTheme(isDarkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Сохраняем настройки через интерактор
        themeInteractor.switchTheme(darkThemeEnabled)

        // Применяем тему
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    val darkTheme: Boolean
        get() = themeInteractor.getCurrentTheme()
}