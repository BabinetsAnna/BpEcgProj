package com.example.ekgappab.bluetoothHandler

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

interface ScanListener {
    fun onScanStarted()
    fun onScanStopped()
}

interface StateListener {
    fun onConnected()
    fun onDisconnected()
}

class BLEManager(
    private val context: Context,
    private val scanListener: ScanListener,
    private val stateListener: StateListener
) {
    companion object {
        private const val TAG = "BLEManager"
        private const val TARGET_MAC_ADDRESS = "CA:02:ED:31:61:32"
        val SERVICE_UUID: UUID = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        val WRITE_UUID: UUID = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        val NOTIFY_UUID: UUID = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null
    private var bpManager: BPManager? = null

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth адаптер недоступний.")
            return
        }

        // Перевіряємо, чи увімкнений Bluetooth
        if (!bluetoothAdapter.isEnabled) {
            Log.i(TAG, "Bluetooth вимкнений. Спроба увімкнути.")
            bluetoothAdapter.enable()

            // Перевірка стану після увімкнення
            var attempts = 0
            while (!bluetoothAdapter.isEnabled && attempts < 10) {
                Thread.sleep(500) // Затримка для перевірки
                attempts++
            }

            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Не вдалося увімкнути Bluetooth.")
                return
            }
        }

        val device = bluetoothAdapter.getRemoteDevice(TARGET_MAC_ADDRESS)
        if (device != null) {
            connectToDevice(device)
            scanListener.onScanStarted()
        } else {
            Log.e(TAG, "Пристрій з MAC-адресою $TARGET_MAC_ADDRESS не знайдено.")
        }
    }


    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                stateListener.onConnected()
                Log.d(TAG, "Пристрій підключено, виявлення сервісів...")
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                stateListener.onDisconnected()
                Log.d(TAG, "Пристрій відключено")
                bluetoothGatt?.close()
                bluetoothGatt = null
                bpManager = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Сервіси виявлено")

                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(WRITE_UUID)
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        // Ініціалізуємо BPManager для подальшої обробки характеристик
                        bpManager = BPManager(gatt, context)
                        onCharacteristicChanged(gatt, characteristic)
                    } else {
                        Log.e(TAG, "Характеристика WRITE не знайдена в сервісі.")
                    }
                } else {
                    Log.e(TAG, "Сервіс не знайдений.")
                }
            } else {
                Log.e(TAG, "Помилка виявлення сервісів: статус $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == NOTIFY_UUID) {
                bpManager?.onCharacteristicChanged(gatt, characteristic)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        bpManager = null
    }
}
