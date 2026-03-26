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
}
