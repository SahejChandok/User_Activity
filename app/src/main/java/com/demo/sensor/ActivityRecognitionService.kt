package com.demo.sensor

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService : IntentService("ActivityRecognitionService") {

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val activityRecognitionResult = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity = activityRecognitionResult.mostProbableActivity.type
            displayActivity(mostProbableActivity)
        }
    }

    private fun displayActivity(activityType: Int) {
        val activityImageView: ImageView = findViewById(R.id.image)
        val activityTextView: TextView = findViewById(R.id.image_text)
        when (activityType) {
            DetectedActivity.STILL -> {
                activityImageView.setImageResource(R.drawable.still)
                activityTextView.text = "You are still"
            }
            DetectedActivity.ON_FOOT -> {
                activityImageView.setImageResource(R.drawable.walking)
                activityTextView.text = "You are walking"
            }
            DetectedActivity.RUNNING -> {
                activityImageView.setImageResource(R.drawable.running)
                activityTextView.text = "You are running"
            }
            DetectedActivity.IN_VEHICLE -> {
                activityImageView.setImageResource(R.drawable.vehicle)
                activityTextView.text = "You are in a vehicle"
            }
        }
    }

}