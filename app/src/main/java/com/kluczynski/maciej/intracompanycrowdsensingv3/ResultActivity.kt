package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class ResultActivity : AppCompatActivity() {

    private val currentDateProvider = DateManager()

    companion object {
        const val PERMISSION_WRITE_STORAGE = 101
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        parseSensingRequest()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SimpleDateFormat")
    private fun parseSensingRequest() {
        val questionContent = intent.getStringExtra("Content")
        val questionHint = intent.getStringExtra("Hint")
        val questionType = intent.getStringExtra("QuestionType")
        val questionWhyAsk = intent.getStringExtra("WhyAsk")
        val questionTime = intent.getStringExtra("QuestionTime")
        displayQuestion(questionContent!!)
        activateWhyAskBtn(questionWhyAsk!!)
        activateHintBtn(questionHint!!)
        activateAddCommentBtn()
        activateTooManyBtn()
        val questionTimeString = DateManager().getSimpleDateFormat().parse(questionTime!!)!!.toString()
        activteDontKnowBtn(
            questionTimeString,
            questionContent
        )
        if (questionType == "close_ended") {
            createCloseEndedScreen(content = questionContent, time = questionTimeString)
        } else if (questionType == "numerical") {
            createOpenQuestionScreen(content = questionContent, time = questionTimeString)
        }
    }

    private fun activateTooManyBtn() {
        val tooManyBtn = findViewById<Button>(R.id.resultActivityTooManyBtn)
        tooManyBtn.setOnClickListener {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun activteDontKnowBtn(questionTime: String, questionContent: String) {
        val dontKnowBtn = findViewById<Button>(R.id.resultActivityDontKnowBtn)
        dontKnowBtn.setOnClickListener {
            saveDataToTxtFile(
                questionContent,
                "DONT KNOW",
                questionTime,
                currentDateProvider.getCurrentDate()
            )
        }
    }

    private fun activateWhyAskBtn(reason: String) {
        val whyAskBtn = findViewById<Button>(R.id.resultActivityWhyAskBtn)
        whyAskBtn.setOnClickListener {
            val reasonTextView = findViewById<TextView>(R.id.resultActivityWhyAskTextView)
            reasonTextView.visibility = View.VISIBLE
            reasonTextView.text = reason
        }
    }

    private fun activateHintBtn(hint: String) {
        val whyAskBtn = findViewById<Button>(R.id.resultActivityGetHintBtn)
        whyAskBtn.setOnClickListener {
            val hintTextView = findViewById<TextView>(R.id.resultActivityGetHintTextView)
            hintTextView.visibility = View.VISIBLE
            hintTextView.text = hint
        }
    }

    private fun activateAddCommentBtn() {
        val addCommentBtn = findViewById<Button>(R.id.resultActivityAddCommentBtn)
        addCommentBtn.setOnClickListener {
            findViewById<EditText>(R.id.resultActivityAddCommentEditText).visibility = View.VISIBLE
        }
    }

    private fun displayQuestion(questionContent: String) {
        val result = findViewById<TextView>(R.id.resultTextView)
        result.text = questionContent
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createOpenQuestionScreen(content: String, time: String) {
        val editTextNumber = findViewById<EditText>(R.id.editTextNumber)
        editTextNumber.visibility = View.VISIBLE
        val save_btn = findViewById<Button>(R.id.saveBtn)
        save_btn.visibility = View.VISIBLE
        val yesBtn = findViewById<Button>(R.id.yes_btn)
        yesBtn.visibility = View.GONE
        val noBtn = findViewById<Button>(R.id.no_btn)
        noBtn.visibility = View.GONE
        save_btn.setOnClickListener {
            saveDataToTxtFile(
                content,
                editTextNumber.text.toString(),
                time,
                currentDateProvider.getCurrentDate()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createCloseEndedScreen(content:String, time:String) {
        activateYesBtn(content = content, time = time)
        activateNoBtn(content = content, time = time)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun activateYesBtn(content:String, time:String) {
        val yesBtn = findViewById<Button>(R.id.yes_btn)
        yesBtn.visibility = View.VISIBLE
        yesBtn.setOnClickListener {
            saveDataToTxtFile(
                content,
                "YES",
                time,
                currentDateProvider.getCurrentDate()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun activateNoBtn(content:String, time:String) {
        val noBtn = findViewById<Button>(R.id.no_btn)
        noBtn.visibility = View.VISIBLE
        noBtn.setOnClickListener {
            saveDataToTxtFile(
                content,
                "NO",
                time,
                currentDateProvider.getCurrentDate()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveDataToTxtFile(
        content: String,
        result: String,
        ask_time: String,
        anwser_time: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_STORAGE
            )
        } else {
            val text = findViewById<EditText>(R.id.resultActivityAddCommentEditText).text.toString()
            val resultSaver = ResultSaver(this)
            resultSaver.saveResult(ResultModel(content, result, ask_time, anwser_time, text))
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION GRANTED TRY SAVING AGAIN", Toast.LENGTH_LONG)
                    .show()
                parseSensingRequest()
            } else {
                Toast.makeText(this, "YOU MUST PROVIDE PERMISSIONS TO STORAGE", Toast.LENGTH_LONG)
                    .show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_STORAGE
                )
            }
        }
    }

}