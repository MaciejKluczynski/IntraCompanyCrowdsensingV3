package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.kluczynski.maciej.intracompanycrowdsensingv3.MainActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.ObjectToResultConverter
import java.io.*

//https://stackoverflow.com/questions/64189667/write-permissions-not-working-scoped-storage-android-sdk-30-aka-android-11
//https://stackoverflow.com/questions/59511147/create-copy-file-in-android-q-using-mediastore
//finding creating and editing files done to Android Q - czyli 10.0
class FileManager(var context: Context) {

    private var objectToResultConverter: ObjectToResultConverter = ObjectToResultConverter()
    private val sharedPrefsProvider = SharedPrefsProvider(context)

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createScheduleFile(content: List<MainActivity.ExaminationPlanString>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createFileAndQAndAbove(
                content,
                "${sharedPrefsProvider.getUserNameFromSharedPrefs()}_schedule.txt"
            )
        } else {
            createTextFileAndWriteDataToItAndroidBelowQ(
                content,
                "${sharedPrefsProvider.getUserNameFromSharedPrefs()}_schedule.txt"
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun <T> createFileAndQAndAbove(content: T, filename: String) {
        try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/"
            )
            val uri =
                context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri!!)
            outputStream!!.write(objectToResultConverter.convertObjectToJson(content).toByteArray())
            outputStream.close()
            Toast.makeText(context, "File created successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Fail to create file", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findAndReadFileAndQAndAbove(): String? {
        var content: String? = ""
        val uri =
            findFileAndGetUriAndQAndAbove("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt")
        if (uri == null) {
            Toast.makeText(context, "\"results.txt\" not found", Toast.LENGTH_SHORT).show();
        } else {
            try {
                content = readFileContent(uri)
            } catch (e: IOException) {
                Toast.makeText(context, "Fail to read file", Toast.LENGTH_SHORT).show()
            }
        }
        return content
    }

    fun readFileContent(uri: Uri): String? {
        var text: String? = ""
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val size = inputStream.available()
                val bytes = ByteArray(size)
                inputStream.read(bytes)
                inputStream.close()
                Log.d("FILE CONTENT READ", bytes.toString())
                text = String(bytes)
            }
        } catch (e: IOException) {
            Toast.makeText(
                context,
                "CANNOT READ FILE - IT IS OPENED BY ANOTHER PROCESS",
                Toast.LENGTH_LONG
            ).show()
            text = null
        }
        return text
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("Range", "Recycle")
    fun findFileAndGetUriAndQAndAbove(filename: String): Uri? {
        val contentUri: Uri = MediaStore.Files.getContentUri("external")
        val selection: String = MediaStore.MediaColumns.RELATIVE_PATH + "=?"
        val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/")
        val cursor: Cursor =
            context.contentResolver.query(contentUri, null, selection, selectionArgs, null)!!
        var uri: Uri? = null
        if (cursor.count == 0) {
            Toast.makeText(
                context,
                "No file found in \"" + Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/\"",
                Toast.LENGTH_LONG
            ).show()
        } else {
            while (cursor.moveToNext()) {
                val fileName: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                if (fileName == filename) {
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    uri = ContentUris.withAppendedId(contentUri, id)
                    Log.d("FILE", "ANDROID Q AND ABOVE URL FOUND SUCCESSFULLY $uri")
                    break
                }
            }
        }
        return uri
    }

    fun getFileUriAndroidBelowQ(filename: String): Uri {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/Sensing requests data/", filename
        ).toUri()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun overwriteFileAndQAndAbove(content: ResultModel) {
        val fileContent = findAndReadFileAndQAndAbove()
        val uri =
            findFileAndGetUriAndQAndAbove("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt")
        if (uri == null) {
            Toast.makeText(context, "\"result.txt\" not found", Toast.LENGTH_SHORT).show();
        } else {
            try {
                overwriteText(fileContent, uri, content)
                Toast.makeText(context, "File over-written successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Fail to write file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun overwriteText(file_content: String?, uri: Uri, content: ResultModel) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri, "rwt")
            val overwriteData = file_content + objectToResultConverter.convertObjectToJson(content)
            outputStream?.let {
                it.write(overwriteData.toByteArray())
                it.close()
                Log.d("FILE", "ANDROID BELOW Q FILE OVERWRITTEN SUCCESFULLY")
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Fail to overwrite file", Toast.LENGTH_LONG).show()
        }

    }

    @SuppressLint("ShowToast")
    fun saveResultToFile(
        questionContent: String,
        result: String,
        sensingRequestAskTime: String,
        timeDisplayQuestionOnScreen: String,
        answerTime: String,
        comment: String,
        sensingRequestId: String
    ) {
        val result = ResultModel(
            questionContent = questionContent,
            result = result,
            askTimeSensingRequest = sensingRequestAskTime,
            answerTime = answerTime,
            comment = comment,
            timeDisplayQuestionOnScreen = timeDisplayQuestionOnScreen,
            sensingRequestId = sensingRequestId
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (findFileAndGetUriAndQAndAbove("${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt") != null) {
                overwriteFileAndQAndAbove(result)
            } else {
                createFileAndQAndAbove(
                    result,
                    "${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt"
                )
            }
        } else {
            if (ifFileExists()) {
                overwriteFile(result)
            } else {
                createTextFileAndWriteDataToItAndroidBelowQ(
                    result,
                    "${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt"
                )
            }
        }
    }

    private fun <T> createTextFileAndWriteDataToItAndroidBelowQ(result: T, filename: String) {
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/Sensing requests data/"
            val file1 = File(dir)
            if (!file1.exists()) {
                file1.mkdirs()
            }
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/Sensing requests data/", filename
            )
            val fos = FileOutputStream(file)
            fos.write(objectToResultConverter.convertObjectToJson(result).toByteArray())
            fos.close()
            Log.d("FILE", "ANDROID BELOW Q FILE CREATED SUCCESFULLY IN $dir")
        } catch (e: IOException) {
            Toast.makeText(context, "FAILED TO CREADTE FILE ANDROID Q AND ABOVE", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun overwriteFile(result: ResultModel) {
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/Sensing requests data/"
            val fw = FileWriter(
                "$dir/${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt",
                true
            )
            fw.append(objectToResultConverter.convertObjectToJson(result))
            fw.close()
            Log.d(
                "FILE",
                "ANDROID BELOW Q FILE OVERWRITTEN SUCCESFULLY IN $dir/${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt"
            )
        } catch (e: IOException) {
            Toast.makeText(context, "CANNOT OVERWRITE FILE", Toast.LENGTH_LONG).show()
        }
    }

    private fun ifFileExists(): Boolean {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/Sensing requests data/",
            "${sharedPrefsProvider.getUserNameFromSharedPrefs()}_results.txt"
        )
        return file.exists()
    }
}