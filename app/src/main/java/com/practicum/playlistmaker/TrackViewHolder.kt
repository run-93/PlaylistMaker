package com.practicum.playlistmaker

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class TrackViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    private val vhTrackName: MaterialTextView = itemView.findViewById(R.id.track_Name)
    private val vhArtistName: MaterialTextView = itemView.findViewById(R.id.artist_Name)
    private val vhTrackTime: MaterialTextView = itemView.findViewById(R.id.track_Time)
    private val vhImageCover: ImageView = itemView.findViewById(R.id.image_Cover)
    /// определяем переменные и привязываем их к view

    fun bind(item: Track) {
        vhTrackName.text = item.trackName
        vhArtistName.text = item.artistName
        vhTrackTime.text = item.trackTime


    // связываем переменные с значениями из полей view

    }


        /// переопределяем значения
}
