package com.demo.sensor
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.widget.TextView
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransitionRequest
import com.vmadalin.easypermissions.EasyPermissions
import pub.devrel.easypermissions.AppSettingsDialog
import java.util.*

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var client: ActivityRecognitionClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView: TextView = findViewById(R.id.date1)
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        textView.text = currentDateAndTime
        client = ActivityRecognitionClient(this)
        println(ActivityTransitionUtil.hasActivityTransitionPermissions(this).toString())

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && !ActivityTransitionUtil.hasActivityTransitionPermissions(this)) {
            requestForActivityTransitionPermissions()

        } else {
            requestForUpdates()

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestForActivityTransitionPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "You need to allow activity transition permissions to use this app",
            Constants.ACTIVITY_TRANSITION_REQUEST_CODE,
            android.Manifest.permission.ACTIVITY_RECOGNITION,

        )
    }

    private fun requestForUpdates() {
        client
            .requestActivityTransitionUpdates(
                ActivityTransitionUtil.getActivityTransitionRequest(),
                getPendingIntent()
            )
            .addOnSuccessListener {
                println("successful registration")
            }
            .addOnFailureListener { e: Exception ->
                println(e.printStackTrace())
            }
    }

    private fun removeForUpdates() {
        client.removeActivityUpdates(getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
         return PendingIntent.getBroadcast(
             this,
             Constants.ACTIVITY_TRANSITION_RECIEVER_CODE,
             intent,
             PendingIntent.FLAG_MUTABLE
         )
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
       requestForUpdates()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeForUpdates()
    }
}
