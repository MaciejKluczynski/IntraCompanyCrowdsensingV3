package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.FileManager
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase.FirebaseLoginManager

class ResultSaver(var context: Context) {

    private val firebaseLoginManager = FirebaseLoginManager(context)
    private val fileManager = FileManager(context)

    fun saveResult(result: ResultModel) {
        if (checkIfPermissionToWriteStorageIsGranted()) {
            saveDataToTextFile(result)
        } else {
            Toast.makeText(
                context,
                "CANNOT SAVE RESULT - WRITE PERMISSION NOT GRANTED",
                Toast.LENGTH_LONG
            ).show()
        }
        authenticateUserAndWriteDataToCloud(result, context)
    }

    private fun authenticateUserAndWriteDataToCloud(result: ResultModel, context: Context) =
        firebaseLoginManager.provideUserAndPerformCloudOperations(result, context)

/*    private fun authenticateUserAndWriteNickToCloud() =
        firebaseLoginManager.provideUserAndUpdateNickToCloud()*/

    fun authenticateUserAndUploadSchedule() =
        firebaseLoginManager.provideUserAndUploadScheduleFile(context)

    private fun saveDataToTextFile(result: ResultModel) {
        fileManager.saveResultToFile(
            questionContent = result.questionContent,
            result = result.result,
            sensingRequestAskTime = result.askTimeSensingRequest,
            answerTime = result.answerTime,
            comment = result.comment,
            sensingRequestId = result.sensingRequestId,
            timeDisplayQuestionOnScreen = result.timeDisplayQuestionOnScreen,
        )
    }

    private fun checkIfPermissionToWriteStorageIsGranted() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}