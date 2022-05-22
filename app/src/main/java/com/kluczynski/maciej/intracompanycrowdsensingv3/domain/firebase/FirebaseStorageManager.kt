package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SharedPrefsProvider

class FirebaseStorageManager(var context: Context) {

    private val fileManager = FileManager(context)
    var storageRef = FirebaseStorage.getInstance().reference
    private val sharedPrefsProvider = SharedPrefsProvider(context)

    fun backupResultsFileToCloud(nick:String): UploadTask? {
        val filesRef = storageRef.child("/${nick}_results")
        val url:Uri? = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            fileManager.findFileAndGetUriAndQAndAbove("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt")
        } else{
            fileManager.getFileUriAndroidBelowQ("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt")
        }
        return url?.let { filesRef.putFile(it)  }
    }

    fun backupScheduleFileToCloud(nick:String): UploadTask? {
        val filesRef = storageRef.child("/${nick}_schedule")
        val url:Uri? = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            fileManager.findFileAndGetUriAndQAndAbove("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_schedule.txt")
        } else{
            fileManager.getFileUriAndroidBelowQ("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_schedule.txt")
        }
        return url?.let { filesRef.putFile(it)  }
    }

}