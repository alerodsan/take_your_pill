package com.example.takeyourpill.utils


import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.takeyourpill.R
import com.example.takeyourpill.activities.MainActivity
import com.example.takeyourpill.activities.TimeActivity

class AlarmReceiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

       val notificationManager =ContextCompat.getSystemService(context,NotificationManager::class.java) as NotificationManager



            val notification = NotificationCompat.Builder(context, "Alarma").
            setSmallIcon(R.drawable.ic_add_pill_24dp).
            setContentTitle(intent!!.getStringExtra("title")).
            setContentText(intent!!.getStringExtra("text")).
            setTicker("ticker").
            setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), notification.build())



    }
}