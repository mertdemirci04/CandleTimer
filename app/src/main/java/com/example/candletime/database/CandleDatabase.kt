package com.example.candletime.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.candletime.models.CandleSession

@Database(
    entities = [CandleSession::class],
    version = 1,
    exportSchema = false
)
abstract class CandleDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: CandleDatabase? = null

        fun getInstance(context: Context): CandleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CandleDatabase::class.java,
                    "candle_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}