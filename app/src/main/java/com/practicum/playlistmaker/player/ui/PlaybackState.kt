package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.search.domain.models.Track


data class PlayerUiState(
    val trackInfo: TrackInfoState,
    val playbackState: PlaybackState,
    val currentPosition: String
) {
    val isPlayButtonEnabled: Boolean
        get() = playbackState != PlaybackState.PREPARING

    val isPlaying: Boolean
        get() = playbackState == PlaybackState.PLAYING
}

data class TrackInfoState(
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val album: String,
    val releaseYear: String,
    val genre: String,
    val country: String,
    val artworkUrl: String
)

sealed class PlaybackState {
    object PREPARING : PlaybackState()
    object PREPARED : PlaybackState()
    object PLAYING : PlaybackState()
    object PAUSED : PlaybackState()
}