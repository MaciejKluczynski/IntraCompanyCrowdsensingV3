package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import java.time.LocalDate
import java.time.LocalTime


class DismissBroadcast : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DISMISSED", "DISMISSED")
        context?.let { context ->
            //zabezpieczenie - uzydkownik wylaczy permissiony do aplikacji podczas jej uzywania
            if (checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                //logika biznesowa - znalezienie pytania i udzielenie odpowiedzi
                val content = intent!!.getStringExtra("Content")
                val type = intent!!.getStringExtra("QuestionType")
                val questionTime = intent!!.getStringExtra("QuestionTime")
                val why_ask = intent!!.getStringExtra("WhyAsk")
                val hint = intent!!.getStringExtra("Hint")
                val sensingRequestId = intent!!.getStringExtra("Id") ?: "-1"
                content?.let { content ->
                    ResultSaver(context).saveResult(
                        ResultModel(
                            questionContent = content,
                            result = "DISMISSED",
                            askTimeSensingRequest = questionTime.toString(),
                            timeDisplayQuestionOnScreen = questionTime.toString(),
                            answerTime = "${LocalDate.now()} ${LocalTime.now()}",
                            comment = "",
                            sensingRequestId = sensingRequestId
                        )
                    )
                }
            }

        }
    }
}