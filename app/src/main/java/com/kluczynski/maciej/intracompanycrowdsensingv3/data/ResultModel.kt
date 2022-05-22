package com.kluczynski.maciej.intracompanycrowdsensingv3.data

data class ResultModel(
        val sensingRequestId:String,
        val questionContent:String,
        val result: String,
        val askTimeSensingRequest: String,
        val timeDisplayQuestionOnScreen:String,
        val answerTime: String,
        val comment:String)