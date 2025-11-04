package com.practicum.playlistmaker.search.domain.api

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.models.Track

interface TrackInteractor {
    fun searchDebounced(query: String)
    fun showHistory()
    fun addTrackToHistory(track: Track)
    fun clearHistory()
    fun clearSearch()


    fun getSearchState(): LiveData<SearchState>

    sealed class SearchState {
        object Loading : SearchState()
        object Empty : SearchState()
        object EmptyHistory : SearchState()
        data class Content(val tracks: List<Track>) : SearchState()
        data class History(val tracks: List<Track>) : SearchState()
        data class Error(val errorType: ErrorType) : SearchState()
    }
}