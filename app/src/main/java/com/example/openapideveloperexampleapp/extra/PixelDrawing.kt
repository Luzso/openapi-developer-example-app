package com.example.openapideveloperexampleapp.extra

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.io.ByteArrayOutputStream

// Swarovski documentation on OpenAPI rendering pixels on the device:
// The Outside App can also draw arbitrary graphics on the AX Visio using the RenderPixelGraphic topic. There are two graphics slot in the AX Visio. One slot for a rotated image and one slot for a non-rotated image.
//
//If the Outside App sends a new graphic before the duration of the old graphic is elapsed, the new graphic replaces the old one.
//
//The Outside App's image is not scaled. It's drawn at the same size on the AX Visio. The current drawable resolution of the AX Visio display is 1366x768. Furthermore the image is drawn at the center of the AX Visio screen, which is the important visual view area for a user. So the image does not have to be the same resolution as the AX Visio screen. It can be smaller and only cover the area that the Outside App wants to draw in the center of the view. The border of the screen is hard to see and recognizable by the user.
//
//The image can be grayscale. It should mostly only use white and transparency. Black is not visible on the display. RGB images work technically, too, but are shown as grayscale on the monochrome AX Visio display.

class PixelDrawing {

    fun drawDemoBitmap(): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(2f, 2f, size - 2f, size - 2f, paint)
        }

        return bitmapToPngBytes(bitmap)
    }

    fun drawTopHalfSemiCircleBitmap(): Bitmap {
        val strokeWidth: Float = 10f
        val color: Int = Color.WHITE
        val drawableWidth = 1366
        val drawableHeight = 768
        val bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }

            val left = drawableWidth * 0.1f
            val right = drawableWidth * 0.9f
            val top = drawableHeight * 0.1f
            val bottom = drawableHeight * 0.9f
            val oval = RectF(left, top, right, bottom)

            canvas.drawArc(oval, 180f, 180f, false, paint)
        }

        return bitmap
    }

    fun drawTopHalfSemiCircleGraphic(): ByteArray {
        val bitmap = drawTopHalfSemiCircleBitmap()
        return bitmapToPngBytes(bitmap)
    }

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
    }

}