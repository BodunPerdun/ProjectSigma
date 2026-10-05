package com.example.projectsigma.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EventEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "social_map_database.db"
                    ).fallbackToDestructiveMigration().build()
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error initializing Room DB, falling back to in-memory: ${e.message}")
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java
                    ).fallbackToDestructiveMigration().build()
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
