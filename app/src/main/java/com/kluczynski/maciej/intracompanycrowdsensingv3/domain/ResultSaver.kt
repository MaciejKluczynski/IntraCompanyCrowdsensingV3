package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase.FirebaseDatabaseManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase.FirebaseLoginManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase.FirebaseStorageManager

class ResultSaver(var context: Context) {

    private val firebaseStorageManager = FirebaseStorageManager(context)
    private val firebaseLoginManager = FirebaseLoginManager(context)
    private val firebaseDatabaseManager = FirebaseDatabaseManager("TEST")
    private val fileManager = FileManager(context)

    fun saveResult(result: ResultModel){
        authenticateUser()
        saveDataToTextFile(result)
        saveResultToFirebase(result)
        uploadFileToCloud()
        killActivityIfExists()
    }

    private fun authenticateUser(){
        firebaseLoginManager.provideUser()
    }

    private fun saveResultToFirebase(result: ResultModel){
        firebaseDatabaseManager.insertSensingRequestResultIntoDatabase(result)
    }

    private fun uploadFileToCloud(){
        firebaseStorageManager.backupTxtFileToCloud()
    }

    private fun saveDataToTextFile(result: ResultModel){
        fileManager.saveResultToFile(
                result.content,
                result.result,
                result.ask_time,
                result.answer_time,
                result.comment
        )
    }

    private fun killActivityIfExists(){
        //kill the activity
        if (context is AppCompatActivity)
            (context as AppCompatActivity).finishAndRemoveTask()
    }
}