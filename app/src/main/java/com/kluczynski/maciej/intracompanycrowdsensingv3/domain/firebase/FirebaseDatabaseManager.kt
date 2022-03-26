package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel


class FirebaseDatabaseManager(var name:String) {
    private val db = Firebase.firestore

    fun insertSensingRequestResultIntoDatabase(result: ResultModel): Task<DocumentReference> {
        val sensingRequest = hashMapOf(
            "content" to result.content,
            "ask_time" to result.ask_time,
            "result" to result.result,
            "anwser_time" to result.answer_time,
            "comment" to result.comment
        )
        return db.collection(name)
            .add(sensingRequest)

    }

}