package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsParser
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsResultFilePathProvider
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase.FirebaseDatabaseManager

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
            val sensingRequest = sensingRequestsParser.findAskedSensingRequest()
            val resultSaver = ResultSaver(it)
            resultSaver.saveResult(ResultModel(
                    sensingRequest.content,
                    "DISMISSED",
                    sensingRequest.time,
                    dateManager.getCurrentDate(),
                    ""))
        }
    }
}