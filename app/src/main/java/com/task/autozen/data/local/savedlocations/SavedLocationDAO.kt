package com.task.autozen.data.local.savedlocations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface SavedLocationDao {

    @Query("SELECT * FROM saved_locations")
    fun getAllLocations(): List<SavedLocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    fun deleteLocation(id: Int)

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    fun getLocationById(id: Int): SavedLocationEntity?

    @androidx.room.Update
    fun updateLocation(location: SavedLocationEntity)
}