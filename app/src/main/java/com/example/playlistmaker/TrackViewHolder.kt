package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val trackTitle: TextView = itemView.findViewById(R.id.track_name)
    private val trackArtist: TextView = itemView.findViewById(R.id.track_author)
    private val trackDuration: TextView = itemView.findViewById(R.id.track_time)
    private val trackImage: ImageView = itemView.findViewById(R.id.track_cover)

    fun bind(track: Track) {
        trackTitle.text = track.trackName
        trackArtist.text = track.artistName
        trackDuration.text = track.trackTime

        val coverUrl = if (track.artworkUrl100.isNotEmpty()) {
            track.getCoverArtwork()
        } else {
            null
        }

        Glide.with(itemView.context)
            .load(coverUrl)
            .placeholder(R.drawable.track_cover_placeholder)
            .centerCrop()
            .transform(RoundedCorners(10))
            .into(trackImage)
    }
}