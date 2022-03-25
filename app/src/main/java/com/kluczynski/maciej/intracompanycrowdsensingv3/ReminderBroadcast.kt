package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.MyNotificationManager

class ReminderBroadcast: BroadcastReceiver() {

    companion object{
        const val NOTIFICATION_TIMEOUT_MS:Long = 30000
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val myNotificationManager = MyNotificationManager(it)
            myNotificationManager.createCloseEndedNotification(it,intent)
        }
    }


}