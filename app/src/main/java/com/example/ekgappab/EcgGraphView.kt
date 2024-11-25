package com.example.ekgappab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class EcgGraphView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var ecgData: FloatArray = floatArrayOf()



    init {
        paint.color = Color.BLACK
        paint.strokeWidth = width / 1250f
        paint.isAntiAlias = true
    }

    // Метод для оновлення даних
    fun updateData(newData: FloatArray) {
        ecgData = if (newData.size > 1250) {
            newData.takeLast(1250).toFloatArray()
        } else {
            newData
        }
        Log.d("EcgGraphView", "Data updated with ${ecgData.size} points.")
        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (ecgData.isEmpty()) return

        val xSpacing = width / 1250f
        val yMid = height / 2

        for (i in 1 until ecgData.size) {
            val startX = (i - 1) * xSpacing
            val startY = yMid - (ecgData[i - 1] * 100) // Збільшуємо масштаб для осі Y
            val stopX = i * xSpacing
            val stopY = yMid - (ecgData[i] * 100)

            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }
    }
}