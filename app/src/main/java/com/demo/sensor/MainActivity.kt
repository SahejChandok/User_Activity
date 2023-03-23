package com.demo.sensor
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.widget.TextView
import android.widget.ImageView
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityRecognition
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private lateinit var googleApiClient: GoogleApiClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        val textView: TextView = findViewById(R.id.date1)
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        textView.text = currentDateAndTime
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        googleApiClient.disconnect()
        super.onStop()
    }

    override fun onConnected(bundle: Bundle?) {
        val activityRecognitionRequest = ActivityRecognitionRequest.create()
        val activityRecognitionPendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, ActivityRecognitionService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
            googleApiClient,
            0,
            activityRecognitionPendingIntent
        )
    }

    override fun onConnectionSuspended(i: Int) {
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
    }


}