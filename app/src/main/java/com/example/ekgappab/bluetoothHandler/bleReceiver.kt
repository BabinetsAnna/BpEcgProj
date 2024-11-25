package com.example.ekgappab.bluetoothHandler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData

class BleDataReceiver : BroadcastReceiver() {

    companion object {
        // Статичні LiveData для спостереження
        val heartRateLive = MutableLiveData<Int>()
        val ecgDataLive = MutableLiveData<FloatArray?>()
        val bpSysLive = MutableLiveData<Int?>()
        val bpDiaLive = MutableLiveData<Int?>()

    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == DeviceAction.ACTION_HEART_DATA_AVAILABLE || action == DeviceAction.ACTION_BP2_DATA_AVAILABLE) {
            // Отримуємо дані з інтенту і оновлюємо LiveData
            val heartRate = intent.getIntExtra(DataKey.HEART_RATE, 0)
            val waveData = intent.getFloatArrayExtra(DataKey.WAVE_DATA)
            val bpSysLevel = intent.getIntExtra(DataKey.BP_SYS, 0)
            val bpDiaLevel = intent.getIntExtra(DataKey.BP_DIA, 0)


            // Оновлюємо LiveData для спостереження
            heartRateLive.postValue(heartRate)
            ecgDataLive.postValue(waveData)
            bpSysLive.postValue(bpSysLevel)
            bpDiaLive.postValue(bpDiaLevel)


            // Додаткове оброблення для хвильових даних
            intent.getStringExtra(DataKey.WAVE_SOURCE)?.let { source ->
                if (source == "Bp2") {
                    waveData?.let {
                        Er1DataController.receive(it)
                    }
                }
            }
        }
    }
}
