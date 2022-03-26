package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process.myPid
import android.os.Process.myUid
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsParser
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsResultFilePathProvider

class DismissBroadcast: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DISMISSED", "DISMISSED")
        context?.let{
            val fileManager =  FileManager(it)
            val dateManager = DateManager()
            val sensingRequestsParser = SensingRequestsParser(
                    SensingRequestsResultFilePathProvider(it),
                    fileManager,
                    dateManager
            )
            //zabezpieczenie - uzydkownik wylaczy permissiony do aplikacji podczas jej uzywania
            if (checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)==
                    PackageManager.PERMISSION_GRANTED){
                //logika biznesowa - znalezienie pytania i udzielenie odpowiedzi
                val sensingRequest = sensingRequestsParser.findAskedSensingRequest()
                ResultSaver(it).saveResult(ResultModel(
                        sensingRequest.content,
                        "DISMISSED",
                        sensingRequest.time,
                        dateManager.getCurrentDate(),
                        ""))
            }

        }
    }
}