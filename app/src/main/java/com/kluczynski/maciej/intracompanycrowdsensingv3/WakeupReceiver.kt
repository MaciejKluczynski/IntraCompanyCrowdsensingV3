package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.WakeupScheduler


class WakeupReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        // tutaj co ma sie dziac po wybudzeniu aplikacji
        Log.d("EXC", "EXCs")
        Toast.makeText(context, "EXCs",Toast.LENGTH_LONG).show()
        context?.let {
            WakeupScheduler().scheduleWakeup(it,24,0,0)
        }
    }
}