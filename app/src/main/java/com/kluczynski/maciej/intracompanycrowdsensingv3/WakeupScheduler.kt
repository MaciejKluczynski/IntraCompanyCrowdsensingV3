package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class WakeupScheduler {
    fun scheduleWakeup(context: Context,hour:Int,minute:Int,second:Int){
        val am = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val futureDate = Calendar.getInstance()
        futureDate.timeInMillis = System.currentTimeMillis()
        futureDate.set(Calendar.HOUR_OF_DAY,hour)
        futureDate.set(Calendar.MINUTE,minute)
        futureDate.set(Calendar.SECOND,second)
        val intent = Intent(context, WakeupReceiver::class.java)
        Log.d("TIME",futureDate.timeInMillis.toString())
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        am.set(AlarmManager.RTC_WAKEUP, futureDate.timeInMillis, sender)
    }
}