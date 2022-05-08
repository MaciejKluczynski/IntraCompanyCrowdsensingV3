package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.ReminderBroadcast
import java.util.*

class AlertManager(var context: Context, var dateManager: DateManager) {

    //scheduling notifications at specific time
    private fun createAlert(
        date: Date,
        requestCode: Int,
        questionContent: String,
        hint: String,
        questionType: String,
        why_ask: String
    ) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("Question", "Have you got some time to answer a question")
        intent.putExtra("Content", questionContent)
        intent.putExtra("Hint", hint)
        intent.putExtra("WhyAsk", why_ask)
        intent.putExtra("QuestionType", questionType)
        intent.putExtra("QuestionTime", DateManager().convertDateToString(date))

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.time, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    fun scheduleAlerts(examinationPlan: List<ExaminationPlanModel>) {
        var i = 0
        for (dayOfExamination in examinationPlan) {
            //tworzenie notyfikacji dla zadanych wartosci pytan
            dayOfExamination.allocatedSensingRequests.forEach { sensingRequest ->
                var monthName = dayOfExamination.singleDateOfExaminationPlan.monthValue.toString()
                if (monthName.length == 1) {
                    monthName = "0$monthName"
                }
                val date = dateManager.getSimpleDateFormat()
                    .parse(
                        "${dayOfExamination.singleDateOfExaminationPlan.dayOfMonth}-${monthName}-${dayOfExamination.singleDateOfExaminationPlan.year} ${sensingRequest.time}:00"
                    )
                createAlert(
                    date,
                    i,
                    sensingRequest.content,
                    sensingRequest.hint,
                    sensingRequest.questionType,
                    sensingRequest.why_ask
                )
                i++
            }
        }
        Toast.makeText(context, "File loaded successfully!", Toast.LENGTH_SHORT).show()
        killTheActivity()
    }

    private fun killTheActivity() {
        //kill activity after creating alerts
        if (context is AppCompatActivity)
            (context as AppCompatActivity).finishAndRemoveTask()
    }


}