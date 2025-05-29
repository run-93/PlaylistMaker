package com.practicum.playlistmaker.data.dto
import com.practicum.playlistmaker.domain.models.Track
import java.util.Locale


data class TrackDto(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,  // Длительность в миллисекундах
    val artworkUrl100: String,
    val collectionName: String?,  // Может быть null
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String?      // Может быть null
) {
    fun toDomain() = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = formatTrackTime(trackTimeMillis),
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )

    private fun formatTrackTime(ms: Long): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

}