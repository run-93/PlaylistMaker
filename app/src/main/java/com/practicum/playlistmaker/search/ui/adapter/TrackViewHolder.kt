package com.practicum.playlistmaker.search.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val trackName: TextView = itemView.findViewById(R.id.track_Name)
    private val artistName: TextView = itemView.findViewById(R.id.artist_Name)
    private val trackTime: TextView = itemView.findViewById(R.id.track_Time)
    private val imageCover: ImageView = itemView.findViewById(R.id.image_Cover)

    fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTime.text = track.trackTime

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(RoundedCorners(2))

        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .apply(requestOptions)
            .into(imageCover)
    }
}
