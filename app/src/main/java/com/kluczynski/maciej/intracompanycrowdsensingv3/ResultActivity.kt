package com.kluczynski.maciej.intracompanycrowdsensingv3

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.*
import androidx.appcompat.app.AlertDialog


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
        val buttonOption5 = intent?.getStringExtra("buttonOption5")

        val timeDisplayQuestionOnScreen = currentDateProvider.getCurrentDate()
        displayQuestion(questionContent!!)
        activateWhyAskBtn(questionWhyAsk!!)
        activateHintBtn(questionHint!!)
        activateAddCommentBtn()
        val questionTimeString =
            DateManager().getSimpleDateFormat().parse(questionTime!!)!!.toString()
        activateDontKnowBtn(
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
            if(buttonOption5!=null){
                activateFifthBtn(
                    content = questionContent,
                    sensingRequestTime = questionTimeString,
                    realAskTime = timeDisplayQuestionOnScreen,
                    sensingRequestId = sensingRequestId,
                    buttonText = buttonOption5
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


    private fun activateDontKnowBtn(
        sensingRequestAskTime: String,
        questionContent: String,
        timeDisplayQuestionOnScreen: String,
        sensingRequestId: String
    ) {
        val dontKnowBtn = findViewById<Button>(R.id.resultActivityDontKnowBtn)
        dontKnowBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
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
            createAlertDialog("Why ask", reason)
        }
    }

    private fun activateHintBtn(hint: String) {
        val whyAskBtn = findViewById<Button>(R.id.resultActivityGetHintBtn)
        whyAskBtn.setOnClickListener {
            createAlertDialog("Hint",hint)
        }
    }

    private fun createAlertDialog(title:String, message:String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val appCompatDialog: AppCompatDialog = builder.create()
        appCompatDialog.show()
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
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        showResultEditText()
        showSaveBtn()
        deactivateCloseEndedButtons()
        saveBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
                content = content,
                result = editTextNumber.text.toString(),
                timeDisplayQuestionOnScreen = realAskTime,
                sensingRequestAskTime = sensingRequestAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun deactivateCloseEndedButtons(){
        hideFirstBtn()
        hideSecondBtn()
        hideThirdBtn()
        hideForthBtn()
        hideFifthBtn()
    }

    private fun hideFirstBtn(){
        val firstBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutFirstBtn)
        val params = firstBtnLinearLayout.layoutParams
        params.height = 0
    }

    private fun hideSecondBtn(){
        val secondBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutSecondBtn)
        val params = secondBtnLinearLayout.layoutParams
        params.height = 0
    }

    private fun hideThirdBtn(){
        val thirdBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutThirdBtn)
        val params = thirdBtnLinearLayout.layoutParams
        params.height = 0
    }

    private fun hideForthBtn(){
        val forthBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutForthBtn)
        val params = forthBtnLinearLayout.layoutParams
        params.height = 0
    }

    private fun hideFifthBtn(){
        val fifthBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutFifthBtn)
        val params = fifthBtnLinearLayout.layoutParams
        params.height = 0
    }

    private fun createCloseEndedScreen() {
        deactivateCloseEndedButtons()
        hideSaveBtn()
        hideResultEditText()
    }

    private fun hideResultEditText(){
        val linearLayoutSaveBtn = findViewById<LinearLayout>(R.id.linearLayoutResultEditText)
        val params = linearLayoutSaveBtn.layoutParams
        params.height = 0
    }

    private fun showResultEditText(){
        val linearLayoutSaveBtn = findViewById<LinearLayout>(R.id.linearLayoutResultEditText)
        val params = linearLayoutSaveBtn.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT
    }

    private fun hideSaveBtn(){
        val linearLayoutSaveBtn = findViewById<LinearLayout>(R.id.linearLayoutSaveBtn)
        val params = linearLayoutSaveBtn.layoutParams
        params.height = 0
    }

    private fun showSaveBtn(){
        val linearLayoutSaveBtn = findViewById<LinearLayout>(R.id.linearLayoutSaveBtn)
        val params = linearLayoutSaveBtn.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT
    }

    private fun activateFirstBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText: String
    ) {
        val firstBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutFirstBtn)
        val params = firstBtnLinearLayout.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        val firstBtn= findViewById<Button>(R.id.firstBtn)
        firstBtn.text = buttonText
        firstBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
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
        buttonText: String
    ) {
        val secondBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutSecondBtn)
        val params = secondBtnLinearLayout.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        val secondBtn = findViewById<Button>(R.id.secondBtn)
        secondBtn.text = buttonText
        secondBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
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
        buttonText: String
    ) {
        val thirdBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutThirdBtn)
        val params = thirdBtnLinearLayout.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        val thirdBtn = findViewById<Button>(R.id.thirdBtn)
        thirdBtn.text = buttonText
        thirdBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
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
        buttonText: String
    ) {
        val forthBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutForthBtn)
        val params = forthBtnLinearLayout.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        val forthBtn = findViewById<Button>(R.id.forthBtn)
        forthBtn.text = buttonText
        forthBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
                content = content,
                result = forthBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun activateFifthBtn(
        content: String,
        sensingRequestTime: String,
        realAskTime: String,
        sensingRequestId: String,
        buttonText: String
    ) {
        val fifthBtnLinearLayout = findViewById<LinearLayout>(R.id.linearLayoutFifthBtn)
        val params = fifthBtnLinearLayout.layoutParams
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        val fifthBtn = findViewById<Button>(R.id.fifthBtn)
        fifthBtn.text = buttonText
        fifthBtn.setOnClickListener {
            saveDataToTxtFileAndCloud(
                content = content,
                result = fifthBtn.text.toString(),
                sensingRequestAskTime = sensingRequestTime,
                timeDisplayQuestionOnScreen = realAskTime,
                answerTime = currentDateProvider.getCurrentDate(),
                sensingRequestId = sensingRequestId
            )
        }
    }

    private fun saveDataToTxtFileAndCloud(
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