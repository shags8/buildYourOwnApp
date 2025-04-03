package com.task.autozen.data.local.savedlocations

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val mode: Int
)