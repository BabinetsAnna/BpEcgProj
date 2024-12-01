package com.example.ekgappab

import android.content.Context
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserSessionManager(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val database: DatabaseReference = FirebaseDatabase
        .getInstance("https://armfitplusapp-default-rtdb.firebaseio.com/")
        .reference



    fun getUserId(): String? {
        return sharedPrefs.getString("userId", null)
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
    }
}
