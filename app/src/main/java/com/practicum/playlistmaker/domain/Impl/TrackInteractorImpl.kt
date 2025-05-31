package com.practicum.playlistmaker.domain.Impl

import com.practicum.playlistmaker.domain.api.TrackInteractor
import com.practicum.playlistmaker.domain.api.TrackRepository
import java.util.concurrent.Executors

class TrackInteractorImpl(private val repository: TrackRepository) : TrackInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun search(query: String, consumer: TrackInteractor.TrackConsumer) {
        executor.execute {
            consumer.consume(repository.search(query))
        }
    }
}