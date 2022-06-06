package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SharedPrefsProvider

class FirebaseLoginManager(var context: Context) {

    private lateinit var auth: FirebaseAuth

    private val firebaseStorageManager = FirebaseStorageManager(context)
    private val sensingRequestsResultFilePathProvider = SharedPrefsProvider(context)
    private val firebaseDatabaseManager = FirebaseDatabaseManager(
            sensingRequestsResultFilePathProvider.getUserNameFromSharedPrefs())

    fun provideUserAndPerformCloudOperations(result: ResultModel,context: Context) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("USER ALREADY LOGGED IN", currentUser.email.toString())
            Toast.makeText(context,"USER ALREADY LOGGED IN ${currentUser.email}",Toast.LENGTH_LONG).show()
            performCloudOperations(result)
        }else{
            performLoginAndUploadResults(result)
        }
    }

/*    fun provideUserAndUpdateNickToCloud(){
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("USER ALREADY LOGGED IN", currentUser.email.toString())
            Toast.makeText(context,"USER ALREADY LOGGED IN ${currentUser.email}",Toast.LENGTH_LONG).show()
            saveNickToFirebase()
        }else{
            performLoginAndUploadNick()
        }
    }

    private fun performLoginAndUploadNick(){
        var user:FirebaseUser?
        auth.signInWithEmailAndPassword("icc.examination@gmail.com", "Icc2022!")
            .addOnSuccessListener {
                user = auth.currentUser
                user?.let {
                    Log.d("LOGGED IN SUCCESSFULLY",it.email.toString())
                    Toast.makeText(context, "SUCCESFULLY LOGGED IN ${it.email}",Toast.LENGTH_LONG).show()
                    saveNickToFirebase()
                }
            }
            .addOnFailureListener {
                Log.d("FAILED TO LOG IN","FAILED TO LOG IN")
            }
    }*/

    fun provideUserAndUploadScheduleFile(context: Context) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("USER ALREADY LOGGED IN", currentUser.email.toString())
            Toast.makeText(context,"USER ALREADY LOGGED IN ${currentUser.email}",Toast.LENGTH_LONG).show()
            uploadScheduleFileToCloud()
        }else{
            performLoginAndUploadSchedule()
        }
    }

    private fun performCloudOperations(result: ResultModel){
        uploadResultsFileToCloud()
        saveResultToFirebase(result)
    }

    private fun saveResultToFirebase(result: ResultModel){
        firebaseDatabaseManager.insertSensingRequestResultIntoDatabase(result)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(context,"DocumentSnapshot added with ID: ${documentReference.id}",Toast.LENGTH_LONG).show()
                    //killActivityIfExists()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                    Toast.makeText(context,
                            "DOCUMENT NOT SAVED TO CLOUD - PLEASE CONTACT ADMINISTRATOR",
                            Toast.LENGTH_LONG).show()
                    //killActivityIfExists()
                }
    }

  /*  private fun saveNickToFirebase(){
        firebaseDatabaseManager.insertUserIdToDatabase()
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(context,"DocumentSnapshot added with ID: ${documentReference.id}",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
                Toast.makeText(context,
                    "DOCUMENT NOT SAVED TO CLOUD - PLEASE CONTACT ADMINISTRATOR",
                    Toast.LENGTH_LONG).show()
            }
    }*/

    private fun uploadResultsFileToCloud(){
        firebaseStorageManager.backupResultsFileToCloud(
                sensingRequestsResultFilePathProvider.getUserNameFromSharedPrefs())
                ?.addOnSuccessListener {
            Log.d("TAG", "Results file uploaded successfully")
            Toast.makeText(context,"Results file uploaded to cloud successfully",Toast.LENGTH_LONG).show()
                    killActivityIfExists()
        }
                ?.addOnFailureListener {
                    Toast.makeText(context,"Results file NOT uploaded to cloud - contact administrator",Toast.LENGTH_LONG).show()
                    killActivityIfExists()
                }
    }

    private fun uploadScheduleFileToCloud(){
        firebaseStorageManager.backupScheduleFileToCloud(
            sensingRequestsResultFilePathProvider.getUserNameFromSharedPrefs())
            ?.addOnSuccessListener {
                Log.d("TAG", "file uploaded successfully")
                Toast.makeText(context,"Schedule file uploaded to cloud successfully",Toast.LENGTH_LONG).show()
            }
            ?.addOnFailureListener {
                Toast.makeText(context,"Schedule file NOT uploaded to cloud - contact administrator",Toast.LENGTH_LONG).show()
            }
    }

    private fun killActivityIfExists(){
        //kill the activity
        if (context is AppCompatActivity)
            (context as AppCompatActivity).finishAndRemoveTask()
    }

    private fun performLoginAndUploadResults(result: ResultModel) {
        var user:FirebaseUser?
        auth.signInWithEmailAndPassword("icc.examination@gmail.com", "Icc2022!")
                .addOnSuccessListener {
                    user = auth.currentUser
                    user?.let {
                        Log.d("LOGGED IN SUCCESSFULLY",it.email.toString())
                        Toast.makeText(context, "SUCCESFULLY LOGGED IN ${it.email}",Toast.LENGTH_LONG).show()
                        performCloudOperations(result)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "FAILED TO LOG IN",Toast.LENGTH_LONG).show()
                    Log.d("FAILED TO LOG IN","FAILED TO LOG IN")
                }
    }

    private fun performLoginAndUploadSchedule() {
        var user:FirebaseUser?
        auth.signInWithEmailAndPassword("icc.examination@gmail.com", "Icc2022!")
            .addOnSuccessListener {
                user = auth.currentUser
                user?.let {
                    Log.d("LOGGED IN SUCCESSFULLY",it.email.toString())
                    Toast.makeText(context, "SUCCESFULLY LOGGED IN ${it.email}",Toast.LENGTH_LONG).show()
                    uploadScheduleFileToCloud()
                }
            }
            .addOnFailureListener {
                Log.d("FAILED TO LOG IN","FAILED TO LOG IN")
            }
    }
}