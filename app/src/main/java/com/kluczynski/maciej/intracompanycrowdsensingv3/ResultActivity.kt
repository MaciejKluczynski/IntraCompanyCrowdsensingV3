package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*


class ResultActivity : AppCompatActivity() {

    private var fileManager = FileManager(this)
    private val currentDateProvider = DateManager()
    private val sensingRequestsParser = SensingRequestsParser( SensingRequestsResultFilePathProvider(this),fileManager,currentDateProvider)
    private val firebaseManager = FirebaseManager("TEST")

    companion object{
        const val PERMISSION_WRITE_STORAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        parseSensingRequest()
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseSensingRequest(){
        val question = sensingRequestsParser.findAskedSensingRequest()
        displayQuestion(question)
        activateWhyAskBtn(question.reason)
        activateHintBtn(question.hint)
        activateAddCommentBtn()
        activateTooManyBtn()
        activteDontKnowBtn(question)
        if(question.questionType == "close_ended"){
            createCloseEndedScreen(question)
        } else if (question.questionType == "open"){
            createOpenQuestionScreen(question)
        }
    }

    private fun activateTooManyBtn(){
        val tooManyBtn = findViewById<Button>(R.id.resultActivityTooManyBtn)
        tooManyBtn.setOnClickListener {

        }
    }

    private fun activteDontKnowBtn(question: SensingRequestModel) {
        val dontKnowBtn = findViewById<Button>(R.id.resultActivityDontKnowBtn)
        dontKnowBtn.setOnClickListener {
            saveDataToTxtFile(question.content,"DONT KNOW",question.time,currentDateProvider.getCurrentDate())
        }
    }

    private fun activateWhyAskBtn(reason:String){
        val whyAskBtn = findViewById<Button>(R.id.resultActivityWhyAskBtn)
        whyAskBtn.setOnClickListener {
            val reasonTextView = findViewById<TextView>(R.id.resultActivityWhyAskTextView)
            reasonTextView.visibility = View.VISIBLE
            reasonTextView.text = reason
        }
    }

    private fun activateHintBtn(hint:String){
        val whyAskBtn = findViewById<Button>(R.id.resultActivityGetHintBtn)
        whyAskBtn.setOnClickListener {
            val hintTextView = findViewById<TextView>(R.id.resultActivityGetHintTextView)
            hintTextView.visibility = View.VISIBLE
            hintTextView.text = hint
        }
    }

    private fun activateAddCommentBtn(){
        val addCommentBtn = findViewById<Button>(R.id.resultActivityAddCommentBtn)
        addCommentBtn.setOnClickListener {
            findViewById<EditText>(R.id.resultActivityAddCommentEditText).visibility = View.VISIBLE
        }
    }

    private fun displayQuestion(question: SensingRequestModel){
        val result = findViewById<TextView>(R.id.resultTextView)
        result.text = question.content
    }

    private fun createOpenQuestionScreen(question: SensingRequestModel){
        val editTextNumber = findViewById<EditText>(R.id.editTextNumber)
        editTextNumber.visibility = View.VISIBLE
        val save_btn = findViewById<Button>(R.id.saveBtn)
        save_btn.visibility = View.VISIBLE
        save_btn.setOnClickListener {
            saveDataToTxtFile(question.content, editTextNumber.text.toString(), question.time, currentDateProvider.getCurrentDate())
        }
    }

    private fun createCloseEndedScreen(question: SensingRequestModel){
        activateYesBtn(question)
        activateNoBtn(question)
    }

    private fun activateYesBtn(question: SensingRequestModel){
        val yesBtn = findViewById<Button>(R.id.yes_btn)
        yesBtn.visibility = View.VISIBLE
        yesBtn.setOnClickListener {
            saveDataToTxtFile(question.content, "YES", question.time, currentDateProvider.getCurrentDate())
        }
    }

    private fun activateNoBtn(question: SensingRequestModel){
        val noBtn = findViewById<Button>(R.id.no_btn)
        noBtn.visibility = View.VISIBLE
        noBtn.setOnClickListener {
            saveDataToTxtFile(question.content, "NO", question.time, currentDateProvider.getCurrentDate())
        }
    }

    private fun saveDataToTxtFile(content: String, result: String, ask_time: String, anwser_time: String){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_STORAGE
            )
        }else{
            val text = findViewById<EditText>(R.id.resultActivityAddCommentEditText).text.toString()
            firebaseManager.insertSensingRequestResultIntoDatabase(ResultModel(content, result, ask_time, anwser_time, text))
            fileManager.saveResultToFile(content, result, ask_time, anwser_time, text)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_WRITE_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                parseSensingRequest()
            }
      }
    }

}