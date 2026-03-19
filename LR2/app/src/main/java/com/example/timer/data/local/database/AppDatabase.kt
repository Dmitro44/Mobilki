package com.example.timer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.timer.data.local.dao.TimerPhaseDao
import com.example.timer.data.local.dao.TimerSequenceDao
import com.example.timer.data.local.entity.TimerPhase
import com.example.timer.data.local.entity.TimerSequence

/**
 * Room database for the Timer application
 * 
 * Version 1: Initial schema with TimerSequence and TimerPhase entities
 * exportSchema = true enables schema export for migration testing
 */
@Database(
    entities = [
        TimerSequence::class,
        TimerPhase::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun timerSequenceDao(): TimerSequenceDao
    abstract fun timerPhaseDao(): TimerPhaseDao
    
    companion object {
        private const val DATABASE_NAME = "timer_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get singleton database instance
         * Uses double-check locking pattern for thread safety
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
    }
}
