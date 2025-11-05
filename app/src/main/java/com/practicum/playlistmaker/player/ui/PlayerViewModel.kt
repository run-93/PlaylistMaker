package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.search.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel : ViewModel() {

    private val _uiState = MutableLiveData<PlayerUiState>()
    val uiState: LiveData<PlayerUiState> = _uiState

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updatePositionRunnable: Runnable? = null

    companion object {
        private const val UPDATE_INTERVAL = 300L
    }

    fun initialize(track: Track) {
        currentTrack = track

        _uiState.value = PlayerUiState(
            trackInfo = TrackInfoState(
                trackName = track.trackName,
                artistName = track.artistName,
                trackTime = track.trackTime,
                album = track.collectionName ?: "Unknown Album",
                releaseYear = track.releaseDate?.take(4) ?: "Unknown Year",
                genre = track.primaryGenreName ?: "Unknown Genre",
                country = track.country ?: "Unknown Country",
                artworkUrl = track.getCoverArtwork()
            ),
            playbackState = PlaybackState.PREPARING,
            currentPosition = "00:00"
        )

        preparePlayer()
    }

    private fun preparePlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(currentTrack?.previewUrl)
            setOnPreparedListener {
                _uiState.value = _uiState.value?.copy(
                    playbackState = PlaybackState.PREPARED
                )
            }
            setOnCompletionListener {
                _uiState.value = _uiState.value?.copy(
                    playbackState = PlaybackState.PREPARED,
                    currentPosition = "00:00"
                )
                handler.removeCallbacks(updatePositionRunnable ?: return@setOnCompletionListener)
            }
            prepareAsync()
        }
    }

    fun playbackControl() {
        val currentState = _uiState.value?.playbackState
        when (currentState) {
            PlaybackState.PLAYING -> pausePlayer()
            PlaybackState.PREPARED, PlaybackState.PAUSED -> startPlayer()
            else -> {} // Игнорируем другие состояния
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        _uiState.value = _uiState.value?.copy(
            playbackState = PlaybackState.PLAYING
        )
        startPositionUpdates()
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        _uiState.value = _uiState.value?.copy(
            playbackState = PlaybackState.PAUSED
        )
        stopPositionUpdates()
    }

    private fun startPositionUpdates() {
        updatePositionRunnable = object : Runnable {
            override fun run() {
                if (_uiState.value?.playbackState == PlaybackState.PLAYING) {
                    val position = getCurrentPosition()
                    _uiState.value = _uiState.value?.copy(
                        currentPosition = position
                    )
                    handler.postDelayed(this, UPDATE_INTERVAL)
                }
            }
        }
        updatePositionRunnable?.let { handler.post(it) }
    }

    private fun stopPositionUpdates() {
        updatePositionRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun getCurrentPosition(): String {
        return mediaPlayer?.let { player ->
            val currentPosition = player.currentPosition
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
        } ?: "00:00"
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

