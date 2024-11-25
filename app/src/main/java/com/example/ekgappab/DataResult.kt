package com.example.ekgappab

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

data class BPData(
    val bpId: String = "",
    val userId: String = "",
    val heartrate: Int = 0,
    val bpsys: Int = 0,
    val bpdia: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val time: Long = System.currentTimeMillis()
)

data class EcgData(
    val ecgId: String = "",
    val userId: String = "",
    val heartrate: Int = 0,
    val ecgDataArray: List<Float> = emptyList(),
    val date: Long = System.currentTimeMillis(),
    val time: Long = System.currentTimeMillis()
)

class DataResultDB {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    // Функція для збереження даних BP
    fun saveBPData(bpData: BPData) {
        val bpDataRef = database.child("BPData").push()
        bpDataRef.setValue(bpData)

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Дані успішно збережено
                    println("BPData saved successfully.")
                } else {
                    // Обробка помилки
                    println("Error saving BPData: ${task.exception?.message}")
                }
            }
    }

    // Функція для збереження даних ECG
    fun saveEcgData(ecgData: EcgData) {
        val ecgDataRef = database.child("EcgData").push()
        ecgDataRef.setValue(ecgData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Дані успішно збережено
                    println("EcgData saved successfully.")
                } else {
                    // Обробка помилки
                    println("Error saving EcgData: ${task.exception?.message}")
                }
            }
    }

    fun getAllBPDataForUser(userId: String, onDataReceived: (List<BPData>) -> Unit) {
        database.child("BPData").orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val bpDataList = mutableListOf<BPData>()
                for (dataSnapshot in snapshot.children) {
                    val bpData = dataSnapshot.getValue(BPData::class.java)
                    bpData?.let { bpDataList.add(it) }
                }
                onDataReceived(bpDataList)
            }
            .addOnFailureListener { exception ->
                println("Error getting BPData: ${exception.message}")
            }
    }

    fun getAllEcgDataForUser(userId: String, onDataReceived: (List<EcgData>) -> Unit) {
        database.child("EcgData").orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val ecgDataList = mutableListOf<EcgData>()
                for (dataSnapshot in snapshot.children) {
                    val ecgData = dataSnapshot.getValue(EcgData::class.java)
                    ecgData?.let { ecgDataList.add(it) }
                }
                onDataReceived(ecgDataList)
            }
            .addOnFailureListener { exception ->
                println("Error getting EcgData: ${exception.message}")
            }
    }

    // Функція для форматування дати в Firebase timestamp
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    // Функція для конвертації timestamp у читабельний формат
    fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}
