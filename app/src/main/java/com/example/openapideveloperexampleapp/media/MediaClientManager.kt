package com.example.openapideveloperexampleapp.media

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.swarovskioptik.comm.SOCommDeviceCommunication
import com.swarovskioptik.comm.SOCommOutsideAPI
import com.swarovskioptik.comm.media.DeviceCommunication
import com.swarovskioptik.comm.media.MediaFileNameHandler
import com.swarovskioptik.comm.media.MediaType
import com.swarovskioptik.comm.media.SOCommMediaClient
import com.swarovskioptik.comm.media.SOCommMediaClientBuilder
import com.swarovskioptik.comm.media.model.MediaItem
import com.swarovskioptik.comm.media.wrapper.flow.asFlowApi
import kotlinx.coroutines.flow.Flow

class MediaClientManager(
    private val sdk: SOCommOutsideAPI,
    private val context: Context
) {

    private var deviceCommunication: DeviceCommunication
    private var mediaClient: SOCommMediaClient

    init {
        deviceCommunication = SOCommDeviceCommunication(api = sdk)
        mediaClient = SOCommMediaClientBuilder(context = context)
            .debugLogs{ tag, message -> android.util.Log.d(tag, message)}
            .errorLogs { tag, message -> android.util.Log.e(tag, message) }
            .deviceCommunication(deviceCommunication)
            .mediaType(MediaType.Picture)
            .mediaFileNameHandler(MediaFileNameHandler(emptyList()))
            .maxNumberOfThumbnailsToFetch(1)
            .folderName("DCIM")
            .build()
    }

    fun start() {
       mediaClient.start()
    }

    fun stop() {
        mediaClient.stop()
    }

    fun getConnectionState() : Flow<SOCommMediaClient.ConnectionState> {
        return mediaClient.asFlowApi().connectionState
    }

    fun getAvailableImages() : Flow<List<MediaItem.Thumbnail>> {
        return mediaClient.asFlowApi().availableThumbnails
    }

    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - only need READ_MEDIA_IMAGES for Pictures
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12
            context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10 and below
            context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}