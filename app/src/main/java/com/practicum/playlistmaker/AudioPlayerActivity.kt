package com.practicum.playlistmaker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val KEY_EXTRA_TRACK = "track"

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var buttonBack: ImageButton
    private lateinit var imageCover: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var addToMyTracklist: ImageButton
    private lateinit var buttonPlay: ImageButton
    private lateinit var battonLike: ImageButton
    private lateinit var timePlay: TextView
    private lateinit var durationValue: TextView
    private lateinit var albumValue: TextView
    private lateinit var releaseValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var countryValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_player)
//инициализация объектов на view
        buttonBack = findViewById<ImageButton>(R.id.button_back)
        imageCover = findViewById<ImageView>(R.id.image_Cover)
        trackName = findViewById<TextView>(R.id.track_Name)
        artistName = findViewById<TextView>(R.id.artist_Name)
        addToMyTracklist = findViewById<ImageButton>(R.id.add_to_my_tracklist)
        buttonPlay = findViewById<ImageButton>(R.id.button_play)
        battonLike = findViewById<ImageButton>(R.id.batton_like)
        timePlay = findViewById<TextView>(R.id.time_play)
        durationValue = findViewById<TextView>(R.id.duration_value)
        albumValue = findViewById<TextView>(R.id.album_value)
        releaseValue = findViewById<TextView>(R.id.release_value)
        genreValue = findViewById<TextView>(R.id.genre_value)
        countryValue = findViewById<TextView>(R.id.country_value)

// Получаем данные трека из Intent и используем метод в зависимости от версии SDK устроства
        val track = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                intent.getParcelableExtra(KEY_EXTRA_TRACK, Track::class.java)
            } else -> {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Track>(KEY_EXTRA_TRACK)
            }
        }
        if (track != null) {
            // Отображаем данные трека
            trackName.text = track.trackName
            artistName.text = track.artistName
            durationValue.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(track.trackTimeMillis))
            albumValue.text = track.collectionName ?: "Unknown Album"
            releaseValue.text = track.releaseDate?.take(4) ?: "Unknown Year" // форматируем строчку releaseDate
            genreValue.text = track.primaryGenreName ?: "Unknown Genre"
            countryValue.text = track.country ?: "Unknown Country"


// настраиваем параметры запроса Glide
            val requestOption = RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(RoundedCorners(8)) // Скругление углов
// Изменяем URL с помощью функции getCoverArtwork()
            val artworkUrl = track.getCoverArtwork()


            Glide.with(this)
                .load(artworkUrl) // используем измененную с помощью функции ссылку
                .apply(requestOption)
                .into(imageCover)
        }


        // переход из активити поиска на главную актививти
        buttonBack.setOnClickListener{
            val backIntent = Intent(this, SearchActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backIntent)

        }

    }
}