package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class SearchViewModel(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val executor = Executors.newScheduledThreadPool(2)
    private var searchFuture: ScheduledFuture<*>? = null
    private val searchDebounceDelay = 2000L

    fun searchDebounced(query: String) {
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
        _searchState.postValue(SearchState.Loading)

        executor.execute {
            try {
                val tracks = trackRepository.search(query)

                // Если список пустой, но исключения не было - значит действительно ничего не найдено
                if (tracks.isEmpty()) {
                    _searchState.postValue(SearchState.Empty)
                } else {
                    _searchState.postValue(SearchState.Content(tracks))
                }

            } catch (e: IOException) {
                // Сетевая ошибка - показываем сообщение об ошибке сети
                _searchState.postValue(SearchState.Error(ErrorType.NETWORK_ERROR))
            } catch (e: Exception) {
                // Другие ошибки
                _searchState.postValue(SearchState.Error(ErrorType.NETWORK_ERROR))
            }
        }
    }

    fun showHistory() {
        try {
            val history = searchHistoryRepository.getHistory()
            if (history.isEmpty()) {
                _searchState.postValue(SearchState.EmptyHistory)
            } else {
                _searchState.postValue(SearchState.History(history))
            }
        } catch (e: Exception) {
            _searchState.postValue(SearchState.EmptyHistory)
        }
    }

    fun addTrackToHistory(track: Track) {
        try {
            searchHistoryRepository.addTrack(track)
        } catch (e: Exception) {
            // Игнорируем ошибки при добавлении в историю
        }
    }

    fun clearHistory() {
        try {
            searchHistoryRepository.clearHistory()
            showHistory()
        } catch (e: Exception) {
            // Игнорируем ошибки при очистке истории
        }
    }

    fun clearSearch() {
        searchFuture?.cancel(false)
        showHistory()
    }

    override fun onCleared() {
        super.onCleared()
        searchFuture?.cancel(true)
        executor.shutdown()
    }
}