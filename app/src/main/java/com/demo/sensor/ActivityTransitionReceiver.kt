package com.demo.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import java.text.SimpleDateFormat
import java.util.*


class ActivityTransitionReceiver: BroadcastReceiver() {
    private lateinit var start: Date
    private lateinit var end: Date
    private lateinit var activity: String
    override fun onReceive(context: Context?, intent: Intent?) {
        val result = ActivityTransitionResult.extractResult(intent)
        result?.let {
            result.transitionEvents.forEach { event ->
                val currentActivity = ActivityTransitionUtil.stringifyActivity(event.activityType)
                val info =
                    "Transition: " + currentActivity +
                            " (" + ActivityTransitionUtil.stringifyTransition(event.transitionType) + ")" + "   " +
                            SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                println(info)
                when(event.transitionType) {
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                        activity = currentActivity
                        start = Date()
                    }
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                        end = Date()
                        if(activity == currentActivity) {
                            // insert data into the database
                        }
                    }


                }
                Toast.makeText(context, info, Toast.LENGTH_LONG).show()

            }
        }
    }
}