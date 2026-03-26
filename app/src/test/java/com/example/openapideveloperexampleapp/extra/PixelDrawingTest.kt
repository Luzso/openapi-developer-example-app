package com.example.openapideveloperexampleapp.extra

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import org.junit.Assert.assertEquals
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
    fun drawDemoBitmap_producesPngArtifact() {
        val drawing = PixelDrawing()

        val png = drawing.drawDemoBitmap()
        assertTrue(png.size > 8)

        writePngArtifact("demo.png", png)
    }

    @Test
    fun drawTopHalfSemiCircle_producesPngArtifact() {
        val drawing = PixelDrawing()

        val bitmap = drawing.drawTopHalfSemiCircleBitmap()
        assertNotNull(bitmap)

        writeBitmapPreview("top_half_semi_circle.png", bitmap)
    }
    /*
    @Test
    fun circleBitmap_hasExpectedSize_andProducesPngArtifact() {
        val drawing = PixelDrawing()

        val bitmap = drawing.circleBitmap()
        assertNotNull(bitmap)
        assertEquals(128, bitmap.width)
        assertEquals(128, bitmap.height)

        val png = drawing.bitmapToPNGByteArray(bitmap)
        assertTrue(png.size > 8)
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        val expectedHeader = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        assertTrue("Expected PNG header", png.take(8).toByteArray().contentEquals(expectedHeader))

        writePngArtifact("circle.png", png)
    }

    @Test
    fun crosshairBitmap_hasExpectedSize_andProducesPngArtifact() {
        val drawing = PixelDrawing()

        val bitmap = drawing.crosshairBitmap()
        assertNotNull(bitmap)
        assertEquals(30, bitmap.width)
        assertEquals(30, bitmap.height)

        val png = drawing.bitmapToPNGByteArray(bitmap)
        assertTrue(png.size > 8)

        writePngArtifact("crosshair.png", png)
    }



    @Test
    fun bitmapLayersToPNGByteArray_combinesLayers_andProducesPngArtifact() {
        val drawing = PixelDrawing()

        val layers: List<Bitmap> = listOf(
            drawing.circleBitmap(),
            drawing.crosshairBitmap()
        )

        val png = drawing.bitmapLayersToPNGByteArray(layers)
        assertTrue(png.size > 8)

        writePngArtifact("combined_circle_crosshair.png", png)
    }*/
}
