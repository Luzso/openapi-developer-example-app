package com.example.openapideveloperexampleapp.extra

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream

class PixelDrawing {

    /*fun circleBitmap() : Bitmap {

        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
            //canvas.drawRect(2f, 2f, size - 2f, size - 2f, paint)
            canvas.drawCircle(size.toFloat()/2f, size.toFloat()/2f, size.toFloat()/3f - 2f, paint)
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

    fun bitmapLayersToPNGByteArray(bitmapLayers: List<Bitmap>): ByteArray {
        val size = 128
        val combinedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            for (bitmap in bitmapLayers) {
                canvas.drawBitmap(bitmap, (size - bitmap.width) / 2f, (size - bitmap.height) / 2f, null)
            }
        }

        val graphicBytes = ByteArrayOutputStream().use { out ->
            combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        return graphicBytes;
    }

    fun bitmapToPNGByteArray(bitmap: Bitmap): ByteArray {
        val graphicBytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        return graphicBytes;
    }
*/
    fun drawDemoBitmap(): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(2f, 2f, size - 2f, size - 2f, paint)
        }

        val graphicBytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        return graphicBytes;
    }

}