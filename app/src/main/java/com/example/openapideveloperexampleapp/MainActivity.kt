// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.openapideveloperexampleapp.media.MediaClientManager
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.definition.SOContext
import com.swarovskioptik.comm.definition.topic.ConfigureKeyActionProcedure
import com.swarovskioptik.comm.definition.topic.KeyAction
import com.swarovskioptik.comm.media.SOCommMediaClient
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
import kotlin.io.use

/**
 * MainActivity of the application
 *
 * This activity contains the actually feature logic of the application. The functionality
 * that the app provides to a user by facilitating the OpenAPI of the AX Visio.
 */
class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val MEDIA_PERMISSION_REQUEST_CODE = 100
    }

    private var sdk: SOCommOutsideAPI? = null
    private var mediaClientManager: MediaClientManager? = null

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

        mediaClientManager = MediaClientManager(
            sdk!!,
            this)

        // Check permissions before starting
        if (!mediaClientManager!!.hasRequiredPermissions()) {
            requestPermissions(
                mediaClientManager!!.getRequiredPermissions(),
                MEDIA_PERMISSION_REQUEST_CODE
            )
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
                            sdk.use(SOContext.OpenAPIContextBLE).await()
                            Log.d(TAG, "OpenAPI context claimed successfully")

                            val params = ConfigureKeyActionProcedure.Params(
                                "SCROLL_KEY",
                                KeyAction.Down,
                                "TRIGGER_CAMERA_TAKEPICTURE"
                            )
                            sdk.publishTopic(ConfigureKeyActionProcedure, params).await()
                            Log.d(TAG, "Key configuration complete")

                            mediaClientManager?.start()
                            Log.d(TAG, "Media client start() called")

                            mediaClientManager?.getConnectionState()?.collect { connectionState ->
                                Log.d(TAG, "MediaClient connection state: $connectionState")

                                when (connectionState) {
                                    SOCommMediaClient.ConnectionState.ConnectedToPreview,
                                    SOCommMediaClient.ConnectionState.ConnectedToMediaDownload -> {
                                        Log.d(TAG, "MediaClient connected, starting image collection...")

                                        // Collect images only when connected
                                        mediaClientManager?.getAvailableImages()?.collect { thumbnails ->
                                            Log.d(TAG, "Available thumbnails count: ${thumbnails.size}")
                                            thumbnails.forEachIndexed { index, thumbnail ->
                                                Log.d(TAG, "Thumbnail $index: $thumbnail")
                                            }
                                        }
                                    }
                                    else -> {
                                        Log.d(TAG, "MediaClient not yet connected: $connectionState")
                                    }
                                }
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error in OpenAPI context or MediaClient", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Cannot connect to OpenAPI: ${e.message}",
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

        mediaClientManager?.stop()

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MEDIA_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "Media permissions granted, starting MediaClient...")
                mediaClientManager?.start()
            } else {
                Log.e(TAG, "Media permissions denied!")
            }
        }
    }


}