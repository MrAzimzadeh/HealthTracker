package com.azimzada.healthapp.room.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "fit_database_v")
data class FitData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "stepCount") val stepCount: Int,
    @ColumnInfo(name = "activityDuration") val activityDuration: String,
    @ColumnInfo(name = "caloriesBurned") val caloriesBurned: Double,
    @ColumnInfo(name = "dateTime") val dateTime: String
)