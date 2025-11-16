# AX Visio OpenAPI Developer Example App - Workflow Documentation

## Overview

This Android application demonstrates how to connect and interact with the Swarovski Optik AX Visio device using the OpenAPI. The app guides users through the complete connection workflow and demonstrates basic OpenAPI functionality.

## Project Architecture

The application follows a sequential activity flow pattern:

```
IntroActivity → ConnectActivity → MainActivity
```

### Technology Stack
- **Language**: Kotlin
- **Platform**: Android
- **SDK**: Swarovski Optik SOCommOutsideAPI
- **Reactive Programming**: RxJava/RxAndroid
- **Date/Time**: AndroidThreeTen (ThreeTenBP)

---

## Application Workflow

### 1. IntroActivity (Entry Point)

**Purpose**: First screen of the application that serves as the launch point.

**File**: `IntroActivity.kt`

**Workflow**:
1. App launches and displays the intro screen (`activity_intro.xml`)
2. User clicks the "Start" button
3. Navigates to `ConnectActivity` with:
   - Next activity intent: `MainActivity`
   - API Key: Retrieved from `BuildConfig.OPENAPI_API_KEY`

**Key Code Flow**:
```kotlin
buttonStart.onClick() →
  Create Intent(ConnectActivity) →
  Add EXTRA_NEXT_ACTIVITY_INTENT (MainActivity) →
  Add EXTRA_API_KEY (from BuildConfig) →
  startActivity()
```

---

### 2. ConnectActivity (Connection Management)

**Purpose**: Handles the complete connection process to the AX Visio device through multiple states.

**File**: `ConnectActivity.kt`

This is the most complex activity with state-based UI transitions.

#### State Machine

The activity operates through three main UI states:

```
UIState.REQUEST_PERMISSIONS_AND_BLUETOOTH
    ↓
UIState.CONNECT_TO_AX_VISIO
    ↓
UIState.WAIT_FOR_OPENAPI_INSIDE_APP
```

#### Detailed State Workflow

##### State 1: REQUEST_PERMISSIONS_AND_BLUETOOTH

**Condition**: Missing runtime permissions OR Bluetooth disabled

**UI**: `activity_connect_request_permissions_and_bluetooth.xml`

**Actions**:
1. Check for required runtime permissions:
   - API Level > R (Android 11+):
     - `BLUETOOTH_SCAN`
     - `BLUETOOTH_CONNECT`
   - All API Levels:
     - `ACCESS_FINE_LOCATION`
2. Request permissions if missing
3. Monitor Bluetooth state via `BluetoothBroadcastReceiver`
4. Transition to next state when permissions granted AND Bluetooth enabled

**Key Components**:
- `getPermissionForApiLevel()`: Returns required permissions based on Android version
- `areRuntimePermissionsGranted()`: Validates all permissions are granted
- `onRequestPermissionsResult()`: Handles permission grant/deny callbacks
- `bluetoothBroadcastReceiver`: Monitors Bluetooth state changes

##### State 2: CONNECT_TO_AX_VISIO

**Condition**: Permissions granted, Bluetooth enabled, but not connected to device

**UI**: `activity_connect_connect_to_ax_visio.xml`

**Actions**:
1. Initialize `SOCommDeviceSearcher` to find nearby AX Visio devices
2. Search for devices via Bluetooth LE scanning
3. Display first found device to user
4. User selects device and clicks "Connect to AX Visio"
5. Establish Bluetooth connection using `sdk.connect(deviceName)`
6. Connection timeout set to 60 seconds (allows time for initial pairing)
7. Transition to next state on successful connection

**Key Components**:
- `SOCommDeviceSearcher.create()`: Creates device scanner
- `deviceSearcher.search()`: Observable stream of found devices
- `sdk.connect()`: Establishes connection to selected device
- Device search stops when first device found (battery optimization)

**Important Notes**:
- Device search drains battery and should be stopped ASAP
- Real-world apps should:
  - Display list of multiple devices
  - Remember last connected device
  - Allow manual device selection

##### State 3: WAIT_FOR_OPENAPI_INSIDE_APP

**Condition**: Connected to device, waiting for OpenAPI context availability

**UI**: `activity_connect_wait_for_openapi_inside_app.xml`

**Actions**:
1. Monitor `sdk.connectionState` for disconnections
2. Subscribe to `sdk.availableContexts` Observable
3. Wait for `SOContext.OpenAPIContextBLE` to become available
4. Enable "Continue to Main" button when context available
5. User clicks button to proceed to `MainActivity`

**Key Components**:
- `sdk.connectionState`: Monitors connection status
- `sdk.availableContexts`: List of available contexts from AX Visio
- Context becomes available when OpenAPI is activated on the device

**Important Notes**:
- Context is activated by the user on the AX Visio device
- Connection can be lost at any point, requiring reconnection
- App must handle graceful disconnection scenarios

#### SDK Initialization

The activity creates and manages the SOCommOutsideAPI singleton:

```kotlin
sdk = SOCommOutsideAPIBuilder(context = this)
    .apiKey(apiKey)
    .build()
```

**Lifecycle Management**:
- `onCreate()`: Initialize SDK, register Bluetooth receiver, start state machine
- `onDestroy()`: Disconnect SDK, dispose RxJava subscriptions, unregister receiver
- SDK instance stored in companion object for access by MainActivity

#### State Transition Logic

```kotlin
getNewUIState() determines next state:
  if (!areRuntimePermissionsGranted() || !bluetoothEnabled)
    → REQUEST_PERMISSIONS_AND_BLUETOOTH
  else if (connectionState is Connecting or Disconnected)
    → CONNECT_TO_AX_VISIO
  else
    → WAIT_FOR_OPENAPI_INSIDE_APP

updateStateAndMaybeUI() executes transition:
  if (newUIState != currentUIState)
    → Show appropriate screen
    → Update uiState variable
```

---

### 3. MainActivity (Feature Implementation)

**Purpose**: Demonstrates actual OpenAPI functionality using the connected device.

**File**: `MainActivity.kt`

**Workflow**:

1. **onCreate()**:
   - Retrieve SDK instance from `ConnectActivity.getSdk()`
   - Verify SDK is not null (exit if null)
   - Display main activity UI (`activity_main.xml`)
   - Setup disconnect button handler

2. **onResume()**:
   - Subscribe to `sdk.availableContexts` Observable
   - Check if `SOContext.OpenAPIContextBLE` is still available
   - If context removed → finish activity (return to ConnectActivity)
   - If context available:
     - Call `sdk.use(SOContext.OpenAPIContextBLE)` to claim context
     - Configure key action via `ConfigureKeyActionProcedure`
     - Maps SCROLL_KEY Down action to TRIGGER_CAMERA_TAKEPICTURE procedure

3. **onPause()**:
   - Release the OpenAPI context: `sdk.release(SOContext.OpenAPIContextBLE)`
   - Good practice to release when not actively using

4. **onDestroy()**:
   - Dispose all RxJava subscriptions
   - Clean up resources

#### OpenAPI Usage Pattern

```kotlin
// 1. Use the context
sdk.use(SOContext.OpenAPIContextBLE)

// 2. Publish topics/commands
val params = ConfigureKeyActionProcedure.Params(
    "SCROLL_KEY",           // Key identifier
    KeyAction.Down,         // Key action type
    "TRIGGER_CAMERA_TAKEPICTURE" // Procedure to execute
)
sdk.publishTopic(ConfigureKeyActionProcedure, params)

// 3. Release context when done
sdk.release(SOContext.OpenAPIContextBLE)
```

**Example Feature**: 
The app configures the AX Visio's scroll key to trigger the camera when pressed down. This demonstrates how external apps can customize device behavior.

**Error Handling**:
- Context removal detected → return to connection screen
- SDK errors logged and displayed via Toast messages
- Local errors reported (connection issues)
- Remote errors (AX Visio side) not reported by SDK

---

## Key Technical Concepts

### 1. SDK Lifecycle

```
Build SDK → Connect to Device → Use Context → Publish Topics → Release Context → Disconnect
```

### 2. Reactive Programming with RxJava

All asynchronous operations use Observables:
- Device search: `Observable<List<SOCommDevice>>`
- Connection: `Completable`
- Connection state: `Observable<ConnectionState>`
- Available contexts: `Observable<List<SOContext>>`
- Topic publishing: `Completable`

**Pattern**:
```kotlin
sdk.operation()
    .observeOn(AndroidSchedulers.mainThread())  // UI updates on main thread
    .subscribe(
        { /* success */ },
        { error -> /* handle error */ }
    )
    .addTo(disposables)  // Manage subscription lifecycle
```

### 3. Permission Management

**Android Version-Specific Permissions**:
- Android 12+ (API 31+): Requires new Bluetooth permissions
- All versions: Requires location for BLE scanning
- Runtime permission requests required
- Permission state monitored continuously

### 4. Context Management

**SOContext Concept**:
- Represents a capability/feature set on the AX Visio
- `OpenAPIContextBLE`: Main OpenAPI functionality
- Must be "used" before publishing topics
- Should be "released" when not needed
- Can be removed by device user at any time

### 5. State Monitoring

The app continuously monitors:
- Bluetooth adapter state (on/off/turning on/off)
- Connection state (disconnected/connecting/connected)
- Available contexts (context list changes)
- Permissions (granted/denied)

---

## Data Flow Diagram

```
User Input
    ↓
IntroActivity
    ↓ (with API Key)
ConnectActivity
    ↓
[Permission Check] → Request if needed
    ↓
[Bluetooth Check] → Prompt user to enable
    ↓
[Device Search] → SOCommDeviceSearcher
    ↓
[Connect] → sdk.connect(deviceName)
    ↓
[Monitor Contexts] → sdk.availableContexts
    ↓
[Wait for OpenAPI] → User activates on device
    ↓
MainActivity
    ↓
[Use Context] → sdk.use(SOContext.OpenAPIContextBLE)
    ↓
[Publish Topics] → sdk.publishTopic(...)
    ↓
[Execute Feature] → AX Visio performs action
    ↓
[Release Context] → sdk.release(...)
```

---

## Critical Implementation Details

### 1. API Key Management
- Stored in `BuildConfig.OPENAPI_API_KEY`
- Passed through intents between activities
- Required for SDK initialization

### 2. SDK Singleton Pattern
- Single SDK instance shared between activities
- Stored in `ConnectActivity.companion.sdk`
- Retrieved via `ConnectActivity.getSdk()`
- Ensures connection persists across activity transitions

### 3. Battery Optimization
- Device search stopped immediately after finding device
- Context released when not in use (onPause)
- Subscriptions properly disposed to prevent leaks

### 4. AndroidThreeTen Initialization
- **Bug workaround**: SDK has dependency on ThreeTenBP
- Must call `AndroidThreeTen.init(context)` before SDK use
- Required to prevent `ZoneRulesException`

### 5. Connection Timeout
- 60-second timeout on initial connection
- Allows time for user to pair device
- Prevents indefinite blocking

---

## Error Scenarios and Handling

### 1. Permission Denied
- Toast notification shown
- User must manually enable in app settings
- App cannot re-request after denial

### 2. Bluetooth Disabled
- Continuously monitored via broadcast receiver
- User prompted to enable
- State transitions blocked until enabled

### 3. Device Not Found
- Search continues indefinitely
- Battery drain consideration
- Real apps should implement timeout

### 4. Connection Failure
- 60-second timeout
- Error logged and Toast shown
- User can retry connection

### 5. Context Removed
- Detected via `availableContexts` Observable
- MainActivity finishes and returns to ConnectActivity
- Toast notification shown to user

### 6. Connection Lost
- Detected via `connectionState` Observable
- Appropriate UI updates
- User guided to reconnect

---

## Best Practices Demonstrated

1. **Separation of Concerns**: Connection logic isolated in dedicated activity
2. **State Management**: Clear UI states with explicit transitions
3. **Resource Cleanup**: Proper disposal of subscriptions and connections
4. **User Feedback**: Toast messages and UI updates for all state changes
5. **Error Handling**: Try-catch and error callbacks on all async operations
6. **Threading**: UI updates on main thread via `observeOn(AndroidSchedulers.mainThread())`
7. **Lifecycle Awareness**: Operations tied to appropriate lifecycle methods
8. **Battery Optimization**: Device search stopped when not needed
9. **Permission Handling**: Version-specific permission requests
10. **Graceful Degradation**: Handles context removal and disconnections

---

## Future Enhancements (TODOs in Code)

1. **Device Management**:
   - Show list of multiple devices to user
   - Remember last connected device
   - Implement device selection UI

2. **Bluetooth Lifecycle**:
   - Handle Bluetooth removal/shutdown in all activities
   - Broadcast receiver currently only in ConnectActivity

3. **Permission UX**:
   - Inform user about app settings requirement after denial
   - Provide direct link to app settings

4. **Media Client Support**:
   - Additional runtime permissions needed
   - Not currently implemented

5. **Error Reporting**:
   - Remote (AX Visio side) errors not reported by SDK
   - Consider alternative error detection methods

---

## AndroidManifest Configuration

**Activities**:
- `IntroActivity`: Launcher activity (exported=true)
- `ConnectActivity`: Internal (exported=false)
- `MainActivity`: Internal (exported=false)

**Required Permissions** (must be added to manifest):
- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `BLUETOOTH_SCAN` (Android 12+)
- `BLUETOOTH_CONNECT` (Android 12+)
- `ACCESS_FINE_LOCATION`

---

## Testing Workflow

### Manual Test Steps:

1. **Launch App**:
   - Verify intro screen appears
   - Click "Start" button

2. **Permission Request**:
   - Grant all requested permissions
   - Verify Bluetooth prompt if disabled

3. **Device Discovery**:
   - Ensure AX Visio is powered on and in range
   - Wait for device to appear
   - Click "Connect to AX Visio [device name]"
   - Complete Bluetooth pairing if first time

4. **Context Activation**:
   - On AX Visio device, activate OpenAPI
   - Wait for "Continue to Main" button to enable
   - Click button

5. **Feature Test**:
   - Verify main screen appears
   - On AX Visio, press scroll key down
   - Verify camera trigger action occurs
   - Click "Disconnect" to exit

### Error Test Cases:

- Deny permissions → verify toast and UI state
- Disable Bluetooth → verify monitoring and state change
- Move device out of range → verify connection loss handling
- Deactivate OpenAPI on device → verify context removal detection
- Force close app during connection → verify cleanup

---

## Code Organization Summary

```
app/src/main/java/.../openapideveloperexampleapp/
├── IntroActivity.kt          # Entry point, launches connection flow
├── ConnectActivity.kt        # Connection state machine (main logic)
└── MainActivity.kt           # Feature implementation example

app/src/main/res/layout/
├── activity_intro.xml        # Intro screen
├── activity_connect_request_permissions_and_bluetooth.xml
├── activity_connect_connect_to_ax_visio.xml
├── activity_connect_wait_for_openapi_inside_app.xml
└── activity_main.xml         # Main feature screen
```

---

## Dependencies

Key libraries used:
- `com.swarovskioptik.comm:SOCommOutsideAPI` - AX Visio SDK
- `io.reactivex.rxjava2:rxjava` - Reactive programming
- `io.reactivex.rxjava2:rxandroid` - Android-specific RxJava
- `io.reactivex.rxjava2:rxkotlin` - Kotlin extensions for RxJava
- `com.jakewharton.threetenabp:threetenabp` - Date/time library

---

## License

SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
SPDX-License-Identifier: Apache-2.0

---

## Conclusion

This example app provides a complete, production-ready template for connecting to and interacting with the AX Visio device. The modular design of `ConnectActivity` makes it easy to reuse in other applications - developers can copy the entire activity and its layouts to handle the connection workflow, then implement their own features in a separate activity.

The app demonstrates essential patterns: reactive programming, Android lifecycle management, Bluetooth connectivity, runtime permissions, and SDK integration - all crucial for building robust Android applications that communicate with external hardware devices.

