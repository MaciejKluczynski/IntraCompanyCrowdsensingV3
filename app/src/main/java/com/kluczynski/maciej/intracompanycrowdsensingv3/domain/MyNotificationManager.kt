package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kluczynski.maciej.intracompanycrowdsensingv3.*

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

    private fun createActivityPendingIntent(context: Context) : PendingIntent?{
        //start result activity on click on notification
        val mainIntent = Intent(context, ResultActivity::class.java)
        mainIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(context,0,mainIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun createDismissPendingIntent(context: Context): PendingIntent? {
        //start dismiss Broadcast as a result of dismissing notification
        val deleteIntent = Intent(context, DismissBroadcast::class.java)
        deleteIntent.action = "notification_cancelled"
        return PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    //https://stackoverflow.com/questions/5399229/how-to-remove-the-notification-after-the-particular-time-in-android
    //timeout not working in api<26
    //https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder#setTimeoutAfter(long)
    fun createCloseEndedNotification(context: Context,intent: Intent?){
        val activityPendingIntent = createActivityPendingIntent(context)
        val dismissPendingIntent = createDismissPendingIntent(context)
        //create notification
        val builder = NotificationCompat.Builder(context, "ICC")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Intra Company Crowdsensing Examination")
        builder.setContentText(intent!!.getStringExtra("Question"))
        builder.priority = NotificationCompat.PRIORITY_MAX
        //dismiss on tap
        builder.setAutoCancel(true)
        //on notification click action
        builder.setContentIntent(activityPendingIntent)
        // set Timeout After - anuluje notyfikacje po zadanej liczbie milisekund
        builder.setTimeoutAfter(ReminderBroadcast.NOTIFICATION_TIMEOUT_MS)
        builder.setDeleteIntent(dismissPendingIntent)
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(1,builder.build())
    }
}