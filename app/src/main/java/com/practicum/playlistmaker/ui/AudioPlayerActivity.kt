package com.practicum.playlistmaker.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.ui.AudioPlayerActivity.Companion.STATE_PAUSED
import com.practicum.playlistmaker.ui.AudioPlayerActivity.Companion.STATE_PLAYING
import com.practicum.playlistmaker.ui.AudioPlayerActivity.Companion.STATE_PREPARED
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val KEY_EXTRA_TRACK = "track"

class AudioPlayerActivity : AppCompatActivity() {
// константы для отслеживания состояния плеера
    companion object {
    const val KEY_EXTRA_TRACK = "track_extra"
    private const val STATE_DEFAULT = 0 //Проинициализирован, но не подготовлен (освобождён)
    private const val STATE_PREPARED = 1 //Подготовлен, но не воспроизводит аудио
    private const val STATE_PLAYING = 2 //Воспроизводит аудио
    private const val STATE_PAUSED = 3 //Воспроизведение приостановлено (пауза)
    }

    private var playerState = STATE_DEFAULT // переменная, хранящая состояние плеера

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
    private lateinit var currentTrack: String


    private var audioPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper()) // переменная задачи в цикле главного потока

    private lateinit var updateDuration: Runnable //переменная запроса длительности трека

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_player)

        initViews() // запускаем функцию инициализации объктов View
        setupClickListeners() // запускаем функцию установки слушателей на элементы View

// Получаем данные трека из Intent и используем метод в зависимости от версии SDK устроства
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(KEY_EXTRA_TRACK) as? Track
        } ?: run {
            finish() // Закрываем активити, если трек не передан
            return
        }
        setupTrackInfo(track)
        setupPlayer(track)

    }
    //функция инициализации объектов на view
    private fun initViews() {
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
    }

    // функция установки слушателей на элементы View
    private fun setupClickListeners(){
        // переход из активити поиска на главную актививти
        buttonBack.setOnClickListener{
            val backIntent = Intent(this, SearchActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backIntent)
        }
        buttonPlay.setOnClickListener{
            playbackControl()
        }
    }

    // функция вывода на экран информации о треках
    private fun setupTrackInfo(track: Track){
        trackName.text = track.trackName
        artistName.text = track.artistName
        durationValue.text = track.trackTime
        albumValue.text = track.collectionName ?: "Unknown Album"
        releaseValue.text = track.releaseDate?.take(4) ?: "Unknown Year" // форматируем строчку releaseDate
        genreValue.text = track.primaryGenreName ?: "Unknown Genre"
        countryValue.text = track.country ?: "Unknown Country"
        currentTrack = track.previewUrl.toString()

        loadTrackCover(track)
    }
    private fun loadTrackCover(track: Track) {
        val artworkUrl = track.getCoverArtwork().takeIf { it.isNotBlank() } ?: run {
            imageCover.setImageResource(R.drawable.placeholder)
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
            .into(imageCover)
    }
    private fun setupPlayer(track: Track){
        // запускаем запрос времени в основном потоке
        updateDuration = object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    val currentPosition = audioPlayer.currentPosition
                    val time = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
                    timePlay.text = time
                    handler.postDelayed(this, 300) //
                }
            }
        }
        preparePlayer()
    }
    // метод подготовки плеера
    private fun preparePlayer() {
        audioPlayer.setDataSource(currentTrack) // источник воспроизведения песни (ссылка из класса Track)
        audioPlayer.prepareAsync() // подготовка плеера происходит в отдельном потоке
        audioPlayer.setOnPreparedListener { // метод определения завершения подготовки
            buttonPlay.isEnabled = true
            playerState = STATE_PREPARED
        }
        audioPlayer.setOnCompletionListener { // метод отслеживания завершения воспроизведения
            buttonPlay.setImageResource(R.drawable.button_play)
            handler.removeCallbacks(updateDuration)
            timePlay.text = "00:00"
            playerState = STATE_PREPARED
        }
    }
    // метод старта плеера
    private fun startPlayer() {
        audioPlayer.start()
        buttonPlay.setImageResource(R.drawable.pause)
        playerState = STATE_PLAYING
        handler.post(updateDuration)
    }
    // метод паузы плеера
    private fun pausePlayer() {
        audioPlayer.pause()
        buttonPlay.setImageResource(R.drawable.button_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateDuration) // Останавливаем обновление времени
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    // функция жизненного цикла Активити, при сворачивании приложения аудио приостанавливается, если
//    метод убрать то звук продолжит проигрываться при свернутом приложении.
    override fun onPause() {
        super.onPause()
        pausePlayer()
        handler.removeCallbacks(updateDuration)
    }
    // освобождаем память и ресурсы процессора при закрытии приложения
    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
        handler.removeCallbacks(updateDuration)
    }
}