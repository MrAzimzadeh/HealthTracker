package com.azimzada.healthapp


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azimzada.healthapp.adapter.GoogleFitDataAdapter
import com.azimzada.healthapp.adapter.ItemAdapter
import com.azimzada.healthapp.dtos.GoogleFitDataDTO
import com.azimzada.healthapp.fit.GoogleFitManager
import com.azimzada.healthapp.helpers.photo.CircleTransformation
import com.azimzada.healthapp.helpers.photoimport.RoundedImageView
import com.azimzada.healthapp.notification.AlarmReceiver
import com.azimzada.healthapp.preference.SavedPreference
import com.azimzada.healthapp.preference.database.FitDatabase
import com.azimzada.healthapp.room.model.FitData
import com.azimzada.healthapp.timer.TimerActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private var timeInMillis: Long = 0
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 1001
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1002
    private val googleFitManager by lazy {
        GoogleFitManager(this)
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        createNotificationChannel(this)
        scheduleNotification()
        fetchAndSaveFitData()
        fetchTotalStepsFromGoogleFit()

        findViewById<Button>(R.id.training).setOnClickListener()
        {
            var intent = Intent(this@DashboardActivity , TimerActivity::class.java)
            startActivity(intent)
        }


//        logAllFitData()
        fetchActivityDuration()
        fetchLast7DaysGoogleFitData()

        retrieveFitDataFromDatabase()

        val greetingTextView: TextView = findViewById(R.id.fullName)
        val profileImageView: RoundedImageView = findViewById(R.id.roundedImageView)
        val username = SavedPreference.getUsername(this)
        val profileImageURL = auth.currentUser?.photoUrl
        updateUserNameAndProfileImage(username, profileImageURL)
        val currentTimeMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        this.timeInMillis = currentTimeMillis

        calendar.run {
            timeInMillis = currentTimeMillis

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayStartMillis = calendar.timeInMillis


        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val dayEndMillis = calendar.timeInMillis


        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else {
            if (googleSignInAccount != null) {
                checkAndRequestPermissions()

                val readRequest = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(dayStartMillis, dayEndMillis, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(this, googleSignInAccount)
                    .readData(readRequest)
                    .addOnSuccessListener { dataReadResponse ->
                        val dataSet = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
                        val totalSteps = dataSet?.dataPoints?.sumBy { dataPoint ->
                            dataPoint.getValue(Field.FIELD_STEPS).asInt()
                        }
                        updateTotalSteps(totalSteps ?: 0)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Numm", e.toString())
                    }
            }
        }
    }


    private fun retrieveFitDataFromDatabase() {
        CoroutineScope(Dispatchers.Main).launch {
            val itemList: List<FitData> = withContext(Dispatchers.IO) {
                val fitDatabase = FitDatabase.getInstance(this@DashboardActivity)
                fitDatabase.fitDataDao().getAllFitData()
            }
            val reversedItemList = itemList.reversed()
            val recyclerView: RecyclerView = findViewById(R.id.recycle)
            val layoutManager = LinearLayoutManager(this@DashboardActivity)
            recyclerView.layoutManager = layoutManager
            val adapter = ItemAdapter(reversedItemList)
            recyclerView.adapter = adapter
        }
    }

    private fun fetchTotalStepsFromGoogleFit() {
        googleFitManager.fetchTotalSteps(
            onSuccess = { totalSteps ->
                updateTotalSteps(totalSteps)
            },
            onFailure = { error ->
                Log.e("Error", error)
            }
        )
    }

    private fun checkAndRequestPermissions() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .build()

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        ) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        } else {
            fetchData()
        }
    }

    private fun fetchData() {
        val currentTimeMillis = System.currentTimeMillis()
        val startTimeMillis = currentTimeMillis - TimeUnit.DAYS.toMillis(1)
        val endTimeMillis = currentTimeMillis

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(startTimeMillis, endTimeMillis, TimeUnit.MILLISECONDS)
            .build()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (googleSignInAccount != null) {
            val googleFitManager = GoogleFitManager(this)

            googleFitManager.fetchTotalSteps(
                onSuccess = { totalSteps ->
                    updateTotalSteps(totalSteps)
                },
                onFailure = { error ->
                    Log.e("Error", error)
                }
            )
        }
    }


    // Berabr olunan yer = >
    @SuppressLint("SetTextI18n")
    private fun updateTotalSteps(totalSteps: Int) {
        Log.e("Numm", totalSteps.toString())
        val testDataTextView: TextView = findViewById(R.id.textView5)
        var progress: ProgressBar = findViewById(R.id.progress_bar)
        testDataTextView.text = totalSteps.toString()
        progress.progress = 88 //
        updateProgress(totalSteps, 24);
    }

    private fun updateProgress(totalSteps: Int, activityDurationMillis: Int = 24) {
        val progress: ProgressBar = findViewById(R.id.progress_bar)
        val dailyStepGoal = 10000
        val maxProgress = dailyStepGoal
        val stepProgress = (totalSteps.toFloat() / maxProgress * 100).toInt()
        val activityDurationGoalMillis = TimeUnit.HOURS.toMillis(8)
        val activityProgress =
            (activityDurationMillis.toFloat() / activityDurationGoalMillis * 100).toInt()
        val overallProgress = (stepProgress + activityProgress) / 2

        // Calculate calories burned based on the total steps (this is a simplified example)
        val caloriesBurned = totalSteps * 0.05 // Adjust the factor based on your calculation

        progress.progress = overallProgress

        // Calculate percentage of calories burned compared to a maximum calorie goal
        val maxCaloriesGoal = 500 // Adjust the maximum calorie goal as needed
        val caloriesProgress = (caloriesBurned / maxCaloriesGoal * 100).toInt()

        // Update the UI elements to display the calculated values
        val caloriesTextView: TextView = findViewById(R.id.colori)
        val progressColori: ProgressBar = findViewById(R.id.progress_kolori)
        caloriesTextView.text = "${caloriesBurned.toInt()}"
        progressColori.progress = caloriesProgress
    }


    private fun updateUserNameAndProfileImage(username: String?, profileImageURL: Uri?) {
        val greetingTextView: TextView = findViewById(R.id.fullName)
        val profileImageView: RoundedImageView = findViewById(R.id.roundedImageView)
        Log.e("Numm", username.toString())
        if (username != null) {
            greetingTextView.text = "Hello, $username!"
        }

        if (profileImageURL != null) {
            Picasso.get()
                .load(profileImageURL)
                .transform(CircleTransformation())
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.progressavatar)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "step_id"
            val channelName = "My Step"
            val channelDescription = "Channel for my app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        calendar.set(Calendar.MINUTE, 52)
        calendar.set(Calendar.SECOND, 50)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val requestCode = 123
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun fetchAndSaveFitData() {
        val currentTimeMillis = System.currentTimeMillis()
        val dateTimeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(currentTimeMillis)

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

        googleFitManager.fetchTotalSteps(
            onSuccess = { totalSteps ->
                googleFitManager.fetchActivityDuration(
                    startTimeMillis = dayStartMillis,
                    endTimeMillis = dayEndMillis,
                    onSuccess = { activityDuration ->
                        googleFitManager.fetchTotalCaloriesBurned(
                            startTimeMillis = dayStartMillis,
                            endTimeMillis = dayEndMillis,
                            onSuccess = { caloriesBurned ->
                                val updatedActivityDuration = updateActivityDuration(activityDuration)
                                val fitData = FitData(
                                    timestamp = currentTimeMillis,
                                    stepCount = totalSteps ?: 0,
                                    activityDuration = updatedActivityDuration,
                                    caloriesBurned = caloriesBurned,
                                    dateTime = dateTimeString
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    val fitDatabase = FitDatabase.getInstance(this@DashboardActivity)
                                    fitDatabase.fitDataDao().insertFitData(fitData)
                                    Log.d("FitDataInserted", "FitData inserted successfully")
                                }
                            },
                            onFailure = { error ->
                                Log.e("GoogleFitError", error)
                            }
                        )
                    },
                    onFailure = { error ->
                        Log.e("GoogleFitError", error)
                    }
                )
            },
            onFailure = { error ->
                Log.e("GoogleFitError", error)
            }
        )
    }

    private fun fetchActivityDuration() {
        val currentTimeMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayStartMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val dayEndMillis = calendar.timeInMillis

        googleFitManager.fetchActivityDuration(
            startTimeMillis = dayStartMillis,
            endTimeMillis = dayEndMillis,
            onSuccess = { activityDuration ->
                updateActivityDuration(activityDuration)
            },
            onFailure = { error ->
                Log.e("Error", error)
            }
        )
    }


    @SuppressLint("SetTextI18n", "CutPasteId")
    private fun updateActivityDuration(activityDurationMillis: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(activityDurationMillis.toLong())
        val hours = TimeUnit.MINUTES.toHours(minutes)
        val activity: TextView = findViewById(R.id.activ)
        activity.text = "$hours : $minutes"
        val totalDurationMillis = TimeUnit.HOURS.toMillis(5)
        val progress = ((activityDurationMillis.toFloat() / totalDurationMillis) * 100).toInt()
        val prog: ProgressBar = findViewById(R.id.progress_activ)
        prog.progress = progress
        Log.e("Numm", "Activity Duration: $hours hours $minutes minutes")
        return "$hours : $minutes"
    }

    private fun fetchLast7DaysGoogleFitData() {
        googleFitManager.fetchLast7DaysGoogleFitData()
    }

    private fun retrieveAndBindGoogleFitDataToRecyclerView(googleFitDataList: List<GoogleFitDataDTO>) {
        val recyclerView: RecyclerView = findViewById(R.id.recycle)
        val layoutManager = LinearLayoutManager(this@DashboardActivity)
        recyclerView.layoutManager = layoutManager
        val adapter = GoogleFitDataAdapter(googleFitDataList)
        recyclerView.adapter = adapter
    }
}