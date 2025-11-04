package com.practicum.playlistmaker.search.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.api.TrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TrackInteractorImpl(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : TrackInteractor {

    private val _searchState = MutableLiveData<TrackInteractor.SearchState>()
    override fun getSearchState(): LiveData<TrackInteractor.SearchState> = _searchState

    private val executor = Executors.newScheduledThreadPool(2)
    private var searchFuture: ScheduledFuture<*>? = null
    private val searchDebounceDelay = 2000L

    override fun searchDebounced(query: String) {
        searchFuture?.cancel(false)

        if (query.isEmpty()) {
            showHistory()
            return
        }

        searchFuture = executor.schedule({
            performSearch(query)
        }, searchDebounceDelay, TimeUnit.MILLISECONDS)
    }

    private fun performSearch(query: String) {
        _searchState.postValue(TrackInteractor.SearchState.Loading)

        executor.execute {
            try {
                val tracks = trackRepository.search(query)
                if (tracks.isEmpty()) {
                    _searchState.postValue(TrackInteractor.SearchState.Empty)
                } else {
                    _searchState.postValue(TrackInteractor.SearchState.Content(tracks))
                }
            } catch (e: IOException) {
                _searchState.postValue(TrackInteractor.SearchState.Error(ErrorType.NETWORK_ERROR))
            } catch (e: Exception) {
                _searchState.postValue(TrackInteractor.SearchState.Error(ErrorType.NETWORK_ERROR))
            }
        }
    }

    override fun showHistory() {
        try {
            val history = searchHistoryRepository.getHistory()
            if (history.isEmpty()) {
                _searchState.postValue(TrackInteractor.SearchState.EmptyHistory)
            } else {
                _searchState.postValue(TrackInteractor.SearchState.History(history))
            }
        } catch (e: Exception) {
            _searchState.postValue(TrackInteractor.SearchState.EmptyHistory)
        }
    }

    override fun addTrackToHistory(track: Track) {
        try {
            searchHistoryRepository.addTrack(track)
        } catch (e: Exception) {
            // Ignore errors when adding to history
        }
    }

    override fun clearHistory() {
        try {
            searchHistoryRepository.clearHistory()
            showHistory()
        } catch (e: Exception) {
            // Ignore errors when clearing history
        }
    }

    override fun clearSearch() {
        searchFuture?.cancel(false)
        showHistory()
    }

    fun onCleared() {
        searchFuture?.cancel(true)
        executor.shutdown()
    }
}