// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.openapideveloperexampleapp.ui.components.AppScreen
import com.example.openapideveloperexampleapp.ui.components.BodyText
import com.example.openapideveloperexampleapp.ui.components.PrimaryButton
import com.example.openapideveloperexampleapp.ui.components.SectionHeader
import com.example.openapideveloperexampleapp.ui.theme.AppTheme
import com.example.openapideveloperexampleapp.BuildConfig.DEBUG
import com.jakewharton.threetenabp.AndroidThreeTen
import com.swarovskioptik.comm.SOCommDeviceSearcher
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.SOCommOutsideAPIBuilder
import com.swarovskioptik.comm.definition.SOContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.LinkedList
import java.util.concurrent.TimeUnit

/**
 * Complete the necessary steps to connect to the AX Visio
 *
 * This activity guides the user trough the necessary steps to connect the smartphone
 * to the AX Visio. These steps must be done for all apps that want to use the OpenAPI.
 * Therefore they are extract into a distinct activity. This should make code reuse, e.g.
 * copy&paste to other applications easier.
 *
 * Notes how to use the activity:
 *
 * When starting the activity the calling intent must contain two parameters as extra data.
 * It must contain the OpenAPI API key under then name {@link #EXTRA_API_KEY}. And it must contain
 * an intent that should be called under the {@link #EXTRA_NEXT_ACTIVITY_INTENT}, when the connection
 * to the AX Visio was successful.
 *
 * After this activity has finished, the called activity can starting using SO Contexts and
 * publishing and subscribing topics.
 */
class ConnectActivity : ComponentActivity() {
    companion object {
        private const val TAG = "ConnectActivity"
        private const val PERMISSION_REQUEST_CODE = 42

        const val EXTRA_NEXT_ACTIVITY_INTENT = "nextActivityIntent"
        const val EXTRA_API_KEY = "apiKey"

        private var sdk: SOCommOutsideAPI? = null
        fun getSdk(): SOCommOutsideAPI? {
            return sdk
        }
    }

    // Screens/States:
    // - no permissions and no bluetooth, start to request permissions
    // - not connected, start to connect
    // - no OpenAPI context available, waiting for OpenAPI context and use it
    enum class UIState {
        NONE, // Only used initially for the first transition
        REQUEST_PERMISSIONS_AND_BLUETOOTH,
        CONNECT_TO_AX_VISIO,
        WAIT_FOR_OPENAPI_INSIDE_APP,
    }

    private var uiState = UIState.NONE
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val disposables = CompositeDisposable()
    private var nextActivityIntent: Intent? = null
    private var apiKey: String? = null

    private fun bluetoothAdapterStateToString(i: Int): String {
        return when (i) {
            BluetoothAdapter.STATE_OFF -> "STATE_OFF"
            BluetoothAdapter.STATE_TURNING_OFF -> "STATE_TURNING_OFF"
            BluetoothAdapter.STATE_ON -> "STATE_ON"
            BluetoothAdapter.STATE_TURNING_ON -> "STATE_TURNING_ON"
            else -> "UNKNOWN"
        }
    }

    // To monitor the system state of bluetooth
    private val bluetoothBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action != BluetoothAdapter.ACTION_STATE_CHANGED)
                return

            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )

            if (DEBUG) Log.d(TAG, "bluetooth state: ${bluetoothAdapterStateToString(state)}")
            // Recreate activity to update UI based on new bluetooth state
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DEBUG) Log.d(TAG, "onCreate: intent=${intent}")

        nextActivityIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_NEXT_ACTIVITY_INTENT, Intent::class.java)
        } else {
            // See https://stackoverflow.com/a/73019285
            intent.getParcelableExtra(EXTRA_NEXT_ACTIVITY_INTENT)
        }
        if (nextActivityIntent == null) {
            Toast.makeText(this, "Internal Error", Toast.LENGTH_SHORT).show()
            Log.e(
                TAG,
                "Cannot start ConnectActivity: 'nextActivityIntent' is null. Calling Activity must provided it!"
            )
            finish()
            return
        }

        apiKey = intent.getStringExtra(EXTRA_API_KEY)
        if (apiKey == null || apiKey!!.isEmpty()) {
            Toast.makeText(this, "Internal Error", Toast.LENGTH_SHORT).show()
            Log.e(
                TAG,
                "Cannot start ConnectActivity: 'apiKey' is null or empty. Calling Activity must provided it!"
            )
            finish()
            return
        }

        // There is currently a bug in the SOCommOutsideAPI library. Until this bug is resolved
        // this code line must init the transitive dependency AndroidThreeTen. The exception is:
        //    Caused by: org.threeten.bp.zone.ZoneRulesException: No time-zone data files registered
        AndroidThreeTen.init(this)

        // TODO How to handle bluetooth remove/shutdown? The bluetooth broadcast receiver is
        // only in this activity.
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager!!.adapter
        registerReceiver(
            bluetoothBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        // Create the main SDK object
        sdk = SOCommOutsideAPIBuilder(context = this)
            .apiKey(apiKey!!)
            .build()

        setContent {
            AppTheme {
                var currentState by remember { mutableStateOf(getNewUIState()) }

                LaunchedEffect(Unit) {
                    // Trigger initial UI update
                }

                ConnectScreenRouter(
                    currentState = currentState,
                    onRequestPermissions = {
                        val permissions = getPermissionForApiLevel()
                        requestPermissions(permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
                    },
                    onConnectToDevice = { deviceName ->
                        connectToDevice(deviceName) {
                            // Update state after successful connection
                            currentState = getNewUIState()
                        }
                    },
                    onContinueToMain = {
                        val intent = nextActivityIntent!!
                        intent.putExtra("goto", Intent(this, MainActivity::class.java))
                        startActivity(intent)
                    },
                    onUpdateState = {
                        currentState = getNewUIState()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
        sdk?.disconnect()?.subscribe()
        sdk = null

        if (bluetoothManager != null) {
            // Only unregister if the receiver was registered in onCreate(). Otherwise it raises
            // an exception.
            unregisterReceiver(bluetoothBroadcastReceiver)
            bluetoothAdapter = null
            bluetoothManager = null
        }
    }

    private fun getPermissionForApiLevel(): List<String> {
        val permissions = LinkedList<String>()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            permissions.addAll(
                mutableListOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // NOTE: When using the MediaClient more runtime permissions are needed.

        return permissions
    }

    private fun areRuntimePermissionsGranted(): Boolean {
        val permissions = getPermissionForApiLevel()
        permissions.forEach { permission ->
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }

    private fun connectToDevice(deviceName: String, onSuccess: () -> Unit) {
        sdk!!.connect(deviceName)
            // Add a timeout. Otherwise the screen will block forever when no AX Visio device
            // is in reach! But the timeout must also be long enough so the user can handle
            // the initial pairing.
            .timeout(60, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // Connection successful, update state
                    if (DEBUG) Log.d(TAG, "Connected to AX Visio device successfully")
                    onSuccess()
                }, { e ->
                    Log.e(TAG, "Connect connect to the AX Visio device", e)
                    Toast.makeText(this, "Connecting to AX Visio failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            ).addTo(disposables)
    }

    private fun setupConnectionStateMonitoring() {
        sdk!!.connectionState
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ state ->
                if (DEBUG) Log.d(TAG, "connectionState: $state")
                if (state == SOCommOutsideAPI.ConnectionState.Disconnected) {
                    Log.w(TAG, "Connection to AX Visio lost!")
                    Toast.makeText(
                        this,
                        "Connection to AX Visio lost. Please try to reconnect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, { e ->
                Log.e(TAG, "Connection state failed!", e)
                Toast.makeText(this, "Connection to AX Visio failed!", Toast.LENGTH_SHORT).show()
            })
            .addTo(disposables)
    }

    private fun getNewUIState(): UIState {
        if (!areRuntimePermissionsGranted() || !bluetoothAdapter!!.isEnabled)
            return UIState.REQUEST_PERMISSIONS_AND_BLUETOOTH

        val connectionState = sdk!!.connectionState.blockingFirst()
        if (connectionState == SOCommOutsideAPI.ConnectionState.Connecting
            || connectionState == SOCommOutsideAPI.ConnectionState.Disconnected
        )
            return UIState.CONNECT_TO_AX_VISIO

        return UIState.WAIT_FOR_OPENAPI_INSIDE_APP
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        assert(requestCode == PERMISSION_REQUEST_CODE)

        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            Log.e(
                TAG,
                "The user did not grant all permissions: permissions=${permissions.contentToString()} grantResults=${grantResults.contentToString()}"
            )
            // TODO for a real applications:
            // Add note, when a user denied the permission once, she has to enable the permission
            // with the App Settings dialog. The app cannot request it again.
            Toast.makeText(this, "Some permissions were not granted!", Toast.LENGTH_SHORT).show()
        }

        // Recreate activity to trigger recomposition with new state
        recreate()
    }
}

@Composable
fun ConnectScreenRouter(
    currentState: ConnectActivity.UIState,
    onRequestPermissions: () -> Unit,
    onConnectToDevice: (String) -> Unit,
    onContinueToMain: () -> Unit,
    onUpdateState: () -> Unit
) {
    when (currentState) {
        ConnectActivity.UIState.REQUEST_PERMISSIONS_AND_BLUETOOTH -> {
            RequestPermissionsScreen(onRequestPermissions = onRequestPermissions)
        }
        ConnectActivity.UIState.CONNECT_TO_AX_VISIO -> {
            ConnectToAXVisioScreen(onConnectToDevice = onConnectToDevice)
        }
        ConnectActivity.UIState.WAIT_FOR_OPENAPI_INSIDE_APP -> {
            WaitForOpenAPIScreen(onContinueToMain = onContinueToMain)
        }
        ConnectActivity.UIState.NONE -> {
            // Initial state
        }
    }
}

@Composable
fun RequestPermissionsScreen(onRequestPermissions: () -> Unit) {
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
                SectionHeader(text = "Permissions Required")

                BodyText(
                    text = "The App needs access to Bluetooth and the location permission. Please grant it. Also please enable Bluetooth."
                )

                PrimaryButton(
                    text = "Request Permissions",
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}

@Composable
fun ConnectToAXVisioScreen(onConnectToDevice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var deviceName by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val deviceSearcher = SOCommDeviceSearcher.create(context)
        val deviceSearchDisposables = CompositeDisposable()

        deviceSearcher.search()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ foundDevices ->
                if (foundDevices.isEmpty()) return@subscribe

                val foundDevice = foundDevices.first()
                deviceName = foundDevice.deviceName

                // Stop search after finding first device
                deviceSearchDisposables.dispose()
            }, { e ->
                Log.e("ConnectToAXVisioScreen", "Error while searching AX Visio devices", e)
                Toast.makeText(
                    context,
                    "Error while searching for AX Visio devices!",
                    Toast.LENGTH_SHORT
                ).show()
            })
            .addTo(deviceSearchDisposables)
    }

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
                SectionHeader(text = "Connect to Device")

                BodyText(
                    text = "Searching for an AX Visio device in reach. When a device is found, you can touch the button to connect to it. If the AX Visio is not started, long press the POWER button to start it."
                )

                PrimaryButton(
                    text = if (deviceName != null)
                        "Connect to AX Visio ($deviceName)"
                    else
                        "Connect to AX Visio (UNKNOWN)",
                    onClick = {
                        deviceName?.let {
                            isConnecting = true
                            onConnectToDevice(it)
                        }
                    },
                    enabled = deviceName != null && !isConnecting,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}

@Composable
fun WaitForOpenAPIScreen(onContinueToMain: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sdk = ConnectActivity.getSdk()
    var isContextAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sdk?.connectionState
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ state ->
                if (BuildConfig.DEBUG) Log.d("WaitForOpenAPIScreen", "connectionState: $state")
                if (state == SOCommOutsideAPI.ConnectionState.Disconnected) {
                    Log.w("WaitForOpenAPIScreen", "Connection to AX Visio lost!")
                    Toast.makeText(
                        context,
                        "Connection to AX Visio lost. Please try to reconnect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, { e ->
                Log.e("WaitForOpenAPIScreen", "Connection state failed!", e)
                Toast.makeText(context, "Connection to AX Visio failed!", Toast.LENGTH_SHORT).show()
            })

        sdk?.availableContexts
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { contexts ->
                if (BuildConfig.DEBUG) Log.d("WaitForOpenAPIScreen", "availableContexts: $contexts")
                isContextAvailable = contexts.contains(SOContext.OpenAPIContextBLE)
            }
    }

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
                SectionHeader(text = "Start OpenAPI")

                Image(
                    painter = painterResource(id = R.drawable.openapi_icon),
                    contentDescription = "OpenAPI Icon",
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(80.dp)
                )

                BodyText(
                    text = "The OpenAPI Inside App is not started on the AX Visio. Use the Selection Wheel to start the OpenAPI App. If the Screen is off, press the power button to turn on the screen. You should see the text 'Please Connect'."
                )

                PrimaryButton(
                    text = "Continue",
                    onClick = onContinueToMain,
                    enabled = isContextAvailable,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}
