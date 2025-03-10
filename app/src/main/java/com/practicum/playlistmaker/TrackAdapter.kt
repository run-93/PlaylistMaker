package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<TrackViewHolder> () {

    var track = ArrayList<Track>()

    // Интерфейс для обработки кликов
    interface OnItemClickListener {
        fun onItemClick(track: Track)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(track.get(position))

        // Устанавливаем обработчик клика на элемент
        holder.itemView.setOnClickListener {
            listener.onItemClick(track.get(position))
        }
    }

    override fun getItemCount(): Int {
        return track.size
    }
}