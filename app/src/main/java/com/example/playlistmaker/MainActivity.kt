package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.View


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Implementing SEARCH button reaction via an anonymous class
        val searchButton = findViewById<Button>(R.id.search_button)
        val searchButtonClickListener: View.OnClickListener = object : View.OnClickListener { override fun
                onClick(v: View?) {
                    val searchIntent = Intent(this@MainActivity, SearchActivity::class.java)
                    startActivity(searchIntent)
                }
        }
        searchButton.setOnClickListener(searchButtonClickListener)

        // Implementing MEDIA LIBRARY button reaction via an lambda expression
        val mediaButton = findViewById<Button>(R.id.media_button)
        mediaButton.setOnClickListener{
            val searchIntent = Intent(this@MainActivity, MediaActivity::class.java)
            startActivity(searchIntent)
        }

        // Implementing SETTINGS button reaction via an lambda expression
        val settingsButton = findViewById<Button>(R.id.settings_button)
        settingsButton.setOnClickListener{
            val searchIntent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(searchIntent)
        }

    }
}