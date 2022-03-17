package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel

class ObjectToResultConverter {
    fun convertObjectToJson(obiect: ResultModel):String = Gson().toJson(obiect)
}