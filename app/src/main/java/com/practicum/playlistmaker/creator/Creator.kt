package com.practicum.playlistmaker.creator

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.common.App
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.repository.SearchHistoryStorage
import com.practicum.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.practicum.playlistmaker.settings.data.repository.ThemeRepositoryImpl
import com.practicum.playlistmaker.settings.domain.api.ThemeInteractor
import com.practicum.playlistmaker.settings.domain.impl.ThemeInteractorImpl
import com.practicum.playlistmaker.settings.domain.repository.ThemeRepository
import com.practicum.playlistmaker.search.domain.api.TrackInteractor
import com.practicum.playlistmaker.search.domain.impl.TrackInteractorImpl
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository

object Creator {

    private lateinit var applicationContext: Context
    private lateinit var app: App

    fun init(application: Application) {
        applicationContext = application.applicationContext
        app = application as App
    }

    // Search domain
    private fun getTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getTrackInteractor(): TrackInteractor {
        return TrackInteractorImpl(
            getTrackRepository(),
            provideSearchHistoryRepository()
        )
    }

    fun provideTrackInteractor(): TrackInteractor {
        return getTrackInteractor()
    }

    fun provideSearchHistoryRepository(): SearchHistoryRepository {
        val prefs = applicationContext.getSharedPreferences("SEARCH_HISTORY", Context.MODE_PRIVATE)
        return SearchHistoryStorage(prefs, Gson())
    }

    // Theme domain
    private fun getThemeRepository(): ThemeRepository {
        return ThemeRepositoryImpl(applicationContext)
    }

    private fun getThemeInteractor(): ThemeInteractor {
        return ThemeInteractorImpl(getThemeRepository())
    }

    fun provideThemeInteractor(): ThemeInteractor {
        return getThemeInteractor()
    }

    fun provideApp(): App {
        return app
    }
}