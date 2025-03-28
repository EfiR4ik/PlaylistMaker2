package com.example.playlistmaker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteTracksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: FavoriteTrackEntity)

    @Delete
    suspend fun delete(track: FavoriteTrackEntity)

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun getById(trackId: Int): FavoriteTrackEntity?

    @Query("SELECT * FROM favorite_tracks")
    suspend fun getAll(): List<FavoriteTrackEntity>
}