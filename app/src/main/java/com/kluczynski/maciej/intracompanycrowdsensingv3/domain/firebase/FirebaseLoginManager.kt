package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseLoginManager(var context: Context) {
    private lateinit var auth: FirebaseAuth
    fun provideUser() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("USER ALREADY LOGGED IN", currentUser.email.toString())
        }else{
            performLogin()
        }
    }

    private fun performLogin() {
        var user:FirebaseUser?
        auth.signInWithEmailAndPassword("icc.examination@gmail.com", "Icc2022!")
                .addOnSuccessListener {
                    user = auth.currentUser
                    user?.let {
                        Log.d("LOGGED IN SUCCESSFULLY",it.email.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d("FAILED TO LOG IN","FAILED TO LOG IN")
                }
    }
}