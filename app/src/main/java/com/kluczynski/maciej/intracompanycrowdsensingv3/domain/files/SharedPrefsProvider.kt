package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files

import android.content.Context
import android.preference.PreferenceManager

class SharedPrefsProvider(var context: Context) {
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