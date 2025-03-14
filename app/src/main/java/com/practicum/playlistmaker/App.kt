package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

// создаем в приложении переменную в которой будет храниться значение из файла Shared Preferences и ключ для доступа к значению
const val THEME_PREFERENCES = "theme_preferences"
const val SWITCH_KEY = "key_for_theme"

class App : Application(){
    var darkTheme = false
    override fun onCreate() {
        super.onCreate()
        // получаем сохраненные настройки из Shared Preferences файла theme_preferences
        val themePrefs = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE)

        // берем значение переменной из файла theme_preferences, если он пустой, то по умолчанию берем false
        darkTheme = themePrefs.getBoolean(SWITCH_KEY, false)
        // применяем текущую тему
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        //Устанвливаем тему в приложении
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        //Сохраняем текущие настройки в файле theme_preferences
        val themePrefs = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE)
        themePrefs.edit()
            .putBoolean(SWITCH_KEY, darkTheme)
            .apply()
    }
}