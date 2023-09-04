package com.azimzada.healthapp.services
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.azimzada.healthapp.preference.database.FitDatabase
//import com.azimzada.healthapp.room.model.FitData
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.fitness.Fitness
//import com.google.android.gms.fitness.FitnessOptions
//import com.google.android.gms.fitness.data.DataType
//import com.google.android.gms.fitness.data.Field
//import com.google.android.gms.fitness.request.DataReadRequest
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//import java.util.Calendar
//import java.util.concurrent.TimeUnit
//
//class FetchAndSaveDataWorker(
//    context: Context,
//    workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//        return try {
//            fetchData()
//            Result.success()
//        } catch (e: Exception) {
//            Log.e("FetchAndSaveDataWorker", "Error fetching and saving data: ${e.message}")
//            Result.failure()
//        }
//    }
//
//    private suspend fun fetchData() {
//        val fitnessOptions = FitnessOptions.builder()
//            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//            .build()
//
//        val googleSignInAccount =
//            GoogleSignIn.getAccountForExtension(applicationContext, fitnessOptions)
//
//        val currentTimeMillis = System.currentTimeMillis()
//        val startTimeMillis = currentTimeMillis - TimeUnit.DAYS.toMillis(1)
//        val endTimeMillis = currentTimeMillis
//
//        val readRequest = DataReadRequest.Builder()
//            .read(DataType.TYPE_STEP_COUNT_DELTA)
//            .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
//            .build()
//
//        val dataReadResponse = withContext(Dispatchers.IO) {
//            Fitness.getHistoryClient(applicationContext, googleSignInAccount)
//                .readData(readRequest)
//                .await()
//        }
//
//        val dataSet = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
//        val totalSteps = dataSet?.dataPoints?.sumBy { dataPoint ->
//            dataPoint.getValue(Field.FIELD_STEPS).asInt()
//        }
//
//        if (totalSteps != null) {
//            saveDataToDatabase(totalSteps, currentTimeMillis)
//        }
//    }
//    private suspend fun saveDataToDatabase(steps: Int, timestamp: Long) {
//        val database = FitDatabase.getInstance(applicationContext)
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = timestamp
//        val fitData = FitData(timestamp = timestamp, stepCount = steps, dateTime = calendar)
//        database.fitDataDao().insertFitData(fitData)
//    }
//}