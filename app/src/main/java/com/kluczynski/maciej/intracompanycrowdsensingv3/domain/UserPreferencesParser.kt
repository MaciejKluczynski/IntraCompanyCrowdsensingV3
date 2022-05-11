package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel

class UserPreferencesParser {
    fun parseTextToUserPreferencesModel(fileContent:String):UserPreferencesModel{
        return Gson().fromJson(fileContent, UserPreferencesModel::class.java)
    }
}