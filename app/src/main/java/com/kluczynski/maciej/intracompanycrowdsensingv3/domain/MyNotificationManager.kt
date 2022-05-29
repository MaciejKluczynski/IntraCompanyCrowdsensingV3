package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kluczynski.maciej.intracompanycrowdsensingv3.*
import java.util.*

class MyNotificationManager(val context: Context) {

    @SuppressLint("WrongConstant")
    fun createNotificationChannel() {
        //android O - Oreo Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = MainActivity.CHANNEL_NAME
            val description = MainActivity.CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_MAX
            val notificationChannel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance)
            notificationChannel.description = description
            val notificationManager =
                context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //cancelling notifications
    fun cancellNotification() {
        //cancell notification
        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun createActivityPendingIntent(
        context: Context,
        content: String,
        hint: String,
        type: String,
        why_ask: String,
        questionTime: Date,
        sensingRequestId: String,
        buttonOption1: String,
        buttonOption2: String,
        buttonOption3: String? = null,
        buttonOption4: String? = null,
    ): PendingIntent? {
        //start result activity on click on notification
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val mainIntent = Intent(context, ResultBroadcast::class.java)
            mainIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            mainIntent.putExtra("Content", content)
            mainIntent.putExtra("Hint", hint)
            mainIntent.putExtra("QuestionType", type)
            mainIntent.putExtra("WhyAsk", why_ask)
            mainIntent.putExtra("QuestionTime", DateManager().convertDateToString(questionTime))
            mainIntent.putExtra("Id", sensingRequestId)
            mainIntent.putExtra("buttonOption1", buttonOption1)
            mainIntent.putExtra("buttonOption2", buttonOption2)
            mainIntent.putExtra("buttonOption3", buttonOption3)
            mainIntent.putExtra("buttonOption4", buttonOption4)
            return PendingIntent.getBroadcast(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            val mainIntent =
                Intent(context.applicationContext, ResultActivity::class.java)
            // Create the TaskStackBuilder
            mainIntent.putExtra("Content", content)
            mainIntent.putExtra("Hint", hint)
            mainIntent.putExtra("QuestionType", type)
            mainIntent.putExtra("WhyAsk", why_ask)
            mainIntent.putExtra("QuestionTime", DateManager().convertDateToString(questionTime))
            mainIntent.putExtra("Id", sensingRequestId)
            mainIntent.putExtra("buttonOption1", buttonOption1)
            mainIntent.putExtra("buttonOption2", buttonOption2)
            mainIntent.putExtra("buttonOption3", buttonOption3)
            mainIntent.putExtra("buttonOption4", buttonOption4)
            return TaskStackBuilder.create(context).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(mainIntent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT or
                            // mutability flag required when targeting Android12 or higher
                            PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
    }

        private fun createDismissPendingIntent(
            context: Context,
            content: String,
            hint: String,
            type: String,
            why_ask: String,
            questionTime: Date,
            sensingRequestId: String
        ): PendingIntent? {
            //start dismiss Broadcast as a result of dismissing notification
            val deleteIntent = Intent(context, DismissBroadcast::class.java)
            //deleteIntent.action = "notification_cancelled"
            deleteIntent.putExtra("Content", content)
            deleteIntent.putExtra("Hint", hint)
            deleteIntent.putExtra("QuestionType", type)
            deleteIntent.putExtra("WhyAsk", why_ask)
            deleteIntent.putExtra("QuestionTime", DateManager().convertDateToString(questionTime))
            deleteIntent.putExtra("Id", sensingRequestId)
            return PendingIntent.getBroadcast(
                context,
                0,
                deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        //https://stackoverflow.com/questions/5399229/how-to-remove-the-notification-after-the-particular-time-in-android
        //timeout not working in api<26
        //https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder#setTimeoutAfter(long)
        fun createCloseEndedNotification(context: Context, intent: Intent?) {
            val content = intent!!.getStringExtra("Content")
            val hint = intent!!.getStringExtra("Hint")
            val type = intent!!.getStringExtra("QuestionType")
            val why_ask = intent!!.getStringExtra("WhyAsk")
            val questionTime = intent!!.getStringExtra("QuestionTime")
            val sensingRequestId = intent!!.getStringExtra("Id") ?: "-1"
            val buttonOption1 = intent.getStringExtra("buttonOption1") ?: "YES"
            val buttonOption2 = intent.getStringExtra("buttonOption2") ?: "NO"
            val buttonOption3 = intent.getStringExtra("buttonOption3")
            val buttonOption4 = intent.getStringExtra("buttonOption4")
            val dateManager = DateManager()
            val date_internal = dateManager.getSimpleDateFormat().parse(questionTime)
            val activityPendingIntent = createActivityPendingIntent(
                context = context,
                content = content!!,
                hint = hint!!,
                type = type!!,
                why_ask = why_ask!!,
                questionTime = date_internal,
                sensingRequestId = sensingRequestId,
                buttonOption1 = buttonOption1,
                buttonOption2 = buttonOption2,
                buttonOption3 = buttonOption3,
                buttonOption4 = buttonOption4,
            )
            val dismissPendingIntent =
                createDismissPendingIntent(
                    context = context,
                    content = content!!,
                    hint = hint!!,
                    type = type!!,
                    why_ask = why_ask!!,
                    questionTime = date_internal,
                    sensingRequestId = sensingRequestId
                )
            //create notification
            val builder = NotificationCompat.Builder(context, "ICC")
            builder.setSmallIcon(R.mipmap.ic_launcher)
            builder.setContentTitle("Intra Company Crowdsensing Examination")
            builder.setContentText(intent!!.getStringExtra("Question"))
            builder.priority = NotificationCompat.PRIORITY_MAX
            //dismiss on tap
            builder.setAutoCancel(true)
            //on notification click action
            builder.setContentIntent(activityPendingIntent)
            // set Timeout After - anuluje notyfikacje po zadanej liczbie milisekund
            builder.setTimeoutAfter(ReminderBroadcast.NOTIFICATION_TIMEOUT_MS)
            builder.setDeleteIntent(dismissPendingIntent)
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(1, builder.build())
        }
    }