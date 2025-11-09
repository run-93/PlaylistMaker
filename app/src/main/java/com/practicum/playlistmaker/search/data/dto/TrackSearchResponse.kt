package com.practicum.playlistmaker.search.data.dto

data class TrackSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()