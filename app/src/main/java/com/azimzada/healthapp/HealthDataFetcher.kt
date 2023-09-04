package com.azimzada.healthapp
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import java.util.concurrent.TimeUnit

class HealthDataFetcher(private val context: Context) {

    fun fetchStepCountData(startTimeMillis: Long, endTimeMillis: Long,
                           successCallback: (dataReadResponse: DataReadResponse) -> Unit,
                           failureCallback: (exception: Exception) -> Unit) {

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
            .build()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
            Fitness.getHistoryClient(context, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse ->
                    successCallback(dataReadResponse)
                }
                .addOnFailureListener { e ->
                    failureCallback(e)
                }
        } else {

        }
    }
}