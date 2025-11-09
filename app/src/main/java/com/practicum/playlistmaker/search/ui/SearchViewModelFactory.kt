package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.search.domain.api.TrackInteractor

class SearchViewModelFactory(
    private val trackInteractor: TrackInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(trackInteractor) as T
    }
}