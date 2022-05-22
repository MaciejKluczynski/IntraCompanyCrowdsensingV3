package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

class SharedPrefsProvider(var context: Context) {
    fun getUserNameFromSharedPrefs():String{
        //todo preferenceManager - shared preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val filePath = preferences.getString("username", "abc")
        return filePath.toString()
    }

    fun generateAndSaveUserNameInSharedPrefs(){
        //todo shared prefs - update
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val id = UUID.randomUUID().toString()
        editor.putString("username", id)
        editor.apply()
    }
}