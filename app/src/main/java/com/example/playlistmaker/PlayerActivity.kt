package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayerActivity : AppCompatActivity() {

    private var playerState = STATE_DEFAULT
    private var mediaPlayer = MediaPlayer()
    private var progressHandler = Handler(Looper.getMainLooper())
    private lateinit var play: ImageView
    private lateinit var progress: TextView
    private lateinit var favoriteTracksDao: FavoriteTracksDao

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val db = AppDatabase.getInstance(this)
        favoriteTracksDao = db.favoriteTracksDao()

        val track = intent.getParcelableExtra<Track>("track")
        play = findViewById(R.id.play_pause)
        progress = findViewById(R.id.progress)
        val addToFavorites = findViewById<ImageView>(R.id.add_to_favorites)

        track?.let {
            setupPlayer(it)
            checkIfFavorite(it)

            if (!it.previewUrl.isNullOrEmpty()) {
                preparePlayer(it.previewUrl)
            } else {
                play.isEnabled = false
            }
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        play.setOnClickListener {
            playbackControl()
        }

        addToFavorites.setOnClickListener {
            track?.let { currentTrack ->
                CoroutineScope(Dispatchers.IO).launch {
                    val existingTrack = favoriteTracksDao.getById(currentTrack.trackId)
                    if (existingTrack != null) {
                        favoriteTracksDao.delete(existingTrack)
                        withContext(Dispatchers.Main) {
                            addToFavorites.setImageResource(R.drawable.icon_unlike)
                        }
                    } else {
                        val favoriteTrack = FavoriteTrackEntity(
                            trackId = track.trackId,
                            trackName = track.trackName,
                            artistName = track.artistName,
                            trackTime = track.trackTime,
                            artworkUrl100 = track.artworkUrl100,
                            collectionName = track.collectionName,
                            releaseDate = track.releaseDate,
                            primaryGenreName = track.primaryGenreName,
                            country = track.country,
                            previewUrl = track.previewUrl
                        )
                        favoriteTracksDao.insert(favoriteTrack)
                        withContext(Dispatchers.Main) {
                            addToFavorites.setImageResource(R.drawable.icon_like)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
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

    private fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            play.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            play.setImageResource(R.drawable.icon_play)
            playerState = STATE_PREPARED
        }
    }

    private fun startPlayer() {
        if ((playerState == STATE_PREPARED || playerState == STATE_PAUSED) && !mediaPlayer.isPlaying) {
            mediaPlayer.start()
            play.setImageResource(R.drawable.icon_pause)
            startProgressUpdater()
            playerState = STATE_PLAYING
        }
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        play.setImageResource(R.drawable.icon_play)
        playerState = STATE_PAUSED
        progressHandler.removeCallbacks(progressUpdater)
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        progressHandler.removeCallbacks(progressUpdater)
    }

    private fun startProgressUpdater() {
        progressHandler.postDelayed(progressUpdater, 100)
    }

    private val progressUpdater = object : Runnable {
        override fun run() {
            if ((playerState == STATE_PLAYING) && mediaPlayer.isPlaying) {
                progress.text = formatTime(mediaPlayer.currentPosition)
                progressHandler.postDelayed(this, 300)
            }
        }
    }

    private fun formatTime(millis: Int): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(millis)
    }

    private fun checkIfFavorite(track: Track) {
        CoroutineScope(Dispatchers.IO).launch {
            val isFavorite = favoriteTracksDao.getById(track.trackId) != null
            withContext(Dispatchers.Main) {
                val iconRes = if (isFavorite) R.drawable.icon_like else R.drawable.icon_unlike
                findViewById<ImageView>(R.id.add_to_favorites).setImageResource(iconRes)
            }
        }
    }

}