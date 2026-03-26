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

    /**
     * Draws a semi-circle with three tick marks:
     * - One near the left end (timestamp1 side)
     * - One near the right end (timestamp2 side)
     * - One at the proportional position of [currentTime] between [timestamp1] and [timestamp2]
     *
     * All time values are in the same unit (e.g. epoch milliseconds). [timestamp2] must be > [timestamp1].
     * If [currentTime] is outside [timestamp1]..[timestamp2] the mark is clamped to the nearest end.
     */
    fun drawTimeOfDayBitmap(currentTime: Long, timestamp1: Long, timestamp2: Long): Bitmap {
        require(timestamp2 > timestamp1) { "timestamp2 must be greater than timestamp1" }

        val strokeWidth = 10f
        val tickLength = 40f
        val drawableWidth = 1366
        val drawableHeight = 768

        return Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(this)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }

            val left = drawableWidth * 0.1f
            val right = drawableWidth * 0.9f
            val top = drawableHeight * 0.1f
            val bottom = drawableHeight * 0.9f

            // Top half arc: starts at 180° (left), sweeps clockwise through 270° (top) to 360° (right)
            canvas.drawArc(RectF(left, top, right, bottom), 180f, 180f, false, paint)

            val centerX = (left + right) / 2
            val centerY = (top + bottom) / 2
            val radiusX = (right - left) / 2
            val radiusY = (bottom - top) / 2

            // Returns the arc point and normalized inward direction at an angle
            fun arcGeometry(angleDegrees: Float): FloatArray {
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                val cos = Math.cos(angleRad).toFloat()
                val sin = Math.sin(angleRad).toFloat()
                val arcX = centerX + radiusX * cos
                val arcY = centerY + radiusY * sin
                val dx = centerX - arcX
                val dy = centerY - arcY
                val len = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                return floatArrayOf(arcX, arcY, dx / len, dy / len)
            }

            // Inward tick from the arc surface
            fun drawTick(angleDegrees: Float) {
                val g = arcGeometry(angleDegrees)
                canvas.drawLine(g[0], g[1], g[0] + g[2] * tickLength, g[1] + g[3] * tickLength, paint)
            }

            // Arrow with tip pointing toward the arc; gap=0 places the tip flush with the arc
            fun drawArrow(angleDegrees: Float, gap: Float = 15f) {
                val g = arcGeometry(angleDegrees)
                val arcX = g[0]; val arcY = g[1]; val nx = g[2]; val ny = g[3]
                val stemLength = 60f
                val wingBack = 18f
                val wingSpread = 9f
                val perpX = -ny
                val perpY = nx
                val tipX = arcX + nx * gap
                val tipY = arcY + ny * gap
                // Stem
                canvas.drawLine(tipX, tipY, tipX + nx * stemLength, tipY + ny * stemLength, paint)
                // Arrowhead wings opening backward from tip
                canvas.drawLine(tipX, tipY, tipX + nx * wingBack + perpX * wingSpread, tipY + ny * wingBack + perpY * wingSpread, paint)
                canvas.drawLine(tipX, tipY, tipX + nx * wingBack - perpX * wingSpread, tipY + ny * wingBack - perpY * wingSpread, paint)
            }

            // Icon center placed outward from the arc by [offset] px
            fun iconCenter(angleDegrees: Float, offset: Float): Pair<Float, Float> {
                val g = arcGeometry(angleDegrees)
                return Pair(g[0] - g[2] * offset, g[1] - g[3] * offset) // outward = negate inward
            }

            // Sunrise: top semicircle above horizon line, rays extending upward
            fun drawSunriseIcon(cx: Float, cy: Float) {
                val r = 20f
                canvas.drawLine(cx - 38f, cy, cx + 38f, cy, paint)
                canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), 180f, 180f, false, paint)
                for (deg in listOf(-90f, -135f, -45f)) {
                    val a = Math.toRadians(deg.toDouble())
                    val rc = Math.cos(a).toFloat(); val rs = Math.sin(a).toFloat()
                    canvas.drawLine(cx + rc * (r + 4f), cy + rs * (r + 4f), cx + rc * (r + 14f), cy + rs * (r + 14f), paint)
                }
            }

            // Sunset: bottom semicircle below horizon line, rays extending downward
            fun drawSunsetIcon(cx: Float, cy: Float) {
                val r = 20f
                canvas.drawLine(cx - 38f, cy, cx + 38f, cy, paint)
                canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), 0f, 180f, false, paint)
                for (deg in listOf(90f, 135f, 45f)) {
                    val a = Math.toRadians(deg.toDouble())
                    val rc = Math.cos(a).toFloat(); val rs = Math.sin(a).toFloat()
                    canvas.drawLine(cx + rc * (r + 4f), cy + rs * (r + 4f), cx + rc * (r + 14f), cy + rs * (r + 14f), paint)
                }
            }

            drawTick(215f)
            val (srX, srY) = iconCenter(215f, 65f)
            drawSunriseIcon(srX, srY)

            drawTick(350f)
            val (ssX, ssY) = iconCenter(350f, 65f)
            drawSunsetIcon(ssX, ssY)

            when {
                currentTime == timestamp1 -> drawArrow(215f, gap = 0f)
                currentTime == timestamp2 -> drawArrow(350f, gap = 0f)
                else -> {
                    val proportion = ((currentTime - timestamp1).toFloat() / (timestamp2 - timestamp1))
                        .coerceIn(0f, 1f)
                    val arrowAngle = 180f + proportion * 180f
                    drawArrow(arrowAngle)

                    val minutesDiff = if (currentTime < timestamp1) {
                        (timestamp1 - currentTime) / 60_000L
                    } else {
                        (timestamp2 - currentTime) / 60_000L
                    }
                    val otherTickAngle = if (currentTime < timestamp1) 215f else 350f
                    val midAngle = (arrowAngle + otherTickAngle) / 2f
                    val mg = arcGeometry(midAngle)
                    val labelX = mg[0] + mg[2] * 80f
                    val labelY = mg[1] + mg[3] * 80f
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                        textSize = 38f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("${minutesDiff}m", labelX, labelY - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)
                }
            }
        }
    }

    fun drawTimeOfDayGraphic(currentTime: Long, timestamp1: Long, timestamp2: Long): ByteArray {
        return bitmapToPngBytes(drawTimeOfDayBitmap(currentTime, timestamp1, timestamp2))
    }

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
    }

}