package com.azimzada.healthapp.preference.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azimzada.healthapp.room.dao.FitDataDao
import com.azimzada.healthapp.room.model.FitData

@Database(entities = [FitData::class], version = 3, exportSchema = false)
abstract class FitDatabase : RoomDatabase() {
    abstract fun fitDataDao(): FitDataDao

    companion object {
        @Volatile
        private var INSTANCE: FitDatabase? = null

        fun getInstance(context: Context): FitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitDatabase::class.java,
                    "fit_database_v"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}