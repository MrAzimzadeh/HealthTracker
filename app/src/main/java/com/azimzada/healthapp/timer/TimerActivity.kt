package com.azimzada.healthapp.timer

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.azimzada.healthapp.R
import com.azimzada.healthapp.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var countDownTimer: CountDownTimer
    private var isCountingDown = false
    private var remainingTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val progressAnimation = AnimationUtils.loadAnimation(this, R.anim.progress_animation)
        progressBar = findViewById(R.id.progress_bar)
        startButton = findViewById(R.id.start)
        resetButton = findViewById(R.id.reset)
        progressBar.startAnimation(progressAnimation)

        binding.heightNum.minValue = 0
        binding.heightNum.maxValue = 60
        binding.weighttNum.minValue = 0
        binding.weighttNum.maxValue = 60

        startButton.setOnClickListener {
            if (!isCountingDown) {
                startButton.visibility = View.INVISIBLE
                resetButton.visibility = View.VISIBLE


                binding.timer.visibility = View.GONE
                val layoutParams = binding.card.layoutParams as ConstraintLayout.LayoutParams
                val newMargin = resources.getDimensionPixelSize(R.dimen.m) // Yeni margin değeri
                layoutParams.setMargins(0, newMargin, 0, 0)
                binding.card.layoutParams = layoutParams
                val minutes = binding.heightNum.value
                val seconds = binding.weighttNum.value

                val countdownTime = (minutes * 60 + seconds).toLong() * 1000

                if (::countDownTimer.isInitialized) {
                    countDownTimer.cancel()
                }

                countDownTimer = object : CountDownTimer(countdownTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val remainingSeconds = (millisUntilFinished / 1000).toInt()
                        val totalDurationInMillis = countdownTime
                        val updateIntervalInMillis = 100
                        val progressMax = 100
                        val progress = (((totalDurationInMillis - millisUntilFinished) / totalDurationInMillis.toFloat()) * progressMax).toInt()
                        progressBar.progress = progress

                        val animator = ObjectAnimator.ofInt(progressBar, "progress", progress)

                        val displayMinutes = remainingSeconds / 60
                        val displaySeconds = remainingSeconds % 60
                        binding.textView7.text = "$displayMinutes : $displaySeconds"

                        remainingTime = millisUntilFinished
                    }

                    override fun onFinish() {
                        isCountingDown = false
                        startButton.visibility = View.VISIBLE
                        resetButton.visibility = View.VISIBLE
                        binding.timer.visibility = View.VISIBLE
                        progressBar.progress = 100
                        binding.textView7.text = "0 : 00"
                        val layoutParams = binding.card.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.setMargins(0, 5, 0, 0)
                        resetButton.visibility = View.GONE
                    }
                }

                countDownTimer.start()
                isCountingDown = true
            }
        }

        resetButton.setOnClickListener {
            countDownTimer.cancel()
            binding.timer.visibility = View.VISIBLE
            progressBar.progress = 0
            binding.textView7.text = "0 : 00"
            startButton.isEnabled = true
            isCountingDown = false
            val layoutParams = binding.card.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.setMargins(0, 5, 0, 0)
            resetButton.visibility = View.GONE
            startButton.visibility = View.VISIBLE
        }
    }
}
