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
                is SearchState.Loading -> SearchState.Loading
                is SearchState.EmptyHistory -> SearchState.EmptyHistory
                is SearchState.Content -> SearchState.Content(domainState.tracks)
                is SearchState.History -> SearchState.History(domainState.tracks)
                is SearchState.Error -> SearchState.Error(domainState.errorType)
                else -> {
                    // Обработка непредусмотренных состояний (например, логирование)
                    SearchState.EmptyHistory // или другое состояние по умолчанию
                }
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
        (trackInteractor as? com.practicum.playlistmaker.search.domain.impl.TrackInteractorImpl)?.destroy()
    }
}