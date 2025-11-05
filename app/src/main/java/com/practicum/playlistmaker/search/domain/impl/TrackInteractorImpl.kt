package com.practicum.playlistmaker.search.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.api.TrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository
import com.practicum.playlistmaker.search.ui.SearchState
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TrackInteractorImpl(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : TrackInteractor {

    companion object {
        private const val MAX_HISTORY_SIZE = 10
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val _searchState = MutableLiveData<SearchState>()
    override fun getSearchState(): LiveData<SearchState> = _searchState

    private val executor = Executors.newScheduledThreadPool(2)
    private var searchFuture: ScheduledFuture<*>? = null

    override fun searchDebounced(query: String) {
        searchFuture?.cancel(false)

        if (query.isEmpty()) {
            showHistory()
            return
        }

        searchFuture = executor.schedule({
            performSearch(query)
        }, SEARCH_DEBOUNCE_DELAY, TimeUnit.MILLISECONDS)
    }

    private fun performSearch(query: String) {
        _searchState.postValue(SearchState.Loading)

        executor.execute {
            try {
                val tracks = trackRepository.search(query)
                if (tracks.isEmpty()) {
                    _searchState.postValue(SearchState.Error(ErrorType.EMPTY_RESULT))
                } else {
                    _searchState.postValue(SearchState.Content(tracks))
                }
            } catch (e: IOException) {
                _searchState.postValue(SearchState.Error(ErrorType.NETWORK_ERROR))
            } catch (e: Exception) {
                _searchState.postValue(SearchState.Error(ErrorType.NETWORK_ERROR))
            }
        }
    }

    override fun showHistory() {
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

    override fun addTrackToHistory(track: Track) {
        try {
            val currentHistory = searchHistoryRepository.getHistory().toMutableList()

            // Удаляем трек если уже есть в истории (для избежания дубликатов)
            currentHistory.removeAll { it.trackId == track.trackId }

            // Добавляем в начало
            currentHistory.add(0, track)

            // Обрезаем до максимального размера
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
            }

            // Сохраняем обновленную историю
            searchHistoryRepository.saveHistory(currentHistory)
        } catch (e: Exception) {
            // Логируем ошибку, но не прерываем выполнение
            e.printStackTrace()
        }
    }

    override fun clearHistory() {
        try {
            searchHistoryRepository.clearHistory()
            showHistory()
        } catch (e: Exception) {
            // Логируем ошибку очистки
            e.printStackTrace()
        }
    }

    override fun clearSearch() {
        searchFuture?.cancel(false)
        showHistory()
    }

    fun destroy() {
        searchFuture?.cancel(true)
        executor.shutdown()
    }
}