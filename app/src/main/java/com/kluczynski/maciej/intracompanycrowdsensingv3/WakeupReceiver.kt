package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast


class WakeupReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        Log.d("EXC", "EXCs")
        Toast.makeText(context, "EXCs",Toast.LENGTH_LONG).show()
        context?.let {
            WakeupScheduler().scheduleWakeup(it,24,0,0)
        }
    }
}