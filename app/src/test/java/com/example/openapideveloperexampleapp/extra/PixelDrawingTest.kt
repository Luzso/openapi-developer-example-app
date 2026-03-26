package com.example.openapideveloperexampleapp.extra

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PixelDrawingTest {

    // Composites a Bitmap onto a black background so white-on-transparent
    // drawings are visible when previewing PNGs outside the device.
    // Uses direct pixel manipulation to avoid Robolectric Canvas shadow limitations.
    private fun writeBitmapPreview(name: String, src: Bitmap) {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        // Replace transparent pixels with black, ensure others are fully opaque
        for (i in pixels.indices) {
            pixels[i] = if (Color.alpha(pixels[i]) == 0) Color.BLACK
                        else pixels[i] or 0xFF000000.toInt()
        }

        val preview = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        val out = java.io.ByteArrayOutputStream()
        preview.compress(Bitmap.CompressFormat.PNG, 100, out)
        writePngArtifact(name, out.toByteArray())
    }

    private fun writePngArtifact(name: String, pngBytes: ByteArray) {
        // Written under app/build/... which is already ignored by this repo's .gitignore (/build).
        val outDir = File("build/outputs/pixel-drawing-tests").apply { mkdirs() }
        val outFile = File(outDir, name)
        FileOutputStream(outFile).use { it.write(pngBytes) }

        // Useful breadcrumbs if you run tests from the IDE.
        assertTrue("Expected PNG artifact to be written: ${outFile.absolutePath}", outFile.exists())
        assertTrue("Expected PNG artifact to be non-empty: ${outFile.absolutePath}", outFile.length() > 0)
    }

    @Test
    fun drawTopHalfSemiCircle_producesPngArtifact() {
        val drawing = PixelDrawing()

        val bitmap = drawing.drawTopHalfSemiCircleBitmap()
        assertNotNull(bitmap)

        writeBitmapPreview("top_half_semi_circle.png", bitmap)
    }

    @Test
    fun drawTimeOfDay_currentTimeBeforeTimestamp1_producesPngArtifact() {
        // Condition 1: currentTime < timestamp1 → label shows delta to timestamp1
        val t1 = 3600_000L   // sunrise at 60 min
        val t2 = 7200_000L   // sunset at 120 min
        val current = 1800_000L  // 30 min before sunrise
        val bitmap = PixelDrawing().drawTimeOfDayBitmap(currentTime = current, timestamp1 = t1, timestamp2 = t2)
        assertNotNull(bitmap)
        writeBitmapPreview("time_of_day_before_sunrise.png", bitmap)
    }

    @Test
    fun drawTimeOfDay_currentTimeAtStart_producesPngArtifact() {
        val t1 = 0L
        val t2 = 3600_000L // 1 hour later
        val bitmap = PixelDrawing().drawTimeOfDayBitmap(currentTime = t1, timestamp1 = t1, timestamp2 = t2)
        assertNotNull(bitmap)
        writeBitmapPreview("time_of_day_at_start.png", bitmap)
    }

    @Test
    fun drawTimeOfDay_currentTimeAtMidpoint_producesPngArtifact() {
        val t1 = 0L
        val t2 = 3600_000L
        val mid = (t1 + t2) / 2
        val bitmap = PixelDrawing().drawTimeOfDayBitmap(currentTime = mid, timestamp1 = t1, timestamp2 = t2)
        assertNotNull(bitmap)
        writeBitmapPreview("time_of_day_at_midpoint.png", bitmap)
    }

    @Test
    fun drawTimeOfDay_currentTimeAtEnd_producesPngArtifact() {
        val t1 = 0L
        val t2 = 3600_000L
        val bitmap = PixelDrawing().drawTimeOfDayBitmap(currentTime = t2, timestamp1 = t1, timestamp2 = t2)
        assertNotNull(bitmap)
        writeBitmapPreview("time_of_day_at_end.png", bitmap)
    }
}
