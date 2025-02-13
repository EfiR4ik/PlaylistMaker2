package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: ImageView
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //Back button
        val backButton = findViewById<ImageView>(R.id.search_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Search EditText and Clear button
        searchEditText = findViewById(R.id.search_edit_text)
        clearSearchButton = findViewById<ImageView>(R.id.clear_search_button)

        // TextWatcher
        val searchLineTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearSearchButton.visibility = if (s.isNullOrEmpty()) {
                    ImageView.GONE
                } else {
                    ImageView.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString() // Сохраняем текст в переменную
            }
        }
        searchEditText.addTextChangedListener(searchLineTextWatcher)

        //Clear button click listener
        clearSearchButton.setOnClickListener {
            searchEditText.setText("")
            clearSearchButton.visibility = ImageView.GONE
        }

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery")
            searchEditText.setText(searchQuery)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchQuery", searchQuery)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString("searchQuery")
        searchEditText.setText(searchQuery)
    }
}