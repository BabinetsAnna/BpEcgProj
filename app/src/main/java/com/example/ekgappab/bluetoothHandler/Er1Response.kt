package com.example.ekgappab.bluetoothHandler

import android.os.Parcelable
import android.util.Log
import com.example.ekgappab.bluetoothHandler.Er1DataController

import kotlinx.android.parcel.Parcelize



@ExperimentalUnsignedTypes
@Parcelize
class Er1Response(var bytes: ByteArray) : Parcelable {
    var cmd: Int
    var pkgType: Byte
    var pkgNo: Int
    var len: Int
    var content: ByteArray

    init {
        cmd = (bytes[1].toUInt() and 0xFFu).toInt()
        pkgType = bytes[3]
        pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
        len = toUInt(bytes.copyOfRange(5, 7)).toInt()
        content = bytes.copyOfRange(7, 7+len)

    }
}


@Parcelize
@ExperimentalUnsignedTypes
class RtData(var bytes: ByteArray) : Parcelable {
    var content: ByteArray = bytes
    var param: RtParam
    var wave: RtWave

    init {
        param = RtParam(bytes.copyOfRange(0, 20))
        wave = RtWave(bytes.copyOfRange(20, bytes.size))
    }
}

@ExperimentalUnsignedTypes
@Parcelize
class RtParam(var bytes: ByteArray) : Parcelable {
    var hr: Int
    var sysFlag: Byte
    var battery: Int
    var recordTime: Int = 0
    var runStatusByte: Byte
    var leadOn: Boolean

    init {
        hr = toUInt(bytes.copyOfRange(0, 2)).toInt()
        sysFlag = bytes[2]
        battery = (bytes[3].toUInt() and 0xFFu).toInt()
        if (bytes[8].toUInt() and 0x02u == 0x02u) {
            recordTime = toUInt(bytes.copyOfRange(4, 8)).toInt()
        }
        runStatusByte = bytes[8]
        leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
    }
}

@Parcelize
@ExperimentalUnsignedTypes
class RtWave(var bytes: ByteArray) : Parcelable {
    var content: ByteArray = bytes
    var len: Int
    var wave: ByteArray
    var wFs : FloatArray? = null

    init {
        len = toUInt(bytes.copyOfRange(0, 2)).toInt()
        wave = bytes.copyOfRange(2, bytes.size)
        wFs = FloatArray(len)
        for (i in 0 until len) {
            wFs!![i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
        }
    }
}