package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager

class FirebaseStorageManager(var context: Context) {

    private val fileManager = FileManager(context)
    var storageRef = FirebaseStorage.getInstance().reference

    fun backupTxtFileToCloud(nick:String): UploadTask? {
        val filesRef = storageRef.child("/$nick")
        val url:Uri? = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            fileManager.findFileAndGetUriAndQAndAbove()
        } else{
            fileManager.getFileUriAndroidBelowQ()
        }
        return url?.let { filesRef.putFile(it)  }
    }
}