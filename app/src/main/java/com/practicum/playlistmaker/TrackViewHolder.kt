package com.practicum.playlistmaker

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView

class TrackViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    /// определяем переменные и привязываем их к view
    private val vhTrackName: MaterialTextView = itemView.findViewById(R.id.track_Name)
    private val vhArtistName: MaterialTextView = itemView.findViewById(R.id.artist_Name)
    private val vhTrackTime: MaterialTextView = itemView.findViewById(R.id.track_Time)
    private val vhImageCover: ImageView = itemView.findViewById(R.id.image_Cover)


    fun bind(item: Track) {
        // связываем переменные с значениями из полей view
        vhTrackName.text = item.trackName
        vhArtistName.text = item.artistName
        vhTrackTime.text = item.trackTime

        val requestOption = RequestOptions()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)



        Glide.with(itemView.context)
            .load(item.artworkUrl100)
            .apply(requestOption)
            .apply(RequestOptions().transform(RoundedCorners(2)))
            .into(vhImageCover)

    }

}
