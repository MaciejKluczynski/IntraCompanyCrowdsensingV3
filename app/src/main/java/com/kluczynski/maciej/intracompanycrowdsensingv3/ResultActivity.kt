package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*


class ResultActivity : AppCompatActivity() {

    private val currentDateProvider = DateManager()

    companion object {
        const val PERMISSION_WRITE_STORAGE_ID_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        parseSensingRequest(intent)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseSensingRequest(intent)
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseSensingRequest(intent: Intent?) {
        val questionContent = intent?.getStringExtra("Content")
        val questionHint = intent?.getStringExtra("Hint")
        val questionType = intent?.getStringExtra("QuestionType")
        val questionWhyAsk = intent?.getStringExtra("WhyAsk")
        val questionTime = intent?.getStringExtra("QuestionTime")
        val sensingRequestId = intent?.getStringExtra("Id")
        val buttonOption1 = intent?.getStringExtra("buttonOption1")
        val buttonOption2 = intent?.getStringExtra("buttonOption2")
        val buttonOption3 = intent?.getStringExtra("buttonOption3")
        val buttonOption4 = intent?.getStringExtra("buttonOption4")
        val timeDisplayQuestionOnScreen = currentDateProvider.getCurrentDate()
        displayQuestion(questionContent!!)
        activateWhyAskBtn(questionWhyAsk!!)
        activateHintBtn(questionHint!!)
        activateAddCommentBtn()
        val questionTimeString =
            DateManager().getSimpleDateFormat().parse(questionTime!!)!!.toString()
        activteDontKnowBtn(
            sensingRequestAskTime = questionTimeString,
            questionContent = questionContent,
            timeDisplayQuestionOnScreen = timeDisplayQuestionOnScreen,
            sensingRequestId = sensingRequestId!!
        )
        if (questionType == "close_ended") {
            createCloseEndedScreen()
            if(buttonOption1!=null){
                activateFirstBtn(
                    content = questionContent,
                    sensingRequestTime = questionTimeString,
                    realAskTime = timeDisplayQuestionOnScreen,
                    sensingRequestId = sensingRequestId,
                    buttonText = buttonOption1
                )
            }
            if(buttonOption2!=null){
                activateSecondBtn(
                    content = questionContent,
                    sensingRequestTime = questionTimeString,
                    realAskTime = timeDisplayQuestionOnScreen,
                    sensingRequestId = sensingRequestId,
                    buttonText = buttonOption2
                )
            }
            if(buttonOption3!=null){
                activateThirdBtn(
                    content = questionContent,
                    sensingRequestTime = questionTimeString,
                    realAskTime = timeDisplayQuestionOnScreen,
                    sensingRequestId = sensingRequestId,
                    buttonText = buttonOption3
                )
            }
            if(buttonOption4!=null){
                activateForthBtn(
                    content = questionContent,
                    sensingRequestTime = questionTimeString,
                    realAskTime = timeDisplayQuestionOnScreen,
                    sensingRequestId = sensingRequestId,
                    buttonText = buttonOption4
                )
            }

        } else if (questionType == "numerical") {
            createOpenQuestionScreen(
                content = questionContent,
                sensingRequestAskTime = questionTimeString,
                realAskTime = timeDisplayQuestionOnScreen,
                sensingRequestId = sensingRequestId
            )
        }
    }


    private fun activteDontKnowBtn(
        sensingRequestAskTime: String,
        questionContent: String,
        timeDisplayQuestionOnScreen: String,
        sensingRequestId: String
    ) {
        val dontKnowBtn = findViewById<Button>(R.id.resultActivityDontKnowBtn)
        dontKnowBtn.setOnClickListener {
            saveDataToTxtFile(
                content = questionContent,
                result = "DONT KNOW",
                sensingRequestAskTime = sensingRequestAskTime,
                timeDisplayQuestionOnScreen = timeDisplayQuestionOnScreen,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun activateWhyAskBtn(reason: String) {
        val whyAskBtn = findViewById<Button>(R.id.resultActivityWhyAskBtn)
        whyAskBtn.setOnClickListener {
            val reasonTextView = findViewById<TextView>(R.id.resultActivityWhyAskTextView)
            reasonTextView.text = reason
            reasonTextView.visibility =
                if (reasonTextView.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun activateHintBtn(hint: String) {
        val whyAskBtn = findViewById<Button>(R.id.resultActivityGetHintBtn)
        whyAskBtn.setOnClickListener {
            val hintTextView = findViewById<TextView>(R.id.resultActivityGetHintTextView)
            hintTextView.text = hint
            hintTextView.visibility =
                if (hintTextView.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun activateAddCommentBtn() {
        val addCommentBtn = findViewById<Button>(R.id.resultActivityAddCommentBtn)
        addCommentBtn.setOnClickListener {
            val addCommentField = findViewById<EditText>(R.id.resultActivityAddCommentEditText)
            addCommentField.visibility =
                if (addCommentField.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun displayQuestion(questionContent: String) {
        val result = findViewById<TextView>(R.id.resultTextView)
        result.text = questionContent
    }

    private fun createOpenQuestionScreen(
        content: String,
        sensingRequestAskTime: String,
        realAskTime: String,
        sensingRequestId: String
    ) {
        val editTextNumber = findViewById<EditText>(R.id.editTextNumber)
        editTextNumber.visibility = View.VISIBLE
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.visibility = View.VISIBLE
        val firstBtn = findViewById<Button>(R.id.firstBtn)
        firstBtn.visibility = View.INVISIBLE
        val secondBtn = findViewById<Button>(R.id.secondBtn)
        secondBtn.visibility = View.INVISIBLE
        val thirdBtn = findViewById<Button>(R.id.thirdBtn)
        thirdBtn.visibility = View.INVISIBLE
        val forthBtn = findViewById<Button>(R.id.forthBtn)
        forthBtn.visibility = View.INVISIBLE
        saveBtn.setOnClickListener {
            saveDataToTxtFile(
                content = content,
                result = editTextNumber.text.toString(),
                timeDisplayQuestionOnScreen = realAskTime,
                sensingRequestAskTime = sensingRequestAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun createCloseEndedScreen() {
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.visibility = View.INVISIBLE
        val editTextField = findViewById<EditText>(R.id.editTextNumber)
        editTextField.visibility = View.INVISIBLE
    }

    private fun activateFirstBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText:String
    ) {
        val firstBtn = findViewById<Button>(R.id.firstBtn)
        firstBtn.visibility = View.VISIBLE
        firstBtn.text = buttonText
        firstBtn.setOnClickListener {
            saveDataToTxtFile(
                content = content,
                result = firstBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun activateSecondBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText:String
    ) {
        val secondBtn = findViewById<Button>(R.id.secondBtn)
        secondBtn.visibility = View.VISIBLE
        secondBtn.text = buttonText
        secondBtn.setOnClickListener {
            saveDataToTxtFile(
                content = content,
                result = secondBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun activateThirdBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText:String
    ) {
        val thirdBtn = findViewById<Button>(R.id.thirdBtn)
        thirdBtn.visibility = View.VISIBLE
        thirdBtn.text = buttonText
        thirdBtn.setOnClickListener {
            saveDataToTxtFile(
                content = content,
                result = thirdBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun activateForthBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText:String
    ) {
        val forthBtn = findViewById<Button>(R.id.forthBtn)
        forthBtn.visibility = View.VISIBLE
        forthBtn.text = buttonText
        forthBtn.setOnClickListener {
            saveDataToTxtFile(
                content = content,
                result = forthBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun saveDataToTxtFile(
        content: String,
        result: String,
        sensingRequestAskTime: String,
        answerTime: String,
        timeDisplayQuestionOnScreen: String,
        sensingRequestId: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_STORAGE_ID_CODE
            )
        } else {
            val text = findViewById<EditText>(R.id.resultActivityAddCommentEditText).text.toString()
            val resultSaver = ResultSaver(this)
            resultSaver.saveResult(
                ResultModel(
                    questionContent = content,
                    result = result,
                    askTimeSensingRequest = sensingRequestAskTime,
                    timeDisplayQuestionOnScreen = timeDisplayQuestionOnScreen,
                    answerTime = answerTime,
                    comment = text,
                    sensingRequestId = sensingRequestId
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_WRITE_STORAGE_ID_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION GRANTED TRY SAVING AGAIN", Toast.LENGTH_LONG)
                    .show()
                parseSensingRequest(intent)
            } else {
                Toast.makeText(this, "YOU MUST PROVIDE PERMISSIONS TO STORAGE", Toast.LENGTH_LONG)
                    .show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_STORAGE_ID_CODE
                )
            }
        }
    }

}