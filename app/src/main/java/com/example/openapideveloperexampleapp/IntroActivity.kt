// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Introduction activity
 *
 * The first activity of the Android application that the users interacts with. It's the entry
 * point of the app before the connection process to the AX Visio starts.
 */
class IntroActivity : ComponentActivity() {
    companion object {
        private const val TAG = "IntroActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: intent=${intent}")

        setContent {
            MaterialTheme {
                IntroScreen(
                    onStartClick = {
                        val intent = Intent(this, ConnectActivity::class.java)
                        intent.putExtra(
                            ConnectActivity.EXTRA_NEXT_ACTIVITY_INTENT,
                            Intent(this, MainActivity::class.java)
                        )
                        intent.putExtra(ConnectActivity.EXTRA_API_KEY, BuildConfig.OPENAPI_API_KEY)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}

@Composable
fun IntroScreen(onStartClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "OpenAPI Developer Example Application",
                fontSize = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(onClick = onStartClick) {
                Text("Start")
            }
        }
    }
}

