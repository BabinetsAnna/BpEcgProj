package com.example.ekgappab.bluetoothHandler


object DataKey {
    const val HEART_RATE = "heart_rate"
    const val BP_SYS = "blood_pressure_sys"
    const val BP_DIA = "blood_pressure_dia"

    const val WAVE_DATA = "wave_data"
    const val WAVE_SOURCE = "wave_source"

}

object BleAction {
    const val ACTION_GATT_CONNECTED = "com.example.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.example.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_DATA_AVAILABLE = "com.example.ACTION_DATA_AVAILABLE"
    const val EXTRA_DATA = "com.example.EXTRA_DATA"
}

object DeviceAction {
    const val ACTION_HEART_DATA_AVAILABLE = "com.example.ACTION_HEART_DATA_AVAILABLE"
    const val ACTION_BP2_DATA_AVAILABLE = "com.example.ACTION_BP2_DATA_AVAILABLE"
}