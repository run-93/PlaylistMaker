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

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<String>()
    val currentPosition: LiveData<String> = _currentPosition

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updatePositionRunnable: Runnable? = null

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_INTERVAL = 300L
    }

    private var playerStateInternal = STATE_DEFAULT

    fun initialize(track: Track) {
        currentTrack = track
        _playerState.value = PlayerState(
            track = track,
            isPlayButtonEnabled = false,
            isPlaying = false
        )
        _currentPosition.value = "00:00"
        preparePlayer()
    }

    private fun preparePlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(currentTrack?.previewUrl)
            setOnPreparedListener {
                playerStateInternal = STATE_PREPARED
                _playerState.value = _playerState.value?.copy(
                    isPlayButtonEnabled = true
                )
            }
            setOnCompletionListener {
                playerStateInternal = STATE_PREPARED
                _playerState.value = _playerState.value?.copy(
                    isPlaying = false
                )
                _currentPosition.value = "00:00"
                handler.removeCallbacks(updatePositionRunnable ?: return@setOnCompletionListener)
            }
            prepareAsync()
        }
    }

    fun playbackControl() {
        when (playerStateInternal) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        playerStateInternal = STATE_PLAYING
        _playerState.value = _playerState.value?.copy(
            isPlaying = true
        )
        startPositionUpdates()
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        playerStateInternal = STATE_PAUSED
        _playerState.value = _playerState.value?.copy(
            isPlaying = false
        )
        stopPositionUpdates()
    }

    private fun startPositionUpdates() {
        updatePositionRunnable = object : Runnable {
            override fun run() {
                if (playerStateInternal == STATE_PLAYING) {
                    val position = getCurrentPosition()
                    _currentPosition.value = position
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

data class PlayerState(
    val track: Track,
    val isPlayButtonEnabled: Boolean,
    val isPlaying: Boolean
)