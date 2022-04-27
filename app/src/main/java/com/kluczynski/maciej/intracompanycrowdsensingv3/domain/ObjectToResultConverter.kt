package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel

class ObjectToResultConverter {
    fun <T> convertObjectToJson(obiect: T):String = Gson().toJson(obiect)
}