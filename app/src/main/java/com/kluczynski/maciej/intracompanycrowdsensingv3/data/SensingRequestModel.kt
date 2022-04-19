package com.kluczynski.maciej.intracompanycrowdsensingv3.data

data class SensingRequestModel(
        val sensing_request_id:String,
        val priority:Int,
        val frequency:String,
        var desired_time_of_the_day:TimeOfTheDay?,
        var desired_day_of_the_week:DayOfTheWeek?,
        val content:String,
        val questionType:String,
        val why_ask:String,
        val hint:String
        )
//time jest w formacie SimpleDateFormat("dd-M-yyyy HH:mm:ss")
