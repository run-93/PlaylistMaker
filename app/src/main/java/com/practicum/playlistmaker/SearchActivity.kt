package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

class SearchActivity : AppCompatActivity(), TrackAdapter.OnItemClickListener  {
    private var textSearch: String = "EDIT_TEXT_DEF"
    private lateinit var searchPref: SharedPreferences
    val SEARCH_KEY = "key_for_search_history"


    companion object{
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
    }

    // базовый адрес для API
    private val iTunesBaseUrl = "https://itunes.apple.com"

    //подключаем библиотеку retrofit для связывания json файлов и классов KOTLIN

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesService = retrofit.create(iTunesSearchApi::class.java)

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

    private val track = ArrayList<Track>()// список для результатов поиска
    private val trackHistory = ArrayList<Track>()// список для истории поиска
    private val adapter = TrackAdapter(this)// адаптер для результатов поиска
    private var adapterHistory = TrackAdapter(this)// адаптер для истории поиска


// создаем переменную в которой будет храниться история поиска
    private lateinit var searchHistory: SearchHistory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //инициализация объектов на view
        buttonBackSearch = findViewById<MaterialToolbar>(R.id.buttonBackSearch)
        clearButton = findViewById<ImageView>(R.id.clearIcon)
        inputEditText = findViewById<EditText>(R.id.search_edittext_view)
        trackListSearch = findViewById<RecyclerView>(R.id.trackListSearch)
        placeholderMessage = findViewById<TextView>(R.id.placeholderMessage)
        placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        updateButton = findViewById<Button>(R.id.updateButton)
        trackListSearchHistory = findViewById<RecyclerView>(R.id.trackListSearchHistory)
        clearHistory = findViewById<MaterialButton>(R.id.clearHistory)
        groopHistory =findViewById<LinearLayout>(R.id.storyTrack)

        // переход из активити поиска на главную актививти
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
            track.clear()
        }

        // после нажатия на кнопку "Обновить" запрос к серверу повторяется полностью
        updateButton.setOnClickListener {
            if (inputEditText.text.isNotEmpty()) {
                searchApi(inputEditText.text.toString())

            }
        }
        //условие для отображения списка истории поиска
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            groopHistory.visibility = if (hasFocus && inputEditText.text.isEmpty() && (searchHistory.getHistory().isNotEmpty())) View.VISIBLE else View.GONE
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Действия перед изменением текста (если необходимо)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Действия во время изменения текста
                clearButton.visibility = clearButtonVisibility(s)
                textSearch = s.toString()

                groopHistory.visibility = if (inputEditText.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Действия после изменения текста
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        adapter.track = track
        adapterHistory.track = trackHistory
        trackListSearch.adapter = adapter
        trackListSearchHistory.adapter = adapterHistory

        // осуществление поискового запроса не через кнопку на View, а через кнопку Done, которая появляется на клавиатуре
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (inputEditText.text.isNotEmpty()) {
                    searchApi(inputEditText.text.toString())

                }
                true
            } else {
                false
            }
        }

        // Инициализация SharedPreferences и SearchHistory
        searchPref = getSharedPreferences("SEARCH_HISTORY", Context.MODE_PRIVATE)
        searchHistory = SearchHistory(searchPref)
        loadTracksFromSharedPreferences()

        // Очистка истории
        clearHistory.setOnClickListener {
            searchHistory.clearHistory()
            loadTracksFromSharedPreferences() // Обновляем историю поиска
            groopHistory.visibility = if(searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE
        }

    }
    // Обработка клика на элементе
    override fun onItemClick(track: Track) {
        // Сохранение данных в SharedPreferences
        searchHistory.addTrack(track)
        loadTracksFromSharedPreferences() // Обновляем историю поиска
// переходим на активити Аудиоплеера
        val audioPlayerIntent = Intent(this, AudioPlayerActivity::class.java)
        audioPlayerIntent.putExtra(KEY_EXTRA_TRACK, track) // Передаем объект Track
        startActivity(audioPlayerIntent)

    }

private fun searchApi(query: String) {
    iTunesService.search(query).enqueue(object : Callback<TrackResponse> {
        override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
            // Логируем статус ответа и тело ответа
            val gson = Gson()
            val responseBodyString = gson.toJson(response.body())
            Log.d("ItunesResponse", "Response Body: $responseBodyString")

            if (response.code() == 200) {
                track.clear() // Очищаем список результатов поиска

                if (response.body()?.results?.isNotEmpty() == true) {
                    track.addAll(response.body()?.results!!) // Добавляем данные от сервера
                    adapter.notifyDataSetChanged()// Обновляем RecyclerView результатов
                    placeholderImage.visibility = View.GONE
                    placeholderMessage.visibility = View.GONE
                    updateButton.visibility = View.GONE
                }
                if (track.isEmpty()) {
                    showMessage(ErrorType.EMPTY_RESULT)
                }
            } else {
                showMessage(ErrorType.NETWORK_ERROR)
            }
        }

        override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
            showMessage(ErrorType.NETWORK_ERROR)
        }
    })
}

//сообщение выводимое при отсутствии интернета и пустом списке после поиска песни

    private fun showMessage(errorType: ErrorType, additionalMessage: String = "") {
        val (textID, imageRes, isButtonVisible) = when (errorType) {
            ErrorType.NETWORK_ERROR -> Triple(R.string.something_went_wrong,
                R.drawable.placeholdererrorinternet,
                true
            )

            ErrorType.EMPTY_RESULT -> Triple(R.string.nothing_found,
                R.drawable.placeholderempty,
                false
            )
        }

        // Установка текста и изображения
        placeholderImage.setImageResource(imageRes)
        placeholderImage.visibility = View.VISIBLE
        placeholderMessage.text = getString(textID)
        placeholderMessage.visibility = View.VISIBLE

        // Установка видимости кнопки
        updateButton.visibility = if (isButtonVisible) View.VISIBLE else View.GONE

        // Очистка списка и уведомление адаптера
        track.clear()
        adapter.notifyDataSetChanged()

        // Показ дополнительного сообщения (если оно есть)
        if (additionalMessage.isNotEmpty()) {
            Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
        }
    }

    // метод выполнения условия видимости кнопки очистки строки поиска
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
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
        outState.putString(EDIT_TEXT_KEY, textSearch)
    }


    // Метод получения текста поисковой строки из оперативную памяти и установка его в EditText
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        textSearch = savedInstanceState.getString(EDIT_TEXT_KEY, EDIT_TEXT_DEF)
    }

    // Загрузка данных из SharedPreferences и обновление RecyclerView
    private fun loadTracksFromSharedPreferences() {
        val tracks = searchHistory.getHistory()// получаем историю посика Track
        trackHistory.clear()// Очищаем список истории
        trackHistory.addAll(tracks)// Добавляем данные в список истории
        adapterHistory.track = trackHistory// Передаем данные в адаптер истории
        adapterHistory.notifyDataSetChanged() // Обновляем RecyclerView
    }

}