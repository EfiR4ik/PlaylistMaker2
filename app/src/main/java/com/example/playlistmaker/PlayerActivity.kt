package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val track = intent.getParcelableExtra<Track>("track")
        track?.let { setupPlayer(it) }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun setupPlayer(track: Track) {
        // Set track info
        findViewById<TextView>(R.id.track_name).text = track.trackName
        findViewById<TextView>(R.id.artist_name).text = track.artistName
        findViewById<TextView>(R.id.track_time).text = track.trackTime
        findViewById<TextView>(R.id.collection_name).text = track.collectionName
        findViewById<TextView>(R.id.release_date).text = track.releaseDate
        findViewById<TextView>(R.id.primary_genre_name).text = track.primaryGenreName
        findViewById<TextView>(R.id.country).text = track.country

        // Load cover art with Glide
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.track_cover_placeholder)
            .centerCrop()
            .transform(RoundedCorners(20))
            .into(findViewById(R.id.album_cover))
    }
}