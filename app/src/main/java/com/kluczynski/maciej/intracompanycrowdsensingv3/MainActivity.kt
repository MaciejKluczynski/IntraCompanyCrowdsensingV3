package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SharedPrefsProvider


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_READ_AND_WRITE_STORAGE = 101
        const val READ_DATABASE_REQUEST_CODE = 420
        const val READ_USER_PREFERENCES_REQUEST_CODE = 421
        const val CHANNEL_NAME = "ICC_CHANNEL"
        const val CHANNEL_DESCRIPTION = "App for ICC examination"
        const val CHANNEL_ID = "ICC"
    }

    private val notificationManager = MyNotificationManager(this)
    private val fileManager = FileManager(this)
    private val alertManager = AlertManager(this, DateManager())
    private val sensingRequestsResultFilePathProvider = SharedPrefsProvider(this)
    private lateinit var sensingRequests: MutableList<SensingRequestModel>
    private lateinit var userPreferences: UserPreferencesModel

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationManager.cancellNotification()
        notificationManager.createNotificationChannel()
        activateLoadSensingRequestsBtn()
    }

    private fun activateLoadUserPreferencesBtn() {
        val userPrefsBtn = findViewById<Button>(R.id.activity_main_load_user_preferences_btn)
        userPrefsBtn.setOnClickListener {
            openFileSearch(READ_USER_PREFERENCES_REQUEST_CODE)
        }
    }

    private fun activateLoadSensingRequestsBtn() {
        val loadBtn = findViewById<Button>(R.id.activity_main_load_sensing_requests_btn)
        loadBtn.setOnClickListener {
            val nick = findViewById<EditText>(R.id.activityMainUserNameEditText).text.toString()
            //request write storage permission
            //android M - Marshmello 6.0
            if (nick != "") {
                sensingRequestsResultFilePathProvider.saveUserNameInSharedPrefs(nick)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                PackageManager.PERMISSION_GRANTED ||
                                checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_READ_AND_WRITE_STORAGE
                    )
                }
                //only when 2 permissions are provided - open file search
                else openFileSearch(READ_DATABASE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "You have to provide nick first", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_READ_AND_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission to READ AND WRITE storage Granted", Toast.LENGTH_SHORT).show()
                openFileSearch(READ_DATABASE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Permission to READ AND WRITE storage NOT Granted", Toast.LENGTH_SHORT).show()
                finishAndRemoveTask()
            }
        }
    }

    //find file in the filesystem
    //method to open file explorer
    private fun openFileSearch(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        startActivityForResult(intent, requestCode)
    }

    //metoda ktora sie wykonuje po wybraniu pliku
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_DATABASE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                //wczytanie danych z pliku txt do zmiennej
                val uri: Uri = data.data!!
                sensingRequestsResultFilePathProvider.savePathInSharedPrefs(uri.toString())
                val fileContent = fileManager.readFileContent(uri)
                if (fileContent != null) {
                    sensingRequests = SensingRequestsDatabaseParser().parseTextToSensingRequestModelList(fileContent)
                    Log.d("TEST",sensingRequests.toString())
                    activateLoadUserPreferencesBtn()
                }else{
                    Toast.makeText(this,"Sensing requests file is empty",Toast.LENGTH_LONG).show()
                    finishAndRemoveTask()
                }
            }
        }
        if (requestCode == READ_USER_PREFERENCES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                //wczytanie danych z pliku txt do zmiennej
                val uri: Uri = data.data!!
                sensingRequestsResultFilePathProvider.savePathInSharedPrefs(uri.toString())
                val fileContent = fileManager.readFileContent(uri)
                if (fileContent != null) {
                    userPreferences = UserPreferencesParser().parseTextToUserPreferencesModel(fileContent)
                    Log.d("TEST",userPreferences.toString())
                    //todo planowanie alertow - algorytm alokacji oraz wyswietlanie notyfikcaji i zabicie aktywanosci
                    SensingRequestsAllocationAlgorithm().allocateSensingRequests(userPreferences, sensingRequests)
                }else{
                    Toast.makeText(this,"User preferences file is empty",Toast.LENGTH_LONG).show()
                    finishAndRemoveTask()
                }
            }
        }

    }
}