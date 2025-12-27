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
    private var cachedThumbnails: List<MediaItem.Thumbnail>? = null
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
            .maxNumberOfThumbnailsToFetch(5)
            .folderName("Camera")
            .build()
    }

    suspend fun start() {
       return mediaClient.asFlowApi().start()
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

    fun cacheThumbnails(thumbnails: List<MediaItem.Thumbnail>) {
        cachedThumbnails = thumbnails
    }

}