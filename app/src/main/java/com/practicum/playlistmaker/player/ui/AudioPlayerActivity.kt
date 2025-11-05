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

        viewModel.initialize(track)
        observeViewModel()
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

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            // Обновляем всю информацию о треке
            setupTrackInfo(state.trackInfo)

            // Обновляем состояние плеера
            binding.buttonPlay.isEnabled = state.isPlayButtonEnabled
            binding.buttonPlay.setImageResource(
                if (state.isPlaying) R.drawable.pause else R.drawable.button_play
            )
            binding.timePlay.text = state.currentPosition

            // Дополнительные действия по состояниям
            when (state.playbackState) {
                PlaybackState.PREPARING -> {

                }
                PlaybackState.PREPARED -> {
                    // Плеер готов к воспроизведению
                }
                PlaybackState.PLAYING -> {
                    // Идет воспроизведение
                }
                PlaybackState.PAUSED -> {
                    // пауза
                }
            }
        }
    }

    private fun setupTrackInfo(trackInfo: TrackInfoState) {
        binding.trackName.text = trackInfo.trackName
        binding.artistName.text = trackInfo.artistName
        binding.durationValue.text = trackInfo.trackTime
        binding.albumValue.text = trackInfo.album
        binding.releaseValue.text = trackInfo.releaseYear
        binding.genreValue.text = trackInfo.genre
        binding.countryValue.text = trackInfo.country

        loadTrackCover(trackInfo.artworkUrl)
    }

    private fun loadTrackCover(artworkUrl: String) {
        if (artworkUrl.isBlank()) {
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

    override fun onPause() {
        super.onPause()
        val currentState = viewModel.uiState.value?.playbackState
        if (currentState == PlaybackState.PLAYING) {
            viewModel.playbackControl()
        }
    }
}