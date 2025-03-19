package com.practicum.playlistmaker

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("trackName") val trackName: String, // Название композиции
    @SerializedName("artistName") val artistName: String, // Имя исполнителя
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long, // Продолжительность трека
    @SerializedName("artworkUrl100") val artworkUrl100: String,// Ссылка на изображение обложки
    @SerializedName("trackId") val trackId: Int, // уникальный идентификатор трека
    @SerializedName("collectionName") val collectionName: String?, // название альбома
    @SerializedName("releaseDate") val releaseDate: String?, // год релиза трека
    @SerializedName("primaryGenreName") val primaryGenreName: String?, // жанр трека
    @SerializedName("country") val country: String? // страна исполнителя
): Parcelable {
    // Конструктор для Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    // Записываем данные в Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(trackName)
        parcel.writeString(artistName)
        parcel.writeLong(trackTimeMillis)
        parcel.writeString(artworkUrl100)
        parcel.writeInt(trackId)
        parcel.writeString(collectionName)
        parcel.writeString(releaseDate)
        parcel.writeString(primaryGenreName)
        parcel.writeString(country)
    }

    // Описываем содержимое объекта (обычно возвращаем 0)
    override fun describeContents(): Int {
        return 0
    }

    // Компаньон-объект для создания экземпляров Track из Parcel
    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }

    // Метод для изменения URL обложки
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}