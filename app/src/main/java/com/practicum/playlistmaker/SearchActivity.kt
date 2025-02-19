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
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {
    private var textSearch: String = "EDIT_TEXT_DEF"

    companion object{
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //инициализация view
        val buttonBackSearch = findViewById<MaterialToolbar>(R.id.buttonBackSearch)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        val inputEditText = findViewById<EditText>(R.id.search_edittext_view)

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
}