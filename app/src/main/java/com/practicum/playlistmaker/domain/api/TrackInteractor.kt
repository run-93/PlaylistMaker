package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.ErrorType
import com.practicum.playlistmaker.domain.models.Track

interface TrackInteractor {
    fun search(query: String, consumer: TrackConsumer)

    interface TrackConsumer {
        fun consume(foundTrack: List<Track>)
        fun onError(error: ErrorType) // обработка ошибок
    }
}