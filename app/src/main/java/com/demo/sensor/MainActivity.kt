package com.demo.sensor
import android.graphics.drawable.Drawable
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import android.media.MediaPlayer
import android.opengl.Visibility
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text
import java.time.Period
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var appDatabase: AppDatabase
    private lateinit var sensorManager: SensorManager
    private var previousMagnitude: Float = 0.0F
    private lateinit var userActivityStart: Date
    private var userActivity: String = Constants.UNKNOWN
    private var userActivityList = ArrayList<String>(15)
   private lateinit var walkingImg: Drawable
    private lateinit var stillImg: Drawable
    private lateinit var runningImg: Drawable
    private lateinit var vehicleImg: Drawable
    private lateinit var unknownImg:Drawable
   private lateinit var textView1:TextView
    private lateinit var imageView:ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var motivationalQuoteText: TextView
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDatabase = AppDatabase.getDatabase(this)
        setContentView(R.layout.activity_main)
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.glory)
        mediaPlayer.isLooping = true
        motivationalQuoteText = findViewById(R.id.motivation_quote)
        motivationalQuoteText.text = "Opportunities don't happen, you create them."
        motivationalQuoteText.visibility = TextView.INVISIBLE
        val textView: TextView = findViewById(R.id.date1)
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        textView.text = currentDateAndTime
        timer = timer(period = 1000) {
            runOnUiThread {
                val currentTime = SimpleDateFormat("yyyy.MM.dd 'at' hh:mm:ss a", Locale.getDefault()).format(Date())
                textView.text = currentTime
            }
        }
         textView1= findViewById(R.id.textView2)
        imageView = findViewById(R.id.imageView2)
        walkingImg=resources.getDrawable(R.drawable.walking, null)
        stillImg=resources.getDrawable(R.drawable.still, null)
        runningImg=resources.getDrawable(R.drawable.running, null)
        vehicleImg=resources.getDrawable(R.drawable.vehicle, null)
        unknownImg=resources.getDrawable(R.drawable.unknown, null)
    }
    override fun onStart() {
        super.onStart()
        resetData()
        textView1.text="Unknown"
        imageView.setImageDrawable(unknownImg)
        motivationalQuoteText.visibility = TextView.INVISIBLE
        setupSensor()
    }

    private fun resetData() {
        userActivity = Constants.UNKNOWN
        userActivityStart = Date()
        userActivityList.clear()
    }

    private fun setupSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val  accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
        stopMediaPlayer()
    }
    private fun stopMediaPlayer() {
        if(mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentDateTime = Date()
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.
            var currentActivity = Constants.UNKNOWN
            val alpha = 0.8f
            val gravity = floatArrayOf(SensorManager.GRAVITY_EARTH,
                SensorManager.GRAVITY_EARTH, SensorManager.GRAVITY_EARTH)
            val linearAcceleration = FloatArray(3)

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Remove the gravity contribution with the high-pass filter.
            linearAcceleration[0] = event.values[0] - gravity[0]
            linearAcceleration[1] = event.values[1] - gravity[1]
            linearAcceleration[2] = event.values[2] - gravity[2]
            val magnitude: Float = sqrt(
                linearAcceleration[0].pow(2)
                        + linearAcceleration[1].pow(2)
            )
            val delta: Float = abs( magnitude - previousMagnitude)

            previousMagnitude = magnitude
            if(delta < 0.20) {
                currentActivity = Constants.STILL
            } else if(delta >= 0.20 && delta < 1.5 ) {
                currentActivity = Constants.WALKING
            } else if(delta >=1.5 && delta < 4) {
                currentActivity = Constants.RUNNING
            } else if (delta >=4 && delta < 8) {
                currentActivity = Constants.IN_VEHICLE
            }
            userActivityList.add(currentActivity)
            if(userActivityList.size > 15) {
                userActivityList.removeAt(0)
            }
//          predict the activity using majority classifier
            val majorityActivity = userActivityList.groupingBy { it }
                .eachCount().maxBy { it.value }.key
            if(majorityActivity != userActivity) {
                if(userActivity != Constants.UNKNOWN) {
//                  store the user activity in the database
                    Log.d("act", "updating the activity $userActivity")
                    getDifferenceAndStoreActivityRecord(this, userActivityStart,
                        currentDateTime, userActivity)
                    updateUIforActivity(majorityActivity)
                }
//              reset the activity and start time for the majority activity
                userActivity = majorityActivity
                userActivityStart = currentDateTime


            } else if (userActivity == Constants.UNKNOWN) {
                userActivityStart = currentDateTime
            }
        }
    }
    private fun updateUIforActivity(majorityActivity: String) {
           if(majorityActivity== Constants.WALKING){
               stopMediaPlayer()
               motivationalQuoteText.visibility = TextView.VISIBLE
               textView1.text="Walking"
               imageView.setImageDrawable(walkingImg)

               //Toast.makeText(this, "You were still for $dur seconds", Toast.LENGTH_SHORT).show()
           } else if(majorityActivity== Constants.RUNNING){
               mediaPlayer.start()
               motivationalQuoteText.visibility = TextView.INVISIBLE
               textView1.text="Running"
               imageView.setImageDrawable(runningImg)
           } else if(majorityActivity == Constants.STILL){
               stopMediaPlayer()
               motivationalQuoteText.visibility = TextView.INVISIBLE
               textView1.text="Still"
               imageView.setImageDrawable(stillImg)
           }else if(majorityActivity == Constants.IN_VEHICLE){
               stopMediaPlayer()
               motivationalQuoteText.visibility = TextView.INVISIBLE
               textView1.text="In Vehicle"
               imageView.setImageDrawable(vehicleImg)
           }else{
               stopMediaPlayer()
               motivationalQuoteText.visibility = TextView.INVISIBLE
               textView1.text="Unknown"
               imageView.setImageDrawable(unknownImg)
           }
    }

    private fun getDifferenceAndStoreActivityRecord(context: Context, start: Date, end: Date, activity: String) {
        var duration: Double = (end.time - start.time)/(60.0 * 1000)
        Log.d("act", "logging $activity $start $end $duration")
        val durationToBeSaved = if (duration < 1) 0.0 else duration
        Log.d("act", "duration to be saved $durationToBeSaved")
        val activityLog = UserActivityLog(null, durationToBeSaved, start.toString())
        var toastMessage = "You were ${activity.lowercase()} for "
        val durationInMinutes = duration.toInt()
        val durationInSeconds = ((duration - durationInMinutes) * 60).toInt()
        if(durationInMinutes == 0 && durationInSeconds > 0) {
            toastMessage += "$durationInSeconds seconds"
        } else if(durationInMinutes > 0 && durationInSeconds == 0){
            toastMessage += "$durationInMinutes minutes"
        } else if(durationInMinutes > 0 && durationInSeconds > 0) {
            toastMessage += "$durationInMinutes minutes, $durationInSeconds seconds"
        } else {
            toastMessage = ""
        }
        Log.d("act", "toast: $toastMessage")
        if(toastMessage != "") {
            GlobalScope.launch(Dispatchers.IO) {
                appDatabase.userActivityLogDao().insert(activityLog)
            }
            Snackbar.make(findViewById(R.id.content), toastMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }




}
