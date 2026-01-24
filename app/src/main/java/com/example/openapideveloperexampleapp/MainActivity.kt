// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.definition.SOContext
import com.swarovskioptik.comm.definition.topic.ConfigureKeyActionProcedure
import com.swarovskioptik.comm.definition.topic.KeyAction
import com.swarovskioptik.comm.definition.topic.RenderPixelGraphic
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.io.ByteArrayOutputStream

/**
 * MainActivity of the application
 *
 * This activity contains the actually feature logic of the application. The functionality
 * that the app provides to a user by facilitating the OpenAPI of the AX Visio.
 */
class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private var sdk: SOCommOutsideAPI? = null
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: intent=${intent}")

        sdk = ConnectActivity.getSdk()
        if (sdk == null) {
            Log.e(TAG, "SOCommOutsideAPI/sdk is null!")
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonDisconnect).setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        sdk!!.availableContexts.observeOn(AndroidSchedulers.mainThread()).subscribe { contexts ->
            if (!contexts.contains(SOContext.OpenAPIContext)) {
                Log.e(
                    TAG,
                    "OpenAPI Context removed. Mostly the app was deselected via the selection wheel!"
                )
                Toast.makeText(this, "OpenAPI on AX Visio was stopped!", Toast.LENGTH_LONG).show()
                // Finished this Activity and switch back to the ConnectActivity
                finish()
                return@subscribe
            } else {
                // NOTE: This chain of init can also be coded with .andThen()-Operator
                sdk!!.use(SOContext.OpenAPIContext)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // Successfully used the OpenAPIContextBLE.
                        // TODO: 1 - Create bytearray for graphic
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

                        // TODO: 2 - Create graphics parameters
                        val params = RenderPixelGraphic.Param(
                            duration = 10_000, // Milliseconds
                            rotatedWithKnickbridgeAngle = true,
                            graphic = graphicBytes
                        )

                        // TODO: 3 - Publish graphics parameters to AX Visio
                        sdk!!.publishTopic(RenderPixelGraphic, params)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({}, { e ->
                                Log.e(TAG, "RenderPixelGraphic failed!", e)
                                Toast.makeText(
                                    this,
                                    "Failed to render graphic on the AX Visio.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                            .addTo(disposables)



                        // NOTE: You should check for errors on the Completable object. But these will only include
                        // errors on the local side, e.g. a lost connection to the AX Visio.
                        // The remote side, the AX Visio, does not report errors, e.g. a wrong keyCode name or
                        // a wrong procedure value.
                        /*val params = ConfigureKeyActionProcedure.Params(
                            "SCROLL_KEY",
                            KeyAction.Down,
                            "TRIGGER_CAMERA_TAKEPICTURE"
                        )
                        sdk!!.publishTopic(ConfigureKeyActionProcedure, params)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({}, { e ->
                                Log.e(TAG, "Configure the key ActionProcedure failed!", e)
                                Toast.makeText(
                                    this,
                                    "Failed to configure the key on the AX Visio.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                            .addTo(disposables)*/


                    }, { e ->
                        Log.e(TAG, "Cannot use OpenAPIBLE context", e)
                        Toast.makeText(
                            this,
                            "Cannot connect to OpenAPI inside App",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                    .addTo(disposables)
            }
        }.addTo(disposables)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")

        // Here the app releases the SOContext. The app does not use the OpenAPI functionality
        // of the AX Visio anymore.
        sdk!!.release(SOContext.OpenAPIContextBLE)
            .subscribe({}, { e -> Log.e(TAG, "Cannot release OpenAPIContextBLE!", e) })
            .addTo(disposables)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        disposables.dispose()
    }
}