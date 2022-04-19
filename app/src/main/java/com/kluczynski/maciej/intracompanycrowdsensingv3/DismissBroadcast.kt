package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*


class DismissBroadcast: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DISMISSED", "DISMISSED")
        context?.let {
            val dateManager = DateManager()
            //zabezpieczenie - uzydkownik wylaczy permissiony do aplikacji podczas jej uzywania
            if (checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                //logika biznesowa - znalezienie pytania i udzielenie odpowiedzi
                /* val content = intent!!.getStringExtra("Content")
                val type = intent!!.getStringExtra("QuestionType")
                val questionTime = intent!!.getStringExtra("QuestionTime")
                val why_ask = intent!!.getStringExtra("WhyAsk")
                val hint = intent!!.getStringExtra("Hint")
                val sensingRequest = SensingRequestModel(
                        content!!,type!!,questionTime!!,why_ask!!,hint!!
                )
                ResultSaver(it).saveResult(ResultModel(
                        sensingRequest.content,
                        "DISMISSED",
                        sensingRequest.time,
                        dateManager.getCurrentDate(),
                        ""))
            }*/

            }
        }
    }
}