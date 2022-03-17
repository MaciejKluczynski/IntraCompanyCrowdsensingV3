package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.content.Context
import android.preference.PreferenceManager

class SensingRequestsResultFilePathProvider(var context: Context) {
    fun getFilePathFromSharedPrefs():String{
        //todo preferenceManager - shared preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val filePath = preferences.getString("file_path", "")
        return filePath.toString()
    }

    fun savePathInSharedPrefs(filepath:String){
        //todo shared prefs - update
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString("file_path", filepath)
        editor.apply()
    }
}