package com.task.autozen.domain.repository

import com.task.autozen.data.local.savedlocations.SavedLocationDao
import com.task.autozen.data.local.savedlocations.SavedLocationEntity
import com.task.autozen.domain.interfaces.ModeRepository

class ModeRepositoryImpl(private val dao: SavedLocationDao) : ModeRepository {

    override fun getSavedLocations(): List<SavedLocationEntity> {
        return dao.getAllLocations()
    }

    override fun saveLocation(location: SavedLocationEntity) {
        dao.insertLocation(location)
    }

    override fun deleteLocation(id: Int) {
        dao.deleteLocation(id)
    }

    override fun getLocationById(id: Int): SavedLocationEntity? {
        return dao.getLocationById(id)
    }

    override fun updateLocation(location: SavedLocationEntity) {
        dao.updateLocation(location)
    }
}