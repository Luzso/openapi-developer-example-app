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
import com.example.openapideveloperexampleapp.ui.components.AppScreen
import com.example.openapideveloperexampleapp.ui.components.BodyText
import com.example.openapideveloperexampleapp.ui.components.PrimaryButton
import com.example.openapideveloperexampleapp.ui.components.SectionHeader
import com.example.openapideveloperexampleapp.ui.theme.AppTheme

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
            AppTheme {
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
        AppScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SectionHeader(text = "OpenAPI Developer Example")

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "Start",
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}

