package com.example.rcp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class CircleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val linePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 6f
    }

    private val centerCirclePaint = Paint().apply {
        color = Color.WHITE
    }

    private val movingCirclePaint = Paint().apply {
        color = Color.CYAN
    }

    private var circles = mutableListOf<Float>()
    private var circleSpeed = 10f
    private var mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.beep)
    private var frame = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val centerX = width / 2f

        // Línea horizontal
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, linePaint)

        // Círculo central
        canvas.drawCircle(centerX, centerY, 40f, centerCirclePaint)

        // Dibujar y mover círculos
        val iterator = circles.listIterator()
        while (iterator.hasNext()) {
            val x = iterator.next() + circleSpeed
            if (x > width + 50) {
                iterator.remove()
            } else {
                canvas.drawCircle(x, centerY, 30f, movingCirclePaint)
                iterator.set(x)

                // Si pasa por el centro → sonar
                if (abs(x - centerX) < 5) {
                    mediaPlayer.start()
                }
            }
        }

        // Agregar nuevo círculo cada segundo aprox
        frame++
        if (frame % 60 == 0) {
            circles.add(-50f)
        }

        postInvalidateOnAnimation()
    }
}