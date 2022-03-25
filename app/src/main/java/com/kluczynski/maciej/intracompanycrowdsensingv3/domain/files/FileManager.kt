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
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.ObjectToResultConverter
import java.io.*

//https://stackoverflow.com/questions/64189667/write-permissions-not-working-scoped-storage-android-sdk-30-aka-android-11
//https://stackoverflow.com/questions/59511147/create-copy-file-in-android-q-using-mediastore
//finding creating and editing files done to Android Q - czyli 10.0
class FileManager(var context: Context) {

    private var objectToResultConverter: ObjectToResultConverter = ObjectToResultConverter()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createFileAndQAndAbove(content: ResultModel) {
        try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "results")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/")
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri!!)
            outputStream!!.write(objectToResultConverter.convertObjectToJson(content).toByteArray())
            outputStream.close()
            Toast.makeText(context, "File created successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Fail to create file", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findAndReadFileAndQAndAbove(): String {
        var content = ""
        val uri = findFileAndGetUriAndQAndAbove()
        if (uri == null) {
            Toast.makeText(context, "\"results.txt\" not found", Toast.LENGTH_SHORT).show();
        } else {
            try {
                content = readFileContent(uri)
                //Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Fail to read file", Toast.LENGTH_SHORT).show()
            }
        }
        return content
    }

    fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val size = inputStream!!.available()
        val bytes = ByteArray(size)
        inputStream.read(bytes)
        inputStream.close()
        Log.d("FILE CONTENT READ", bytes.toString())
        return String(bytes)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("Range", "Recycle")
    fun findFileAndGetUriAndQAndAbove(): Uri? {
        val contentUri: Uri = MediaStore.Files.getContentUri("external")
        val selection: String = MediaStore.MediaColumns.RELATIVE_PATH + "=?"
        val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/")
        val cursor: Cursor = context.contentResolver.query(contentUri, null, selection, selectionArgs, null)!!
        var uri: Uri? = null
        if (cursor.count == 0) {
            Toast.makeText(context, "No file found in \"" + Environment.DIRECTORY_DOCUMENTS + "/Sensing requests data/\"", Toast.LENGTH_LONG).show()
        } else {
            while (cursor.moveToNext()) {
                val fileName: String = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                if (fileName == "results.txt") {
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    uri = ContentUris.withAppendedId(contentUri, id)
                    Log.d("FILE","ANDROID Q AND ABOVE URL FOUND SUCCESSFULLY $uri")
                    break
                }
            }
        }
        return uri
    }

    fun getFileUriAndroidBelowQ():Uri?{
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Sensing requests data/"
        Log.d("FILE","ANDROID BELOW Q URL FOUND SUCCESSFULLY $dir/results.txt")
        return Uri.parse("$dir/results.txt")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun overwriteFileAndQAndAbove(content: ResultModel) {
        val fileContent = findAndReadFileAndQAndAbove()
        val uri = findFileAndGetUriAndQAndAbove()
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

    private fun overwriteText(file_content: String, uri: Uri, content: ResultModel) {
        val outputStream = context.contentResolver.openOutputStream(uri, "rwt")
        val overwriteData = file_content + objectToResultConverter.convertObjectToJson(content)
        outputStream!!.write(overwriteData.toByteArray())
        outputStream.close()
        Log.d("FILE","ANDROID BELOW Q FILE OVERWRITTEN SUCCESFULLY")
    }

    @SuppressLint("ShowToast")
    fun saveResultToFile(content: String, result: String, ask: String, anwser: String, comment:String) {
        val result = ResultModel(content, result, ask, anwser, comment)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            if (findFileAndGetUriAndQAndAbove() != null) {
                //overwrite file
                overwriteFileAndQAndAbove(result)
            } else {
                //create file
                createFileAndQAndAbove(result)
            }
        }else{
            if(ifFileExists()){
                overwriteFile(result)
            }else{
                createTextFileInSpecificLocationAndWriteDataToIt(result)
            }
        }
    }

    private fun createTextFileInSpecificLocationAndWriteDataToIt(result: ResultModel){
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Sensing requests data/"
        val file1 = File(dir)
        if(!file1.exists()){
            file1.mkdirs()
        }
        val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Sensing requests data/", "results.txt")
        val fos = FileOutputStream(file)
        fos.write(objectToResultConverter.convertObjectToJson(result).toByteArray())
        fos.close()
        Log.d("FILE","ANDROID BELOW Q FILE CREATED SUCCESFULLY IN $dir")
    }
    private fun overwriteFile(result: ResultModel){
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Sensing requests data/"
        val fw: FileWriter = FileWriter("$dir/results.txt", true)
        fw.append(objectToResultConverter.convertObjectToJson(result))
        fw.close()
        Log.d("FILE","ANDROID BELOW Q FILE OVERWRITTEN SUCCESFULLY IN $dir/results.txt")
    }

    private fun ifFileExists():Boolean{
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Sensing requests data/", "results.txt")
        return file.exists()
    }
}