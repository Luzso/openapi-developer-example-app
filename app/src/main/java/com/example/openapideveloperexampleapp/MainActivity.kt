// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.definition.SOContext
import com.swarovskioptik.comm.definition.topic.ConfigureKeyActionProcedure
import com.swarovskioptik.comm.definition.topic.KeyAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

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

    // Coroutine scope tied to the Activity lifecycle
    private val mainScope = MainScope()

    // Job for the availableContexts collector started in onResume
    private var contextsJob: Job? = null

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

        val sdk = sdk ?: return;

        // Cancel any previous collector (defensive; should only be one)
        contextsJob?.cancel();

        contextsJob = mainScope.launch {
            sdk.availableContexts
                .asFlow()
                .collect { contexts ->
                    if(!contexts.contains(SOContext.OpenAPIContextBLE)) {
                        Log.e(
                            TAG,
                            "OpenAPI Context removed. Mostly the app was deselected via the selection wheel!"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "OpenAPI on AX Visio was stopped!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        cancel()
                        return@collect
                    }
                    else {
                        // We have the OpenAPI BLE context. Claim it and configure.
                        try {
                            // Wait for use(OpenAPIContextBLE) to complete
                            sdk.use(SOContext.OpenAPIContextBLE).await()

                            // --- CURRENT BEHAVIOUR: configure key mapping ---
                            // You can delete/replace this block with your media client logic later.
                            val params = ConfigureKeyActionProcedure.Params(
                                "SCROLL_KEY",
                                KeyAction.Down,
                                "TRIGGER_CAMERA_TAKEPICTURE"
                            )
                            sdk.publishTopic(ConfigureKeyActionProcedure, params).await()
                            // ------------------------------------------------

                        } catch (e: Throwable) {
                            Log.e(TAG, "Cannot use OpenAPIBLE context or configure key", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Cannot connect to OpenAPI inside App",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")

        val sdk = sdk ?: return

        // Stop listening to contexts while paused
        contextsJob?.cancel()
        contextsJob = null

        // Release the context using coroutines
        mainScope.launch {
            try {
                sdk.release(SOContext.OpenAPIContextBLE).await()
            } catch (e: Throwable) {
                Log.e(TAG, "Cannot release OpenAPIContextBLE!", e)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mainScope.cancel()
    }
}