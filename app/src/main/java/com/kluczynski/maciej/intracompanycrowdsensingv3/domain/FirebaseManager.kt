package com.kluczynski.maciej.intracompanycrowdsensingv3.domain
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.ResultModel


class FirebaseManager(name:String) {
    private val db = Firebase.firestore

    fun insertSensingRequestResultIntoDatabase(result: ResultModel){
        val sensingRequest = hashMapOf(
            "content" to result.content.toString(),
            "ask_time" to result.ask_time.toString(),
            "result" to result.result.toString(),
            "anwser_time" to result.anwser_time.toString(),
            "comment" to result.comment.toString()
        )
        db.collection("TEST")
            .add(sensingRequest)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

}