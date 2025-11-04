package com.practicum.playlistmaker.player.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.databinding.AudioPlayerBinding
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.SearchActivity

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        const val KEY_EXTRA_TRACK = "track_extra"
    }

    private lateinit var binding: AudioPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupClickListeners()

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(KEY_EXTRA_TRACK) as? Track
        } ?: run {
            finish()
            return
        }

        setupTrackInfo(track)
        viewModel.initialize(track)
        observeViewModel()
    }

    private fun initViews() {
        // Все View уже доступны через binding, функция оставлена для совместимости
    }

    private fun setupClickListeners() {
        binding.buttonBack.setOnClickListener {
            val backIntent = Intent(this, SearchActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backIntent)
        }

        binding.buttonPlay.setOnClickListener {
            viewModel.playbackControl()
        }
    }

    private fun setupTrackInfo(track: Track) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.durationValue.text = track.trackTime
        binding.albumValue.text = track.collectionName ?: "Unknown Album"
        binding.releaseValue.text = track.releaseDate?.take(4) ?: "Unknown Year"
        binding.genreValue.text = track.primaryGenreName ?: "Unknown Genre"
        binding.countryValue.text = track.country ?: "Unknown Country"

        loadTrackCover(track)
    }

    private fun loadTrackCover(track: Track) {
        val artworkUrl = track.getCoverArtwork().takeIf { it.isNotBlank() } ?: run {
            binding.imageCover.setImageResource(R.drawable.placeholder)
            return
        }

        val requestOption = RequestOptions()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(RoundedCorners(8))

        Glide.with(this)
            .load(artworkUrl)
            .apply(requestOption)
            .into(binding.imageCover)
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(this) { state ->
            // Обновляем UI на основе состояния плеера
            binding.buttonPlay.isEnabled = state.isPlayButtonEnabled

            // Устанавливаем правильную иконку в зависимости от состояния воспроизведения
            val playButtonResId = if (state.isPlaying) {
                R.drawable.pause
            } else {
                R.drawable.button_play
            }
            binding.buttonPlay.setImageResource(playButtonResId)
        }

        viewModel.currentPosition.observe(this) { position ->
            binding.timePlay.text = position
        }
    }

    override fun onPause() {
        super.onPause()
        // ViewModel сама управляет состоянием плеера
        viewModel.playbackControl() // Пауза при сворачивании приложения
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewModel сама освобождает ресурсы в onCleared()
    }
}