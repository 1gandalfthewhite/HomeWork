package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Participant::class], version = 1, exportSchema = false)
abstract class ConferenceDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao
    
    companion object {
        @Volatile
        private var INSTANCE: ConferenceDatabase? = null
        
        fun getDatabase(context: Context): ConferenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConferenceDatabase::class.java,
                    "conference_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
