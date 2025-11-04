package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter

class SearchActivity : AppCompatActivity() {

    companion object {
        const val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
        const val EDIT_TEXT_DEF = ""
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            Creator.provideTrackInteractor(this)
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
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        setupListeners()
        observeViewModel()

        viewModel.showHistory()
    }

    private fun setupAdapters() {
        binding.trackListSearch.adapter = searchAdapter
        binding.trackListSearchHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.buttonBackSearch.setNavigationOnClickListener {
            finish()
        }

        binding.clearIcon.setOnClickListener {
            binding.searchEdittextView.setText("")
            hideKeyboard()
            viewModel.clearSearch()
        }

        binding.updateButton.setOnClickListener {
            val query = binding.searchEdittextView.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchDebounced(query)
            }
        }

        binding.clearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.searchEdittextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEdittextView.text.isEmpty()) {
                viewModel.showHistory()
            }
        }

        binding.searchEdittextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIcon.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
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
        binding.progressBar.isVisible = true
        binding.trackListSearch.isVisible = false
        binding.storyTrack.isVisible = false
        binding.placeholderImage.isVisible = false
        binding.placeholderMessage.isVisible = false
        binding.updateButton.isVisible = false
    }

    private fun showEmptyResult() {
        binding.progressBar.isVisible = false
        binding.trackListSearch.isVisible = false
        binding.storyTrack.isVisible = false
        binding.placeholderImage.setImageResource(R.drawable.placeholderempty)
        binding.placeholderMessage.setText(R.string.nothing_found)
        binding.placeholderImage.isVisible = true
        binding.placeholderMessage.isVisible = true
        binding.updateButton.isVisible = false
    }

    private fun showEmptyHistory() {
        binding.progressBar.isVisible = false
        binding.trackListSearch.isVisible = false
        binding.storyTrack.isVisible = false
        binding.placeholderImage.isVisible = false
        binding.placeholderMessage.isVisible = false
        binding.updateButton.isVisible = false
    }

    private fun showSearchResults(tracks: List<com.practicum.playlistmaker.search.domain.models.Track>) {
        binding.progressBar.isVisible = false
        searchAdapter.tracks = tracks
        binding.trackListSearch.isVisible = true
        binding.storyTrack.isVisible = false
        binding.placeholderImage.isVisible = false
        binding.placeholderMessage.isVisible = false
        binding.updateButton.isVisible = false
    }

    private fun showSearchHistory(tracks: List<com.practicum.playlistmaker.search.domain.models.Track>) {
        binding.progressBar.isVisible = false
        historyAdapter.tracks = tracks
        binding.trackListSearch.isVisible = false
        binding.storyTrack.isVisible = tracks.isNotEmpty()
        binding.placeholderImage.isVisible = false
        binding.placeholderMessage.isVisible = false
        binding.updateButton.isVisible = false
    }

    private fun showError(errorType: com.practicum.playlistmaker.common.ErrorType) {
        binding.progressBar.isVisible = false
        binding.trackListSearch.isVisible = false
        binding.storyTrack.isVisible = false

        when (errorType) {
            com.practicum.playlistmaker.common.ErrorType.NETWORK_ERROR -> {
                binding.placeholderImage.setImageResource(R.drawable.placeholdererrorinternet)
                binding.placeholderMessage.setText(R.string.something_went_wrong)
                binding.updateButton.isVisible = true
            }
            com.practicum.playlistmaker.common.ErrorType.EMPTY_RESULT -> {
                binding.placeholderImage.setImageResource(R.drawable.placeholderempty)
                binding.placeholderMessage.setText(R.string.nothing_found)
                binding.updateButton.isVisible = false
            }
        }

        binding.placeholderImage.isVisible = true
        binding.placeholderMessage.isVisible = true
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT_KEY, binding.searchEdittextView.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(EDIT_TEXT_KEY) ?: EDIT_TEXT_DEF
        binding.searchEdittextView.setText(savedText)
    }
}