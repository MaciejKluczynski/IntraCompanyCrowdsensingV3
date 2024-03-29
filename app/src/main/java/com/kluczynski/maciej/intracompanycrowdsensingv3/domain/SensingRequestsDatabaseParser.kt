package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel

class SensingRequestsDatabaseParser {
    fun parseTextToSensingRequestModelList(fileContent:String):MutableList<SensingRequestModel>{
        return Gson().fromJson(fileContent, Array<SensingRequestModel>::class.java).toMutableList()
    }
}