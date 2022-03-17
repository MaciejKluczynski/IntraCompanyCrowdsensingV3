package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.MainActivity

class MyNotificationManager(val context: Context) {

    @SuppressLint("WrongConstant")
    fun createNotificationChannel(){
        //android O - Oreo Android 8.0
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.O){
            val name = MainActivity.CHANNEL_NAME
            val description = MainActivity.CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_MAX
            val notificationChannel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance)
            notificationChannel.description = description
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //cancelling notifications
    fun cancellNotification(){
        //cancell notification
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }
}