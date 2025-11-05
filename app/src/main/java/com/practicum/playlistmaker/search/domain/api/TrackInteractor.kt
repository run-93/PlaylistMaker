package com.practicum.playlistmaker.search.domain.api

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.SearchState

interface TrackInteractor {
    fun searchDebounced(query: String)
    fun showHistory()
    fun addTrackToHistory(track: Track)
    fun clearHistory()
    fun clearSearch()

    fun getSearchState(): LiveData<SearchState>
}