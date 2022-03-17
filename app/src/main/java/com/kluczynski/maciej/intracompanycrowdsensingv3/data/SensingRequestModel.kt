package com.kluczynski.maciej.intracompanycrowdsensingv3.data

data class SensingRequestModel(
        val content:String,
        val questionType:String,
        val time:String,
        val reason:String,
        val hint:String
        )
//time jest w formacie SimpleDateFormat("dd-M-yyyy HH:mm:ss")
