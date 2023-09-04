package com.azimzada.healthapp.fit

import android.content.Context
import android.util.Log
import com.azimzada.healthapp.dtos.GoogleFitDataDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class GoogleFitManager(private val context: Context) {

    fun fetchTotalCaloriesBurned(
        startTimeMillis: Long,
        endTimeMillis: Long,
        onSuccess: (caloriesBurned: Double) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(context, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse ->
                    val dataSet = dataReadResponse.getDataSet(DataType.TYPE_CALORIES_EXPENDED)
                    val caloriesBurned = dataSet?.dataPoints?.sumByDouble { dataPoint ->
                        dataPoint.getValue(Field.FIELD_CALORIES).asFloat().toDouble()
                    } ?: 0.0
                    onSuccess(caloriesBurned)
                }
                .addOnFailureListener { e ->
                    onFailure(e.toString())
                }
        }
    }

    fun fetchActivityDuration(
        startTimeMillis: Long,
        endTimeMillis: Long,
        onSuccess: (activityDuration: Int) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(context, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse ->
                    val dataSet = dataReadResponse.getDataSet(DataType.TYPE_ACTIVITY_SEGMENT)
                    val activityDuration = dataSet.dataPoints.sumBy { dataPoint ->
                        val endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                        val startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                        (endTime - startTime).toInt()
                    }
                    onSuccess(activityDuration)
                }
                .addOnFailureListener { e ->
                    onFailure(e.toString())
                }
        }
    }

    fun fetchTotalSteps(
        onSuccess: (totalSteps: Int) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
//            val currentTimeMillis = System.currentTimeMillis()
//            val startTimeMillis = currentTimeMillis - TimeUnit.DAYS.toMillis(1)
//            val endTimeMillis = currentTimeMillis

            val currentTimeMillis = System.currentTimeMillis()

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.timeInMillis = currentTimeMillis

// Günün başlangıcını ayarlayın
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dayStartMillis = calendar.timeInMillis

// Günün bitişini ayarlayın
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)

            val dayEndMillis = calendar.timeInMillis

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(dayStartMillis, dayEndMillis, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(context, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse ->
                    val dataSet = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
                    val totalSteps = dataSet?.dataPoints?.sumBy { dataPoint ->
                        dataPoint.getValue(Field.FIELD_STEPS).asInt()
                    }
                    onSuccess(totalSteps ?: 0)
                }
                .addOnFailureListener { e ->
                    onFailure(e.toString())
                }
        }
    }

    fun fetchLast7DaysGoogleFitData() {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
            val currentTimeMillis = System.currentTimeMillis()

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.timeInMillis = currentTimeMillis

            // Son 7 günü hesapla
            val daysAgo = 7
            val startTimeMillis = currentTimeMillis - TimeUnit.DAYS.toMillis(daysAgo.toLong())
            val endTimeMillis = currentTimeMillis

            val googleFitDataList = mutableListOf<GoogleFitDataDTO>() // DTO listesi oluştur

            val readStepRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
                .build()

            val readActivityRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(context, googleSignInAccount)
                .readData(readStepRequest)
                .addOnSuccessListener { stepDataReadResponse ->
                    val stepDataSet =
                        stepDataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)

                    Fitness.getHistoryClient(context, googleSignInAccount)
                        .readData(readActivityRequest)
                        .addOnSuccessListener { activityDataReadResponse ->
                            val activityDataSet =
                                activityDataReadResponse.getDataSet(DataType.TYPE_ACTIVITY_SEGMENT)

                            // Her bir günün adım ve aktivite verisini hesapla ve DTO'ya ekle
                            for (i in 0 until daysAgo) {
                                val dayMillis = startTimeMillis + TimeUnit.DAYS.toMillis(i.toLong())
                                val daySteps = stepDataSet?.dataPoints?.sumBy { dataPoint ->
                                    if (dataPoint.getStartTime(TimeUnit.MILLISECONDS) >= dayMillis &&
                                        dataPoint.getEndTime(TimeUnit.MILLISECONDS) <= dayMillis + TimeUnit.DAYS.toMillis(
                                            1
                                        )
                                    ) {
                                        dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                    } else {
                                        0
                                    }
                                }
                                val dayActivity = activityDataSet?.dataPoints?.sumBy { dataPoint ->
                                    if (dataPoint.getStartTime(TimeUnit.MILLISECONDS) >= dayMillis &&
                                        dataPoint.getEndTime(TimeUnit.MILLISECONDS) <= dayMillis + TimeUnit.DAYS.toMillis(
                                            1
                                        )
                                    ) {
                                        1 // Örnek olarak her aktiviteyi 1 olarak sayalım
                                    } else {
                                        0
                                    }
                                }
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateString = dateFormat.format(dayMillis)
                                val googleFitDataDTO = GoogleFitDataDTO(
                                    day = i + 1,
                                    date = dateString,
                                    steps = daySteps ?: 0,
                                    calories = 0f, // Bu kısmı doldurmanız gerekiyor
                                    activity = dayActivity ?: 0
                                )
                                googleFitDataList.add(googleFitDataDTO) // DTO'ya veriyi ekle
                            }

                            // Elde edilen verileri burada kullanabilirsiniz (googleFitDataList)
                        }
                        .addOnFailureListener { e ->
                            Log.e("GoogleFitError", e.toString())
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleFitError", e.toString())
                }
        }
    }

}