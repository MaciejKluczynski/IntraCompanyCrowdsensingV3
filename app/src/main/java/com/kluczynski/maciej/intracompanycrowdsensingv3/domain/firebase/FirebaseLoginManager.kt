package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsResultFilePathProvider

class FirebaseLoginManager(var context: Context) {

    private lateinit var auth: FirebaseAuth

    private val firebaseStorageManager = FirebaseStorageManager(context)
    private val sensingRequestsResultFilePathProvider = SensingRequestsResultFilePathProvider(context)
    private val firebaseDatabaseManager = FirebaseDatabaseManager(
            sensingRequestsResultFilePathProvider.getUserNameFromSharedPrefs())

    fun provideUserAndPerformCloudOperations(result: ResultModel,context: Context) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("USER ALREADY LOGGED IN", currentUser.email.toString())
            performCloudOperations(result)
        }else{
            performLogin(result)
        }
    }

    private fun performCloudOperations(result: ResultModel){
        uploadFileToCloud()
        saveResultToFirebase(result)
    }

    private fun saveResultToFirebase(result: ResultModel){
        firebaseDatabaseManager.insertSensingRequestResultIntoDatabase(result)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                    killActivityIfExists()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                    Toast.makeText(context,
                            "DOCUMENT NOT SAVED TO CLOUD - PLEASE CONTACT ADMINISTRATOR",
                            Toast.LENGTH_LONG).show()
                    killActivityIfExists()
                }

    }

    private fun uploadFileToCloud(){
        firebaseStorageManager.backupTxtFileToCloud(
                sensingRequestsResultFilePathProvider.getUserNameFromSharedPrefs())
                ?.addOnSuccessListener {
            Log.d("TAG", "file uploaded successfully")
            Toast.makeText(context,"File uploaded to cloud successfully",Toast.LENGTH_LONG).show()
        }
    }

    private fun killActivityIfExists(){
        //kill the activity
        if (context is AppCompatActivity)
            (context as AppCompatActivity).finishAndRemoveTask()
    }

    private fun performLogin(result: ResultModel) {
        var user:FirebaseUser?
        auth.signInWithEmailAndPassword("icc.examination@gmail.com", "Icc2022!")
                .addOnSuccessListener {
                    user = auth.currentUser
                    user?.let {
                        Log.d("LOGGED IN SUCCESSFULLY",it.email.toString())
                        performCloudOperations(result)
                    }
                }
                .addOnFailureListener {
                    Log.d("FAILED TO LOG IN","FAILED TO LOG IN")
                }
    }
}