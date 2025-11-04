package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.models.Track

sealed class SearchState {
    object Loading : SearchState()
    object Empty : SearchState()
    object EmptyHistory : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    data class Error(val errorType: ErrorType) : SearchState()
}