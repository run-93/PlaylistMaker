package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TrackRepository {
    fun search(query: String): List<Track>
}