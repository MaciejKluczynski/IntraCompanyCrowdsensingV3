package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast: BroadcastReceiver() {

    companion object{
        const val NOTIFICATION_TIMEOUT_MS:Long = 30000
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            //start result activity on click on notification
            val mainIntent = Intent(context, ResultActivity::class.java)
            mainIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val activityPendingIntent = PendingIntent.getActivity(context,0,mainIntent,PendingIntent.FLAG_ONE_SHOT)
            //start dismiss Activity as a result of dismissing notification
            val deleteIntent = Intent(context, DismissBroadcast::class.java)
            deleteIntent.action = "notification_cancelled"
            val onDismissPendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            createCloseEndedNotification(context,intent,activityPendingIntent,onDismissPendingIntent)
        }
    }
    //https://stackoverflow.com/questions/5399229/how-to-remove-the-notification-after-the-particular-time-in-android
    //timeout not working in api<26
    //https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder#setTimeoutAfter(long)
    private fun createCloseEndedNotification(context: Context,intent: Intent?,
                                             activityPendingIntent:PendingIntent,
                                             dismissPendingIntent:PendingIntent){
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
        builder.setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
        builder.setDeleteIntent(dismissPendingIntent)
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(1,builder.build())
    }
}