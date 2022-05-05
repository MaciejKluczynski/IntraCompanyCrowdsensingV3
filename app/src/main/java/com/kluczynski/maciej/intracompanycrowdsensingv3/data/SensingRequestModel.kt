package com.kluczynski.maciej.intracompanycrowdsensingv3.data

import java.time.LocalTime

data class SensingRequestModel(
        var sensing_request_id:String,
        var priority:Int,
        var frequency:String,
        var desired_time_of_the_day:TimeOfTheDay?,
        var desired_day_of_the_week:DayOfTheWeek?,
        var content:String,
        var questionType:String,
        var why_ask:String,
        var hint:String,
        var time:LocalTime?,
        )
//time jest w formacie SimpleDateFormat("dd-M-yyyy HH:mm:ss")
