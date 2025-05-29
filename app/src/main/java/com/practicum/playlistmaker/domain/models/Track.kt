package com.practicum.playlistmaker.domain.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
    val trackName: String, // Название композиции
    val artistName: String, // Имя исполнителя
    val trackTime: String, // Продолжительность трека
    val artworkUrl100: String,// Ссылка на изображение обложки
    val trackId: Int, // уникальный идентификатор трека
    val collectionName: String?, // название альбома
    val releaseDate: String?, // год релиза трека
    val primaryGenreName: String?, // жанр трека
    val country: String?, // страна исполнителя
    val previewUrl: String? // отрывок текста

): Parcelable {
    fun getCoverArtwork() = artworkUrl100
        .substringBefore('?')
        .replaceAfterLast('/', "512x512bb.jpg")
}


