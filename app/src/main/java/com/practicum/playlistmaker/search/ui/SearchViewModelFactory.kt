package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TrackRepository

class SearchViewModelFactory(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(trackRepository, searchHistoryRepository) as T
    }
}