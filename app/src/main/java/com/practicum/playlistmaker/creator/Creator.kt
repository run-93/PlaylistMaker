package com.practicum.playlistmaker.creator

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.repository.SearchHistoryStorage
import com.practicum.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.TrackInteractor
import com.practicum.playlistmaker.search.domain.impl.TrackInteractorImpl
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository

object Creator {

    private fun getTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getTrackInteractor(context: Context): TrackInteractor {
        return TrackInteractorImpl(
            getTrackRepository(),
            provideSearchHistoryRepository(context)
        )
    }

    fun provideTrackInteractor(context: Context): TrackInteractor {
        return getTrackInteractor(context)
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val prefs = context.getSharedPreferences("SEARCH_HISTORY", Context.MODE_PRIVATE)
        return SearchHistoryStorage(prefs, Gson())
    }
}