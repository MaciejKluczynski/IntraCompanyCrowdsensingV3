package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SensingRequestsResultFilePathProvider


class MainActivity : AppCompatActivity() {

    companion object{
        const val PERMISSION_READ_AND_WRITE_STORAGE = 101
        const val READ_REQUEST_CODE = 420
        const val CHANNEL_NAME = "ICC_CHANNEL"
        const val CHANNEL_DESCRIPTION = "App for ICC examination"
        const val CHANNEL_ID = "ICC"
    }

    private val notificationManager = MyNotificationManager(this)
    private val fileManager = FileManager(this)
    private val alertManager = AlertManager(this, DateManager())
    private val sensingRequestsResultFilePathProvider = SensingRequestsResultFilePathProvider(this)

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationManager.cancellNotification()
        notificationManager.createNotificationChannel()
        activateLoadBtn()
    }

    private fun activateLoadBtn(){
        val loadBtn = findViewById<Button>(R.id.load_button)
        loadBtn.setOnClickListener {
            //request write storage permission
            //android M - Marshmello 6.0
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M &&
                    (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED)){
                requestPermissions(
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_READ_AND_WRITE_STORAGE
                )
            }
            //only when 2 permissions are provided - open file search
            else openFileSearch()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_READ_AND_WRITE_STORAGE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission to READ AND WRITE storage Granted", Toast.LENGTH_SHORT).show()
                openFileSearch()
            }
            else{
                Toast.makeText(this, "Permission to READ AND WRITE storage NOT Granted", Toast.LENGTH_SHORT).show()
                finishAndRemoveTask()
            }
        }
    }

    //find file in the filesystem
    //method to open file explorer
    private fun openFileSearch(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    //metoda ktora sie wykonuje po wybraniu pliku
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data!=null){
                //wczytanie danych z pliku txt do zmiennej
                val uri: Uri = data.data!!
                sensingRequestsResultFilePathProvider.savePathInSharedPrefs(uri.toString())
                val fileContent = fileManager.readFileContent(uri)
                alertManager.scheduleAlerts(fileContent)
            }
        }

    }
}