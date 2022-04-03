package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.ReminderBroadcast
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import java.util.*

class AlertManager(var context: Context, var dateManager: DateManager) {

    //scheduling notifications at specific time
    private fun createAlert(date:Date ,
                            requestCode: Int,
                            questionContent:String,
                            hint:String,
                            questionType:String,
                            why_ask:String){
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("Question", "Have you got some time to answer a question")
        intent.putExtra("Content",questionContent)
        intent.putExtra("Hint",hint)
        intent.putExtra("WhyAsk",why_ask)
        intent.putExtra("QuestionType",questionType)
        intent.putExtra("QuestionTime",DateManager().convertDateToString(date))

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
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
            createAlert(date,
                    iterator,
                    sensingRequestList[iterator].content,
                    sensingRequestList[iterator].hint,
                    sensingRequestList[iterator].questionType,
                    sensingRequestList[iterator].why_ask)
        }
        Toast.makeText(context,"File loaded successfully!", Toast.LENGTH_SHORT).show()
        killTheActivity()
    }

    private fun killTheActivity(){
        //kill activity after creating alerts
        if (context is AppCompatActivity)
            (context as AppCompatActivity).finishAndRemoveTask()
    }


}