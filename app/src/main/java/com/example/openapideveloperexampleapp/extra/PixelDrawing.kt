package com.example.openapideveloperexampleapp.extra

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream

class PixelDrawing {

    fun circleBitmap() : Bitmap {

        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
            canvas.drawCircle(0f, 0f, size.toFloat()/2 - 2f, paint)
        }

        return bitmap
    }

    fun crosshairBitmap(): Bitmap{
        val size = 30
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f}
            canvas.drawLine(0f, 0f, -size/2f, size/2f, paint)
            canvas.drawLine(size/2f, -size/2f,0f, 0f, paint)
        }

        return bitmap
    }

    fun toPNGByteArray(bitmap: Bitmap): ByteArray {
        val graphicBytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        return graphicBytes;
    }

    fun drawDemoBitmap(): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
            canvas.drawRect(2f, 2f, size - 2f, size - 2f, paint)
        }

        val graphicBytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        return graphicBytes;
    }

}