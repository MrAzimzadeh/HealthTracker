package com.azimzada.healthapp.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azimzada.healthapp.room.model.FitData

@Dao
interface FitDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFitData(fitData: FitData)

    @Query("SELECT * FROM fit_database_v")
    fun getAllFitData(): List<FitData>


    @Query("SELECT * FROM fit_database_v WHERE dateTime = :dateTime AND stepCount = :stepCount")
    fun getFitDataByDateTimeAndSteps(dateTime: String, stepCount: Int): FitData?
}