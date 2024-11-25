package com.example.ekgappab.bluetoothHandler



fun add(existing: ByteArray?, newBytes: ByteArray): ByteArray {
    return if (existing == null) {
        newBytes
    } else {
        val combined = existing + newBytes  // Об'єднуємо два масиви
        combined
    }
}

fun toUInt(bytes: ByteArray): UInt {
    val size = bytes.size.coerceAtMost(4)  // обмежуємо до 4 байтів
    var result = 0U

    for (i in 0 until size) {
        result = result or ((bytes[i].toUInt() and 0xFFu) shl (8 * i))
    }

    return result
}