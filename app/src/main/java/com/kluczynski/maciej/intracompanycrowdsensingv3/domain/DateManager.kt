package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class DateManager {

    companion object{
        const val DATE_AND_TIME_FORMAT_PATTERN = "dd-M-yyyy HH:mm:ss"
        const val DATE_FORMAT_PATTERN = "dd-M-yyyy"
    }
    fun getCurrentDate():String{
        val date = Calendar.getInstance().time
        val sdf = getSimpleDateFormat()
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getSimpleDateFormat() = SimpleDateFormat(DATE_AND_TIME_FORMAT_PATTERN)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSimpleDateOnlyFormat(): DateTimeFormatter? = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
    fun getCurrentTimeMs(): Long = Calendar.getInstance().timeInMillis
    fun convertDateToMs(date:String):Long = getSimpleDateFormat().parse(date).time
    fun convertDateToString(date: Date):String = getSimpleDateFormat().format(date)
    //17-03-2022 13:01:00
}