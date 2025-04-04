package com.task.autozen.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.task.autozen.data.local.savedlocations.SavedLocationEntity
import com.task.autozen.domain.interfaces.ModeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModeViewModel(private val repository: ModeRepository) : ViewModel() {

    private val _savedLocations = MutableLiveData<List<SavedLocationEntity>>()
    val savedLocations: LiveData<List<SavedLocationEntity>> get() = _savedLocations
    private val _selectedLocation = MutableLiveData<SavedLocationEntity?>()
    val selectedLocation: LiveData<SavedLocationEntity?> get() = _selectedLocation

    fun loadSavedLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val locations = repository.getSavedLocations()
            _savedLocations.postValue(locations)
        }
    }

    fun saveLocation(location: SavedLocationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val allLocations = repository.getSavedLocations()
            val existingLocation = allLocations.find { it.name == location.name }

            if (existingLocation != null) {
                val updatedLocation = location.copy(id = existingLocation.id)
                repository.updateLocation(updatedLocation)
            } else {
                repository.saveLocation(location)
            }

            loadSavedLocations()
        }
    }


    fun deleteLocation(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLocation(id)
            loadSavedLocations() // Refresh list
        }
    }

    fun fetchLocationById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val location = repository.getLocationById(id)
            _selectedLocation.postValue(location)
        }
    }

    fun setManualLocation(lat: Double, lon: Double) {
        _selectedLocation.value = SavedLocationEntity(
            name = "Manual Selection",
            latitude = lat,
            longitude = lon,
            radius = 5f,
            mode = 0
        )
    }

}


class ModeViewModelFactory(private val repository: ModeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModeViewModel::class.java)) {
            return ModeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
