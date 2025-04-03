package com.task.autozen.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.task.autozen.data.local.savedlocations.SavedLocationDao
import com.task.autozen.data.local.savedlocations.SavedLocationEntity

@Database(entities = [SavedLocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mode_switch_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}