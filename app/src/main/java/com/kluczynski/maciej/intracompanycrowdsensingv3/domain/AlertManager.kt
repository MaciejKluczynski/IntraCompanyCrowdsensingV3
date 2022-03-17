package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.ReminderBroadcast
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel

class AlertManager(var context: AppCompatActivity, var dateManager: DateManager) {

    //scheduling notifications at specific time
    private fun createAlert(alertTimeInterval: Long, requestCode: Int){
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("Question", "Have you got some time to answer a question")
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTimeInterval, pendingIntent)
    }

    @SuppressLint("SimpleDateFormat")
    fun scheduleAlerts(sensingRequests:String){
        //parsowanie tekstu na obiekty
        val gson = Gson()
        val sdf = dateManager.getSimpleDateFormat()
        val sensingRequestList: List<SensingRequestModel> =
                gson.fromJson(sensingRequests, Array<SensingRequestModel>::class.java).toList()
        for((iterator, i) in sensingRequestList.withIndex()){
            //tworzenie notyfikacji dla zadanych wartosci pytan
            val date = sdf.parse(i.time)
            createAlert(date.time, iterator)
        }
        Toast.makeText(context,"File loaded successfully!", Toast.LENGTH_SHORT).show()
        //kill activity after creating alerts
        context.finishAndRemoveTask()
    }


}