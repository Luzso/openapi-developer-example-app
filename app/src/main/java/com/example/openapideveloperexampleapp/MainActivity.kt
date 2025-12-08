// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.openapideveloperexampleapp.media.MediaClientManager
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.definition.SOContext
import com.swarovskioptik.comm.definition.topic.ConfigureKeyActionProcedure
import com.swarovskioptik.comm.definition.topic.KeyAction
import com.swarovskioptik.comm.definition.topic.RequestPictureThumbnails
import com.swarovskioptik.comm.media.SOCommMediaClient
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

        mediaClientManager = MediaClientManager(sdk!!, context = this)

        mainScope.launch {
            try {
                mediaClientManager?.start()
                Log.d(TAG, "MediaClient started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start MediaClient: ${e.message}", e)
            }
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

        val sdk = sdk ?: return

        // Cancel any previous collector (defensive; should only be one)
        contextsJob?.cancel()
            contextsJob = mainScope.launch {
                sdk.availableContexts
                    .asFlow()
                    .collect { contexts ->
                        if (!contexts.contains(SOContext.OpenAPIContextBLE) || !contexts.contains(
                                SOContext.PictureContext
                            )
                        ) {
                            Log.e(TAG, "OpenAPI Context removed...")
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
                        } else {
                            try {
                                sdk.use(SOContext.OpenAPIContextBLE).await()

                            /*Log.d(TAG, "OpenAPI context claimed successfully")

                            val params = ConfigureKeyActionProcedure.Params(
                                "SCROLL_KEY",
                                KeyAction.Down,
                                "TRIGGER_CAMERA_TAKEPICTURE"
                            )
                            sdk.publishTopic(ConfigureKeyActionProcedure, params).await()
                            Log.d(TAG, "Key configuration complete")
*/


                                Log.d(TAG, "Media client start() called")

                                // Launch separate coroutine for connection state
                                launch {
                                    mediaClientManager?.getConnectionState()
                                        ?.collect { connectionState ->
                                            Log.d(
                                                TAG,
                                                "MediaClient connection state: $connectionState"
                                            )
                                        }
                                }

                                // Launch separate coroutine for available images
                                launch {
                                    try{
                                    mediaClientManager?.getAvailableImages() 
                                        ?.collect { thumbnails ->
                                            Log.d(
                                                TAG,
                                                "Available thumbnails count: ${thumbnails.size}"
                                            )
                                            thumbnails.forEachIndexed { index, thumbnail ->
                                                Log.d(TAG, "Thumbnail $index: $thumbnail")
                                            }
                                        }
                                    }
                                    catch (e: Exception){
                                        Log.e(TAG, "Error collecting available images: ${e.message}", e)
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
                sdk.release(SOContext.PicturePreviewContext).await()
                sdk.release(SOContext.VideoPreviewContext).await()
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