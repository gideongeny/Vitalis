package com.gideongeng.Vitalis.Service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.Utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Math.abs
import java.time.LocalDate
import java.util.*

class MyService : Service(), SensorEventListener {
    private var running = false
    private var totalsteps = 0
    private var step: String? = "400"
    private lateinit var sensorManager: SensorManager
    private lateinit var steocounterListener: SensorEventListener
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun start() {
        try {
            step_count()
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
        val stepString = Constant.loadData(this, "step_count", "total_step", "0") ?: "0"
        val previousStepString = Constant.loadData(this, "step_count", "previous_step", "0") ?: "0"
        val totalStepsResets = Constant.loadData(this, "step_count", "resets", "0") ?: "0"
        
        val total_s = stepString.toInt()
        val prev_s = previousStepString.toInt()
        val resets_acc = totalStepsResets.toInt()

        val c_step = abs(total_s - prev_s) + resets_acc
        val target = Constant.loadData(this, "myPrefs", "target", "1000").toString()
        if (Constant.isInternetOn(applicationContext)) {
            dataupload(c_step.toString(), LocalDate.now().toString())
        }

        notification = NotificationCompat.Builder(this, "Stepcount")
            .setContentTitle("Tracking steps...")
            .setContentText("Current Steps : $c_step  Target Steps:$target")
            .setSmallIcon(R.drawable.mainlogo)
            .setOngoing(true)
            .setSilent(true)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    private fun step_count() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepsensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepsensor == null) {
            Toast.makeText(this, "sensor not working", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, stepsensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        val total_steps = event!!.values[0].toInt()
        
        // Load persistent data
        var previoustotalstep = Constant.loadData(this, "step_count", "previous_step", "0")!!.toInt()
        var lastSensorValue = Constant.loadData(this, "step_count", "last_sensor", "0")!!.toInt()
        var resets = Constant.loadData(this, "step_count", "resets", "0")!!.toInt()

        // Initialization: If this is the very first time tracking, set the current sensor value as the baseline
        if (previoustotalstep == 0) {
            previoustotalstep = total_steps
            Constant.savedata(this, "step_count", "previous_step", previoustotalstep.toString())
        }

        // Detect Reboot/Sensor Reset
        if (total_steps < lastSensorValue) {
            // Sensor reset occurred (reboot). Add the last known value to resets.
            resets += lastSensorValue
            Constant.savedata(this, "step_count", "resets", resets.toString())
        }

        // Save current sensor value for next change detection
        Constant.savedata(this, "step_count", "last_sensor", total_steps.toString())
        Constant.savedata(this, "step_count", "total_step", total_steps.toString())

        // Calculate current steps: (Current - DayStart) + Accumulated from reboots
        var currsteps = (total_steps - previoustotalstep) + resets
        if (currsteps < 0) currsteps = total_steps // Fallback for edge cases

        // Dynamic Target Logic: Increment target by 10k when reached
        var targetValue = Constant.loadData(this, "myPrefs", "target", "1000")!!.toInt()
        if (currsteps >= targetValue) {
            targetValue += 10000
            Constant.savedata(this, "myPrefs", "target", targetValue.toString())
        }
        val target = targetValue.toString()

        val curr_date = LocalDate.now().toString()
        
        if (Constant.isInternetOn(applicationContext)) {
            dataupload(currsteps.toString(), curr_date)
        }
        
        // Update Notification for every single step
        notificationManager.notify(1,
            notification.setContentText("Current Steps : $currsteps  Target Steps: $target")
                .build()
        )
        
        // Send broadcast to update UI in real-time
        val intent = Intent("com.gideongeng.Vitalis.STEP_UPDATE")
        intent.putExtra("steps", currsteps)
        intent.putExtra("target", target)
        sendBroadcast(intent)

        startForeground(1, notification.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dataupload(currsteps: String, curr_date: String) {
        val steps = hashMapOf(
            "steps" to currsteps,
            "date" to curr_date
        )
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("user").document(user.uid)
                .collection("steps").document(curr_date).set(steps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for accuracy changes
    }
}
