package com.practicum.playlistmaker.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.ErrorType
import com.practicum.playlistmaker.MainActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.SearchHistory
import com.practicum.playlistmaker.domain.api.TrackInteractor
import com.practicum.playlistmaker.domain.models.Track

class SearchActivity : AppCompatActivity(), TrackAdapter.OnItemClickListener {

    companion object{
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
        private const val CLICK_DEBOUNCE_DELAY = 1000L// константа определяющая задержку нажатия на элемент списка
        private const val SEARCH_DEBOUNCE_DELAY = 2000L // константа определяющая задержку автоматического выполнения поиск трека
        private const val SEARCH_KEY = "key_for_search_history"
    }

    private lateinit var buttonBackSearch: MaterialToolbar
    private lateinit var clearButton: ImageView
    private lateinit var inputEditText: EditText
    private lateinit var trackListSearch: RecyclerView
    private lateinit var placeholderMessage: TextView
    private lateinit var placeholderImage: ImageView
    private lateinit var updateButton: Button
    private lateinit var trackListSearchHistory: RecyclerView
    private lateinit var clearHistory: MaterialButton
    private lateinit var groopHistory: LinearLayout
    private lateinit var progressBar: ProgressBar

    private val track = ArrayList<Track>()// список для результатов поиска
    private val trackHistory = ArrayList<Track>()// список для истории поиска
    private val adapter = TrackAdapter(this)// адаптер для результатов поиска
    private var adapterHistory = TrackAdapter(this)// адаптер для истории поиска
    private val handler = Handler(Looper.getMainLooper()) // переменная задачи в цикле основного потока
    private var isClickAllowed = true// глобальная переменная определяющая возможность нажатия на кнопку
    private lateinit var searchHistory: SearchHistory // создаем переменную в которой будет храниться история поиска
    private lateinit var trackInteractor: TrackInteractor // переменая для отправки запроса на сервер


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        trackInteractor =
            Creator.provideTrackInteractor()// инициализация интерактора для создания класса со строкой поиска

        searchHistory = SearchHistory(
            getSharedPreferences(
                "SEARCH_HISTORY",
                Context.MODE_PRIVATE
            )
        )  // Инициализация ShadPrefererences и SearchHistory

        initViews()
        setupAdapter()
        setupListeners()
        loadSearchHistory()
    }

        //инициализация объектов на view
    private fun initViews() {
        buttonBackSearch = findViewById<MaterialToolbar>(R.id.buttonBackSearch)
        clearButton = findViewById<ImageView>(R.id.clearIcon)
        inputEditText = findViewById<EditText>(R.id.search_edittext_view)
        trackListSearch = findViewById<RecyclerView>(R.id.trackListSearch)
        placeholderMessage = findViewById<TextView>(R.id.placeholderMessage)
        placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        updateButton = findViewById<Button>(R.id.updateButton)
        trackListSearchHistory = findViewById<RecyclerView>(R.id.trackListSearchHistory)
        clearHistory = findViewById<MaterialButton>(R.id.clearHistory)
        groopHistory = findViewById<LinearLayout>(R.id.storyTrack)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        }
    //функция запуска адаптеров
    private fun setupAdapter(){
        trackListSearch.adapter = adapter
        trackListSearchHistory.adapter = adapterHistory
    }
    //функция установки слушаталей нажатий на кнопки
    private fun setupListeners() {
        buttonBackSearch.setNavigationOnClickListener{
            val backSearchIntent = Intent(this, MainActivity::class.java)
            backSearchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backSearchIntent)
            finish()
        }

        // после нажатия кнопки очиски поиска, строка становиться пустой и закрывается клавиатура
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            clearSearchResults()
        }

        // после нажатия на кнопку "Обновить" запрос к серверу повторяется полностью через интерактор
        updateButton.setOnClickListener {
            performSearch(inputEditText.text.toString())

            }

        //кнопка очистки истории поиска
        clearHistory.setOnClickListener {
            searchHistory.clearHistory()
            loadSearchHistory()
        }
        //условие для отображения списка истории поиска
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            groopHistory.visibility = if (hasFocus && inputEditText.text.isEmpty()
                && (searchHistory.getHistory().isNotEmpty())) View.VISIBLE else View.GONE
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (s.isNullOrEmpty()) {
                    loadSearchHistory() // Показывает историю поиска при пустом поле
                } else {
                    handler.removeCallbacks(searchRunnable) // Отменяет предыдущий отложенный поиск
                    handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY) // Запускает поиск с задержкой
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private val searchRunnable = Runnable {
        performSearch(inputEditText.text.toString())
    }

    // дополнительная функция проверки наличия интернета
    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            clearSearchResults()
            hideErrorMessages() // Скрываем все сообщения при пустом запросе
            return
        }

        if (!isOnline()) {
            showMessage(ErrorType.NETWORK_ERROR)
            return
        }

        // Перед новым поиском скрываем предыдущие сообщения
        hideErrorMessages()
        progressBar.visibility = View.VISIBLE
        trackListSearch.visibility = View.GONE // Скрываем RecyclerView на время загрузки
        try {
            trackInteractor.search(query, object : TrackInteractor.TrackConsumer {
                override fun consume(foundTrack: List<Track>) {
                    handler.post {
                        progressBar.visibility = View.GONE
                        if (foundTrack.isEmpty()) {
                            showEmptyResult()
                        } else {
                            showSearchResults(foundTrack)
                        }
                    }
                }

                override fun onError(error: ErrorType) {
                    handler.post {
                        progressBar.visibility = View.GONE
                        showMessage(error)
                    }
                }
            })
        } catch (e: Exception) {
            handler.post {
                progressBar.visibility = View.GONE
                showMessage(ErrorType.NETWORK_ERROR)
            }
        }
    }

    private fun showEmptyResult() {
        // Очищаем результаты и скрываем историю
        track.clear()
        adapter.notifyDataSetChanged()
        trackListSearch.visibility = View.GONE

        // Показываем сообщение "Ничего не найдено"
        placeholderImage.setImageResource(R.drawable.placeholderempty)
        placeholderMessage.setText(R.string.nothing_found)
        placeholderImage.visibility = View.VISIBLE
        placeholderMessage.visibility = View.VISIBLE

        groopHistory.visibility = View.GONE
        clearButton.visibility = View.GONE
    }

    private fun showSearchResults(tracks: List<Track>) {
        // Обновляем результаты поиска
        track.clear()
        track.addAll(tracks)
        adapter.track = track
        adapter.notifyDataSetChanged()

        // Показываем RecyclerView и скрываем остальные элементы
        trackListSearch.visibility = View.VISIBLE
        groopHistory.visibility = View.GONE
        placeholderImage.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        updateButton.visibility = View.GONE
        // Показываем кнопку очистки, если есть текст
        clearButton.visibility = if (inputEditText.text.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideErrorMessages() {
        placeholderImage.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        updateButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun clearSearchResults() {
        track.clear()
        adapter.notifyDataSetChanged()
        trackListSearch.visibility = View.GONE
        hideErrorMessages()
        loadSearchHistory()
    }

          // функция вывода на экран истории поиска
    private fun loadSearchHistory() {
        trackHistory.clear()
        trackHistory.addAll(searchHistory.getHistory())
        adapterHistory.track = trackHistory
        adapterHistory.notifyDataSetChanged()

        groopHistory.visibility = if (inputEditText.hasFocus() && trackHistory.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    // Обработка клика на элементе
    // переходим на активити Аудиоплеера с исключением множественного нажатия на элемент списка треков
    override fun onItemClick(track: Track) {
        if (clickDebounce()) {
            searchHistory.addTrack(track)
            startActivity(Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra(AudioPlayerActivity.KEY_EXTRA_TRACK, track)
            })
        }
    }

   // метод задержки нажатия на кнопку (для исключения двойного нажатия)
    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

   //сообщение выводимое при отсутствии интернета и пустом списке после поиска песни
   private fun showMessage(errorType: ErrorType) {
       // Скрываем историю поиска при любой ошибке
       groopHistory.visibility = View.GONE

       when (errorType) {
           ErrorType.NETWORK_ERROR -> {
               track.clear()
               adapter.notifyDataSetChanged()
               placeholderImage.setImageResource(R.drawable.placeholdererrorinternet)
               placeholderMessage.setText(R.string.something_went_wrong)
               updateButton.visibility = View.VISIBLE
           }
           ErrorType.EMPTY_RESULT -> {
               track.clear()
               adapter.notifyDataSetChanged()
               placeholderImage.setImageResource(R.drawable.placeholderempty)
               placeholderMessage.setText(R.string.nothing_found)
               updateButton.visibility = View.GONE
           }
       }

       placeholderImage.visibility = View.VISIBLE
       placeholderMessage.visibility = View.VISIBLE
       progressBar.visibility = View.GONE
   }

    // метод выполняющий скрытие клавиатуры
    private fun hideKeyboard() {
        // Получение InputMethodManager
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Проверка на наличие открытой клавиатуры
        val view = currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
    // Метод сохранения текста поисковой строки в оперативную память
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT_KEY, inputEditText.text.toString())
    }


    // Метод получения текста поисковой строки из оперативную памяти и установка его в EditText
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(EDIT_TEXT_KEY) ?: EDIT_TEXT_DEF
        inputEditText.setText(savedText)
    }



}
