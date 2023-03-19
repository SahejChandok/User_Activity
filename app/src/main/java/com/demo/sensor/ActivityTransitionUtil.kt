package com.demo.sensor

import android.content.Context
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.vmadalin.easypermissions.EasyPermissions


object ActivityTransitionUtil {
    fun hasActivityTransitionPermissions(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
    private fun getTransitions(): MutableList<ActivityTransition> {
        val transitions: MutableList<ActivityTransition> = ArrayList()
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        return transitions
    }

    fun getActivityTransitionRequest() = ActivityTransitionRequest(getTransitions())

    fun stringifyActivity(activity: Int): String {
        return when(activity) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.IN_VEHICLE -> "IN VEHICLE"
            else -> "UNKNOWN"
        }
    }

    fun stringifyTransition(transitionType: Int): String {
        return when(transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            else -> "NO IDEA"
        }
    }
}