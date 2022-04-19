package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateManager {

    fun getCurrentDate():String{
        val date = Calendar.getInstance().time
        val sdf = getSimpleDateFormat()
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getSimpleDateFormat() = SimpleDateFormat("dd-M-yyyy HH:mm:ss")
    fun getSimpleDateOnlyFormat() = SimpleDateFormat("dd-M-yyyy")
    fun getCurrentTimeMs(): Long = Calendar.getInstance().timeInMillis
    fun convertDateToMs(date:String):Long = getSimpleDateFormat().parse(date).time
    fun convertDateToString(date: Date):String = getSimpleDateFormat().format(date)
}