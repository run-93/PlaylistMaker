package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.common.App
import com.practicum.playlistmaker.settings.domain.api.ThemeInteractor

class SettingsViewModel(
    private val themeInteractor: ThemeInteractor,
    private val app: App
) : ViewModel() {

    private val _screenState = MutableLiveData<ScreenState>()
    val screenState: LiveData<ScreenState> = _screenState

    init {
        loadCurrentTheme()
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        themeInteractor.switchTheme(darkThemeEnabled)
        app.switchTheme(darkThemeEnabled)
        _screenState.value = ScreenState.ThemeChanged(darkThemeEnabled)
    }

    fun onBackClicked() {
        _screenState.value = ScreenState.NavigateBack
    }

    fun onShareClicked() {
        _screenState.value = ScreenState.NavigateShare
    }

    fun onSupportClicked() {
        _screenState.value = ScreenState.NavigateSupport
    }

    fun onUserAgreementClicked() {
        _screenState.value = ScreenState.NavigateUserAgreement
    }

    private fun loadCurrentTheme() {
        _screenState.value = ScreenState.ThemeChanged(themeInteractor.getCurrentTheme())
    }

    sealed class ScreenState {
        data class ThemeChanged(val isDarkTheme: Boolean) : ScreenState()
        object NavigateBack : ScreenState()
        object NavigateShare : ScreenState()
        object NavigateSupport : ScreenState()
        object NavigateUserAgreement : ScreenState()
    }

    companion object {
        fun provideFactory(themeInteractor: ThemeInteractor, app: App): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(themeInteractor, app) as T
                }
            }
        }
    }
}