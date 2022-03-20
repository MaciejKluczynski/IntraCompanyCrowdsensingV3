package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*

class DismissBroadcast: BroadcastReceiver() {

    private val firebaseManager = FirebaseManager("TEST")
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
            firebaseManager.insertSensingRequestResultIntoDatabase(ResultModel(
                sensingRequest.content,
                "DISMISSED",
                sensingRequest.time,
                dateManager.getCurrentDate(),
                ""))
            fileManager.saveResultToFile(
                sensingRequest.content,
                "DISMISSED",
                sensingRequest.time,
                dateManager.getCurrentDate(),
                "")
        }
        //start dismiss activity
        /*val i = Intent()
        i.setClassName("com.kluczynski.maciej.navigationtutorial2", "com.kluczynski.maciej.navigationtutorial2.DismissActivity")
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(i)*/
    }
}