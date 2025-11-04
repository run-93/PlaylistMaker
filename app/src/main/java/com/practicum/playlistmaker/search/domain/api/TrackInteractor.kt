package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.common.ErrorType
import com.practicum.playlistmaker.search.domain.models.Track

interface TrackInteractor {
    fun search(query: String, consumer: TrackConsumer)

    interface TrackConsumer {
        fun consume(foundTrack: List<Track>)
        fun onError(error: ErrorType) // обработка ошибок
    }
}