package com.example.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocalVideo(
    val id: Long,
    val uri: Uri,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val width: Int,
    val height: Int,
    val channelName: String,
    val path: String,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val lastWatchedPositionMs: Long = 0L,
    val viewCount: Int = 0,
    val description: String = "",
    val customChannelId: Long? = null,
    val customChannelName: String? = null,
    val customChannelIconUri: String? = null,
    val customChannelColorHex: String? = null,
    val customTitle: String? = null
) {
    val isShort: Boolean
        get() = (durationMs in 1..65_000) || (height > 0 && width > 0 && height > width)

    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "00:00"
            val totalSeconds = durationMs / 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                else -> String.format(Locale.US, "%.0f KB", kb)
            }
        }

    val formattedDate: String
        get() {
            if (dateAddedSec <= 0) return "Recently"
            val date = Date(dateAddedSec * 1000)
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return formatter.format(date)
        }

    val formattedViews: String
        get() = "$viewCount ${if (viewCount == 1) "view" else "views"}"

    val effectiveChannelName: String
        get() = customChannelName ?: channelName

    val metadataSubline: String
        get() = "$effectiveChannelName • $formattedViews • $formattedDate"

    val resolutionLabel: String
        get() = if (width > 0 && height > 0) {
            when {
                width >= 3840 || height >= 2160 -> "${width}x${height} 4K UHD"
                width >= 1920 || height >= 1080 -> "${width}x${height} Full HD"
                width >= 1280 || height >= 720 -> "${width}x${height} HD"
                else -> "${width}x${height} SD"
            }
        } else {
            "Standard Resolution"
        }
}
