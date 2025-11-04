package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.search.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.search.data.network.NetworkClient
import com.practicum.playlistmaker.search.domain.repository.TrackRepository
import com.practicum.playlistmaker.search.domain.models.Track

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