package com.practicum.playlistmaker.data

import com.practicum.playlistmaker.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.domain.api.TrackRepository
import com.practicum.playlistmaker.domain.models.Track

class TrackRepositoryImpl (private val networkClient: NetworkClient) : TrackRepository {

    override fun search(query: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(query))
        return if (response.resultCode == 200 && response is TrackSearchResponse) {
            response.results.map { it.toDomain() }
        } else {
             emptyList()
        }
    }
}