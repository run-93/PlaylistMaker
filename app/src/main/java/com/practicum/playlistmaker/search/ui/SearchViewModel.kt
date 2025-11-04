package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.api.TrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track

class SearchViewModel(
    private val trackInteractor: TrackInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    init {
        trackInteractor.getSearchState().observeForever { domainState ->
            val uiState = when (domainState) {
                is TrackInteractor.SearchState.Loading -> SearchState.Loading
                is TrackInteractor.SearchState.Empty -> SearchState.Empty
                is TrackInteractor.SearchState.EmptyHistory -> SearchState.EmptyHistory
                is TrackInteractor.SearchState.Content -> SearchState.Content(domainState.tracks)
                is TrackInteractor.SearchState.History -> SearchState.History(domainState.tracks)
                is TrackInteractor.SearchState.Error -> SearchState.Error(
                    when (domainState.errorType) {
                        ErrorType.NETWORK_ERROR -> ErrorType.NETWORK_ERROR
                        ErrorType.EMPTY_RESULT -> ErrorType.EMPTY_RESULT
                    }
                )
            }
            _searchState.postValue(uiState)
        }
    }

    fun searchDebounced(query: String) {
        trackInteractor.searchDebounced(query)
    }

    fun showHistory() {
        trackInteractor.showHistory()
    }

    fun addTrackToHistory(track: Track) {
        trackInteractor.addTrackToHistory(track)
    }

    fun clearHistory() {
        trackInteractor.clearHistory()
    }

    fun clearSearch() {
        trackInteractor.clearSearch()
    }

    override fun onCleared() {
        super.onCleared()
        (trackInteractor as? com.practicum.playlistmaker.search.domain.impl.TrackInteractorImpl)?.onCleared()
    }
}