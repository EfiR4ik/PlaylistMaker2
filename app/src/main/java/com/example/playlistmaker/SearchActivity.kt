package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: ImageView
    private var searchQuery: String? = null
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var allTracks: List<Track>
    private lateinit var searchHistory: SearchHistory
    private lateinit var recyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyTitle: TextView
    private var displayedTracks: List<Track> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById(R.id.search_edit_text)
        clearSearchButton = findViewById(R.id.clear_search_button)
        recyclerView = findViewById(R.id.recyclerView)
        clearHistoryButton = findViewById(R.id.clear_history_button)

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
                filterTracks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
            }
        })

        // Clear button ClickListener
        clearSearchButton.setOnClickListener {
            searchEditText.setText("")
            clearSearchButton.visibility = ImageView.GONE
            updateUI()
        }

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery")
            searchEditText.setText(searchQuery)
        }

        // Test list of tracks
        allTracks = listOf(
            Track(1, "Smells Like Teen Spirit", "Nirvana", "5:01", "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"),
            Track(2, "Billie Jean", "Michael Jackson", "4:35", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"),
            Track(3, "Stayin' Alive", "Bee Gees", "4:10", "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"),
            Track(4, "Whole Lotta Love", "Led Zeppelin", "5:33", "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"),
            Track(5, "Sweet Child O'Mine", "Guns N' Roses", "5:03", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg")
        )

        // Adapter initialization
        tracksAdapter = TracksAdapter(displayedTracks) { track ->
            searchHistory.addTrack(track)
            updateUI()
        }
        recyclerView.adapter = tracksAdapter

        // Search history title
        historyTitle = findViewById(R.id.history_title)

        // Clear history button
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

    // Function for track filtration
    private fun filterTracks(query: String) {
        displayedTracks = if (query.isEmpty()) {
            searchHistory.getHistory()
        } else {
            allTracks.filter { track ->
                track.trackName.contains(query, ignoreCase = true) || track.artistName.contains(query, ignoreCase = true)
            }
        }
        tracksAdapter.updateTracks(displayedTracks)
        updateUI()
    }

    // Function for ui update
    private fun updateUI() {
        val hasHistory = searchHistory.getHistory().isNotEmpty()
        val isSearchEmpty = searchEditText.text.isNullOrEmpty()
        recyclerView.visibility = if (displayedTracks.isNotEmpty()) View.VISIBLE else View.GONE
        clearHistoryButton.visibility = if (hasHistory && isSearchEmpty) View.VISIBLE else View.GONE
        historyTitle.visibility = if (hasHistory && isSearchEmpty) View.VISIBLE else View.GONE
    }

    // Function for load search history
    private fun loadTracks() {
        displayedTracks = searchHistory.getHistory()
        tracksAdapter.updateTracks(displayedTracks)
        updateUI()
    }
}