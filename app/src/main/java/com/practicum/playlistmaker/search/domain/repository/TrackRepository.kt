package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.domain.models.Track

interface TrackRepository {
    fun search(query: String): List<Track>
}