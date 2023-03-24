package com.demo.sensor
import android.graphics.drawable.Drawable
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import android.media.MediaPlayer
import android.os.SystemClock
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var previousMagnitude: Float = 0.0F
    private lateinit var userActivityStart: Date
    private var userActivity: String = Constants.UNKNOWN
    private var userActivityList = ArrayList<String>(10)
   private lateinit var walkingImg: Drawable
    private lateinit var stillImg: Drawable
    private lateinit var runningImg: Drawable
    private lateinit var vehicleImg: Drawable
    private lateinit var unknownImg:Drawable
   private lateinit var textView1:TextView
    private lateinit var imageView:ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private var startt: Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.glory)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        val textView: TextView = findViewById(R.id.date1)
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        textView.text = currentDateAndTime
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
        setupSensor()
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
        mediaPlayer.stop()
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
            Log.d("delta", "$delta")
            if(delta < 0.6) {
                Log.d("act", "still")
                currentActivity = Constants.STILL
            } else if(delta >= 0.6 && delta < 4 ) {
                Log.d("act", "walk")
                currentActivity = Constants.WALKING
            } else if(delta >=4 && delta < 15) {
               Log.d("act", "run")
                currentActivity = Constants.RUNNING
            } else if (delta >=15 && delta < 25) {
                currentActivity = Constants.IN_VEHICLE

            }
            userActivityList.add(currentActivity)
            if(userActivityList.size > 10) {
                userActivityList.removeAt(0)
            }
//          predict the activity using majority classifier
            val majorityActivity = userActivityList.groupingBy { it }
                .eachCount().maxBy { it.value }.key
            if(majorityActivity != userActivity) {
                if(userActivity != Constants.UNKNOWN) {
//                  store the user activity in the database
                    Log.d("act", "updating the activity $userActivity")
                    getDifferenceAndStoreActivityRecord(currentDateTime,
                        userActivityStart, userActivity)
                }
//              reset the activity and start time for the majority activity
                userActivity = majorityActivity
                userActivityStart = currentDateTime
                updateUIforActivity(majorityActivity)

            } else if (userActivity == Constants.UNKNOWN) {
                userActivityStart = currentDateTime
            }
        }
    }
    private fun updateUIforActivity(majorityActivity: String) {
       /* val currentt= SystemClock.elapsedRealtime()
        val dur=(currentt-startt)/1000;*/
           if(majorityActivity== Constants.WALKING){

               textView1.text="Walking"
               imageView.setImageDrawable(walkingImg)
               Snackbar.make(findViewById(android.R.id.content), "You're doing great!", Snackbar.LENGTH_SHORT).show()
               //Toast.makeText(this, "You were still for $dur seconds", Toast.LENGTH_SHORT).show()
           } else if(majorityActivity== Constants.RUNNING){
               textView1.text="Running"
               imageView.setImageDrawable(runningImg)
           } else if(majorityActivity == Constants.STILL){
               textView1.text="Still"
               imageView.setImageDrawable(stillImg)
           }else if(majorityActivity == Constants.IN_VEHICLE){
               textView1.text="In Vehicle"
               imageView.setImageDrawable(vehicleImg)
           }else{
               textView1.text="Unknown"
               imageView.setImageDrawable(unknownImg)
           }
    }

    private fun getDifferenceAndStoreActivityRecord(start: Date, end: Date, activity: String) {
        val duration: Double = (end.time - start.time)/60.0
        Log.d("act", "$activity $end $duration")
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }




}
