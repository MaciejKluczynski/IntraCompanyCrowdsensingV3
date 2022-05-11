package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files

import android.content.Context
import android.preference.PreferenceManager

class SharedPrefsProvider(var context: Context) {
    fun getUserNameFromSharedPrefs():String{
        //todo preferenceManager - shared preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val filePath = preferences.getString("username", "abc")
        return filePath.toString()
    }

    fun saveUserNameInSharedPrefs(username:String){
        //todo shared prefs - update
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString("username", username)
        editor.apply()
    }
}