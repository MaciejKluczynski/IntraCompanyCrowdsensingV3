package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager

class FirebaseStorageManager(var context: Context) {

    private val fileManager = FileManager(context)
    var storageRef = FirebaseStorage.getInstance().reference


    fun backupTxtFileToCloud(){
        val filesRef = storageRef.child("/abc")
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            fileManager.findFileAndGetUriAndQAndAbove()?.let {
                filesRef.putFile(it)
            }else{
            //TODO test
            fileManager.getFileUriAndroidBelowQ()?.let {
                filesRef.putFile(it)
            }
        }
    }
}