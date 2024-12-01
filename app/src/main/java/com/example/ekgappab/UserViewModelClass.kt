package com.example.ekgappab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class UserViewModelClass : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase
        .getInstance("https://armfitplusapp-default-rtdb.firebaseio.com/").reference
    private val userData = MutableLiveData<Map<String, String>?>()

    fun getUserData(): LiveData<Map<String, String>?> {
        return userData
    }

    fun loadUserData(userId: String) {
        database.child("users").child(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val userMap = dataSnapshot.value as? Map<*, *>
                    userData.postValue(
                        mapOf(
                            "surname" to (userMap?.get("surname") as? String ?: ""),
                            "firstName" to (userMap?.get("firstName") as? String ?: ""),
                            "email" to (userMap?.get("email") as? String ?: "")
                        )
                    )
                } else {
                    userData.postValue(null)
                }
            } else {
                userData.postValue(null)
            }
        }
    }

    fun updateUserField(userId: String, field: String, value: String, callback: (Boolean) -> Unit) {
        val updates = mapOf(field to value)
        database.child("users").child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadUserData(userId)  // Перезавантаження даних після оновлення
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }
}
