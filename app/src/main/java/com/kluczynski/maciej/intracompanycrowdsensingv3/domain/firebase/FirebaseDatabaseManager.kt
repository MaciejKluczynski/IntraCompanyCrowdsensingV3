package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files.SharedPrefsProvider


class FirebaseDatabaseManager(var name: String) {
    private val db = Firebase.firestore

    fun insertSensingRequestResultIntoDatabase(result: ResultModel): Task<DocumentReference> {
        val sensingRequest = hashMapOf(
            "sensingRequestId" to result.sensingRequestId,
            "content" to result.questionContent,
            "askTimeSensingRequest" to result.askTimeSensingRequest,
            "timeDisplayQuestionOnScreen" to result.timeDisplayQuestionOnScreen,
            "result" to result.result,
            "anwserTime" to result.answerTime,
            "comment" to result.comment
        )
        return db.collection(name)
            .add(sensingRequest)
    }

  /*  fun insertUserIdToDatabase(): Task<DocumentReference> {
        val name2 = hashMapOf(
            "userId" to name
        )
        return db.collection(name)
            .add(name2)
    }*/

   /* fun ifCollectionExists() {
        db.collection(name).get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {

            } else {

            }
        }.addOnFailureListener {

        }
    }*/

}