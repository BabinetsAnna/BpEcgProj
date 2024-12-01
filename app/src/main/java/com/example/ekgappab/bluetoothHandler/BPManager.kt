package com.example.ekgappab.bluetoothHandler
import android.app.Service
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.Intent
import android.util.Log


import java.util.*
import kotlin.concurrent.timerTask
import kotlin.experimental.inv

public interface GattManager {
    fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
    fun onUnbind()
}
@SuppressLint("MissingPermission")
@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("OPT_IN_USAGE")
class BPManager(
    private val gatt: BluetoothGatt,
    private val context: Context,

) : GattManager {

    companion object {
        val SERVICE = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        private val WRITE = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
        private val NOTIFY = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")
    }

    private val gattService = gatt.getService(SERVICE)
    private val writeCh: BluetoothGattCharacteristic
        get() = gattService.getCharacteristic(WRITE)
    private val notifyCh: BluetoothGattCharacteristic
        get() = gattService.getCharacteristic(NOTIFY)

    private val timer = Timer()
    private var task: Bp2HRTask? = null

    private var pool: ByteArray? = null

    init {
        gatt.setCharacteristicNotification(notifyCh, true)
        notifyCh.descriptors.firstOrNull()?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
        }
        task = Bp2HRTask()

        timer.scheduleAtFixedRate(task, 0, 1000) //
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        characteristic.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onUnbind() {
    }

    private fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            val len = toUInt(bytes.copyOfRange(i + 5, i + 7)).toInt()

            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = Er1Response(temp)

                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    private fun onResponseReceived(response: Er1Response) {
        when (response.cmd) {
            Bp2BleCmd.RT_DATA -> {
                val rtData = Bp2Response.RtData(response.content)
                val wave = rtData.wave
                val intent = Intent(DeviceAction.ACTION_BP2_DATA_AVAILABLE)

                // Обробка даних кров'яного тиску (систолічний/діастолічний)
                wave.dataBpResult?.let { data ->
                    intent.putExtra(DataKey.HEART_RATE, data.pr)  // Серцевий ритм
                    intent.putExtra(DataKey.BP_SYS, data.sys)     // Систолічний тиск
                    intent.putExtra(DataKey.BP_DIA, data.dia)     // Діастолічний тиск
                    Log.d("DATA", "Blood Pressure: ${data.sys} / ${data.dia}")
                    Log.d("DATA", "HR: ${data.pr}")

                }


                // Обробка ЕКГ даних
                wave.dataEcging?.let { data ->
                    intent.putExtra(DataKey.HEART_RATE, data.hr)  // Серцевий ритм (для ЕКГ)

                    wave.waveFs?.let { wfs ->
                        intent.putExtra(DataKey.WAVE_DATA, wfs)  // Хвилі ЕКГ
                        intent.putExtra(DataKey.WAVE_SOURCE, "Bp2")  // Джерело даних

                    }
                    Log.d("DATA", "ECG Heart Rate: ${data.hr}")
                }

                context.sendBroadcast(intent)
            }
        }
    }

    // Клас для циклічного зчитування даних
    inner class Bp2HRTask : TimerTask() {
        override fun run() {
            writeCh.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            writeCh.value = Bp2BleCmd.getRtData()  // Надсилаємо команду RT_DATA для отримання даних
            gatt.writeCharacteristic(writeCh)
        }
    }
}