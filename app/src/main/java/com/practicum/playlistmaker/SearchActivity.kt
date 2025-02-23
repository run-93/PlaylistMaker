package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.bumptech.glide.Glide

class SearchActivity : AppCompatActivity() {
    private var textSearch: String = "EDIT_TEXT_DEF"

    companion object{
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //инициализация объектов на view
        val buttonBackSearch = findViewById<MaterialToolbar>(R.id.buttonBackSearch)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        val inputEditText = findViewById<EditText>(R.id.search_edittext_view)
        val trackListSearch = findViewById<RecyclerView>(R.id.trackListSearch)

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
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Действия перед изменением текста (если необходимо)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Действия во время изменения текста
                clearButton.visibility = clearButtonVisibility(s)
                textSearch = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // Действия после изменения текста
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        trackListSearch.adapter = trackListAdapter
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

    val trackListAdapter = TrackAdapter(
        listOf(
            Track("Smells Like Teen Spirit",
                "Nirvana",
                "5:01",
                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"),
            Track("Billie Jean",
                "Michael Jackson",
                "4:35",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"),
            Track("Stayin' Alive",
                "Bee Gees",
                "4:10",
                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"),
            Track("Whole Lotta Love",
                "Led Zeppelin",
                "5:33",
                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"),
            Track("Sweet Child O'Mine",
                "Guns N' Roses",
                "5:03",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"),
        )
    )

}