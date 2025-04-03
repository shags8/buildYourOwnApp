package com.task.autozen.domain.interfaces

import com.task.autozen.data.local.savedlocations.SavedLocationEntity

interface ModeRepository {
    fun getSavedLocations(): List<SavedLocationEntity>
    fun saveLocation(location: SavedLocationEntity)
    fun deleteLocation(id: Int)
    fun getLocationById(id: Int): SavedLocationEntity?
    fun updateLocation(location: SavedLocationEntity)
}