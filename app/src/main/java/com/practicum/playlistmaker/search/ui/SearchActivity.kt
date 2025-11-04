package com.practicum.playlistmaker.search.ui

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter

class SearchActivity : AppCompatActivity() {

    companion object {
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
        private const val CLICK_DEBOUNCE_DELAY = 1000L
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

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            Creator.provideTrackRepository(),
            Creator.provideSearchHistoryRepository(this)
        )
    }

    private val searchAdapter = TrackAdapter { track ->
        if (clickDebounce()) {
            viewModel.addTrackToHistory(track)
            startActivity(
                Intent(this, AudioPlayerActivity::class.java).apply {
                    putExtra(AudioPlayerActivity.KEY_EXTRA_TRACK, track)
                }
            )
        }
    }

    private val historyAdapter = TrackAdapter { track ->
        if (clickDebounce()) {
            viewModel.addTrackToHistory(track)
            startActivity(
                Intent(this, AudioPlayerActivity::class.java).apply {
                    putExtra(AudioPlayerActivity.KEY_EXTRA_TRACK, track)
                }
            )
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupAdapters()
        setupListeners()
        observeViewModel()

        viewModel.showHistory()
    }

    private fun initViews() {
        buttonBackSearch = findViewById(R.id.buttonBackSearch)
        clearButton = findViewById(R.id.clearIcon)
        inputEditText = findViewById(R.id.search_edittext_view)
        trackListSearch = findViewById(R.id.trackListSearch)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        placeholderImage = findViewById(R.id.placeholderImage)
        updateButton = findViewById(R.id.updateButton)
        trackListSearchHistory = findViewById(R.id.trackListSearchHistory)
        clearHistory = findViewById(R.id.clearHistory)
        groopHistory = findViewById(R.id.storyTrack)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupAdapters() {
        trackListSearch.adapter = searchAdapter
        trackListSearchHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        buttonBackSearch.setNavigationOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            viewModel.clearSearch()
        }

        updateButton.setOnClickListener {
            val query = inputEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchDebounced(query)
            }
        }

        clearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                viewModel.showHistory()
            }
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                viewModel.searchDebounced(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is SearchState.Loading -> showLoading()
                is SearchState.Empty -> showEmptyResult()
                is SearchState.EmptyHistory -> showEmptyHistory()
                is SearchState.Content -> showSearchResults(state.tracks)
                is SearchState.History -> showSearchHistory(state.tracks)
                is SearchState.Error -> showError(state.errorType)
            }
        }
    }

    private fun showLoading() {
        progressBar.isVisible = true
        trackListSearch.isVisible = false
        groopHistory.isVisible = false
        placeholderImage.isVisible = false
        placeholderMessage.isVisible = false
        updateButton.isVisible = false
    }

    private fun showEmptyResult() {
        progressBar.isVisible = false
        trackListSearch.isVisible = false
        groopHistory.isVisible = false
        placeholderImage.setImageResource(R.drawable.placeholderempty)
        placeholderMessage.setText(R.string.nothing_found)
        placeholderImage.isVisible = true
        placeholderMessage.isVisible = true
        updateButton.isVisible = false
    }

    private fun showEmptyHistory() {
        progressBar.isVisible = false
        trackListSearch.isVisible = false
        groopHistory.isVisible = false
        placeholderImage.isVisible = false
        placeholderMessage.isVisible = false
        updateButton.isVisible = false
    }

    private fun showSearchResults(tracks: List<com.practicum.playlistmaker.search.domain.models.Track>) {
        progressBar.isVisible = false
        searchAdapter.tracks = tracks
        trackListSearch.isVisible = true
        groopHistory.isVisible = false
        placeholderImage.isVisible = false
        placeholderMessage.isVisible = false
        updateButton.isVisible = false
    }

    private fun showSearchHistory(tracks: List<com.practicum.playlistmaker.search.domain.models.Track>) {
        progressBar.isVisible = false
        historyAdapter.tracks = tracks
        trackListSearch.isVisible = false
        groopHistory.isVisible = tracks.isNotEmpty()
        placeholderImage.isVisible = false
        placeholderMessage.isVisible = false
        updateButton.isVisible = false
    }

    private fun showError(errorType: com.practicum.playlistmaker.common.ErrorType) {
        progressBar.isVisible = false
        trackListSearch.isVisible = false
        groopHistory.isVisible = false

        when (errorType) {
            com.practicum.playlistmaker.common.ErrorType.NETWORK_ERROR -> {
                placeholderImage.setImageResource(R.drawable.placeholdererrorinternet)
                placeholderMessage.setText(R.string.something_went_wrong)
                updateButton.isVisible = true
            }
            com.practicum.playlistmaker.common.ErrorType.EMPTY_RESULT -> {
                placeholderImage.setImageResource(R.drawable.placeholderempty)
                placeholderMessage.setText(R.string.nothing_found)
                updateButton.isVisible = false
            }
        }

        placeholderImage.isVisible = true
        placeholderMessage.isVisible = true
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT_KEY, inputEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(EDIT_TEXT_KEY) ?: EDIT_TEXT_DEF
        inputEditText.setText(savedText)
    }
}