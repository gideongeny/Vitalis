package com.gideongeng.Vitalis.Boardcast

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.Utils.Constant

class Step_reset_BoardCast : BroadcastReceiver() {
    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, p1: Intent?) {
        val totalSteps = Constant.loadData(context!!, "step_count", "total_step", "0")!!.toInt()
        
        // Reset Day Start
        Constant.savedata(context, "step_count", "previous_step", totalSteps.toString())
        
        // Reset accumulated reboot steps for the new day
        Constant.savedata(context, "step_count", "resets", "0")
        Constant.savedata(context, "step_count", "last_sensor", totalSteps.toString())

        Log.d("StepReset", "Counters reset successfully for new day")
        
        val target = Constant.loadData(context, "myPrefs", "target", "1000").toString()
        val notification = NotificationCompat.Builder(context, "Stepcount")
            .setContentTitle("New day! Tracking steps...")
            .setContentText("Current Steps : 0  Target Steps: $target")
            .setSmallIcon(R.drawable.mainlogo)
            .setOngoing(true).setSilent(true)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())
    }
}
