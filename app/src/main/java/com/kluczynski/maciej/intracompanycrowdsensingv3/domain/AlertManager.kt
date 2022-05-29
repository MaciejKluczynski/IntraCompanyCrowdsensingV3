package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.ReminderBroadcast
import java.util.*

class AlertManager(var context: Context, var dateManager: DateManager) {

    //scheduling notifications at specific time
    private fun createAlert(
        sensingRequestAskTime: Date,
        requestCode: Int,
        questionContent: String,
        hint: String,
        questionType: String,
        whyAsk: String,
        sensingRequestId:String,
        buttonOptions:List<String>?
    ) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("Question", "Have you got some time to answer a question")
        intent.putExtra("Content", questionContent)
        intent.putExtra("Hint", hint)
        intent.putExtra("WhyAsk", whyAsk)
        intent.putExtra("QuestionType", questionType)
        intent.putExtra("QuestionTime", DateManager().convertDateToString(sensingRequestAskTime))
        intent.putExtra("Id", sensingRequestId)
        //add button options to intent
        buttonOptions?.let {
            var optionNumber = 0
            while(optionNumber < buttonOptions.size){
                intent.putExtra("buttonOption${optionNumber+1}",buttonOptions[optionNumber])
                optionNumber++
            }
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, sensingRequestAskTime.time, pendingIntent)
        }else{
            val canScheduleAlarm = alarmManager.canScheduleExactAlarms()
            if (!canScheduleAlarm){
                alarmManager.set(AlarmManager.RTC_WAKEUP, sensingRequestAskTime.time, pendingIntent)
            }else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, sensingRequestAskTime.time, pendingIntent)
            }
        }
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
                    sensingRequestAskTime = date,
                    requestCode = i,
                    questionContent = sensingRequest.content,
                    hint = sensingRequest.hint,
                    questionType =  sensingRequest.questionType,
                    whyAsk = sensingRequest.why_ask,
                    sensingRequestId = sensingRequest.sensing_request_id,
                    buttonOptions = sensingRequest.buttonOptions
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