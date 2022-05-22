package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResultBroadcast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val content = intent!!.getStringExtra("Content")
        val type = intent.getStringExtra("QuestionType")
        val questionTime = intent.getStringExtra("QuestionTime")
        val whyAsk = intent.getStringExtra("WhyAsk")
        val hint = intent.getStringExtra("Hint")
        val sensingRequestId = intent.getStringExtra("Id")

        val activityIntent = Intent(context,ResultActivity::class.java)
        activityIntent.putExtra("Content",content)
        activityIntent.putExtra("QuestionType",type)
        activityIntent.putExtra("QuestionTime",questionTime)
        activityIntent.putExtra("WhyAsk",whyAsk)
        activityIntent.putExtra("Hint",hint)
        activityIntent.putExtra("Id",sensingRequestId)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(activityIntent)
    }
}