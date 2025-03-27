package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.icu.text.SimpleDateFormat
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: ImageView
    private var searchQuery: String? = null
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var searchHistory: SearchHistory
    private lateinit var recyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyTitle: TextView
    private var displayedTracks: List<Track> = emptyList()
    private lateinit var iTunesApi: ITunesApi
    private lateinit var errorNotFound: FrameLayout
    private lateinit var errorNoConnection: LinearLayout
    private lateinit var refreshButton: Button
    private var lastSearchQuery: String? = null
    private lateinit var progressBar: ProgressBar
    private val searchRunnable = Runnable { performSearch(searchQuery.toString()) }
    private val handler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true


    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById(R.id.search_edit_text)
        clearSearchButton = findViewById(R.id.clear_search_button)
        recyclerView = findViewById(R.id.recyclerView)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        errorNotFound = findViewById(R.id.errorNotFound)
        errorNoConnection = findViewById(R.id.errorNoConnection)
        refreshButton = findViewById(R.id.refreshButton)
        progressBar = findViewById(R.id.progressBar)
        
        // Retrofit init
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        iTunesApi = retrofit.create(ITunesApi::class.java)

        // Keyboard done button click listener
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString()
                performSearch(query)
            }
            false
        }

        // Refresh button click listener
        refreshButton.setOnClickListener{
            showLoading()
            lastSearchQuery?.let { query ->
                if (query.isNotEmpty()){
                    performSearch(query)
                }
            }
            hideLoading()
        }

        // Shared preferences and Search History
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        // Back button
        val backButton = findViewById<ImageView>(R.id.search_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Search line TextWatcher
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearSearchButton.visibility = if (s.isNullOrEmpty()) {
                    ImageView.GONE
                } else {
                    ImageView.VISIBLE
                }
                if (s.isNullOrEmpty()) {
                    updateUI()
                }
                searchQuery = s.toString()
                searchDebounce()
            }

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
            }
        })

        // Clear button ClickListener
        clearSearchButton.setOnClickListener {
            searchEditText.text.clear()
            clearSearchButton.visibility = ImageView.GONE
            updateUI()
        }

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery")
            searchEditText.setText(searchQuery)
        }

        // Adapter initialization
        tracksAdapter = TracksAdapter(displayedTracks) { track ->
            if(clickDebounce()) {
                searchHistory.addTrack(track)
                updateUI()

                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
        recyclerView.adapter = tracksAdapter

        // Search history title init
        historyTitle = findViewById(R.id.history_title)

        // Clear history button click listener
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            displayedTracks = emptyList()
            tracksAdapter.updateTracks(displayedTracks)
            updateUI()
        }

            // On create UI update
            updateUI()
    }

    override fun onResume() {
        super.onResume()
        loadTracks()
    }

    // Functions for save search query when screen rotated
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchQuery", searchQuery)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString("searchQuery")
        searchEditText.setText(searchQuery)
    }

    // Function for ui update
    private fun updateUI() {
        hideAllMessages()

        val hasHistory = searchHistory.getHistory().isNotEmpty()
        val isSearchEmpty = searchEditText.text.isNullOrEmpty()

        if (isSearchEmpty && hasHistory) {
            loadTracks()
        } else if (!isSearchEmpty) {
            recyclerView.visibility = if (displayedTracks.isNotEmpty()) View.VISIBLE else View.GONE
            if (displayedTracks.isEmpty()) {
                showNoResults()
            }
        }

        clearHistoryButton.visibility = if (hasHistory && isSearchEmpty) View.VISIBLE else View.GONE
        historyTitle.visibility = if (hasHistory && isSearchEmpty) View.VISIBLE else View.GONE
    }

    // Function for load search history
    private fun loadTracks() {
        if (searchEditText.text.isNullOrEmpty()) {
            displayedTracks = searchHistory.getHistory()
            tracksAdapter.updateTracks(displayedTracks)
        }
    }

    // Perform search function
    private fun performSearch(query: String) {
        showLoading()
        lastSearchQuery = query
        iTunesApi.search(query).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                if (response.isSuccessful) {
                    response.body()?.results?.let { trackResponses ->
                        displayedTracks = trackResponses.map {
                            Track(
                                trackId = it.trackId,
                                trackName = it.trackName,
                                artistName = it.artistName,
                                trackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis),
                                artworkUrl100 = it.artworkUrl100,
                                collectionName = it.collectionName ?: "",
                                releaseDate = formatReleaseDate(it.releaseDate),
                                primaryGenreName = it.primaryGenreName ?: "",
                                country = it.country ?: "",
                                previewUrl = it.previewUrl ?: "",
                            )
                        }
                        tracksAdapter.updateTracks(displayedTracks)
                        updateUI()
                        if (displayedTracks.isEmpty()) {
                            showNoResults()
                            hideLoading()
                        }
                        else {
                            hideAllMessages()
                            hideLoading()
                        }
                    } ?: run {
                        showNoResults()
                        hideLoading()
                    }
                } else {
                    showError()
                    hideLoading()
                }
            }
            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                showError()
                hideLoading()
            }
        })
    }

    private fun showNoResults() {
        errorNotFound.visibility = View.VISIBLE
        errorNoConnection.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun showError() {
        errorNotFound.visibility = View.GONE
        errorNoConnection.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideAllMessages() {

        errorNotFound.visibility = View.GONE
        errorNoConnection.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showLoading(){
        progressBar.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun hideLoading(){
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun formatReleaseDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString.take(4)
        }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }


}


