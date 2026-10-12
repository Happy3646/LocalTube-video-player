package com.example.data.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.FolderSettingEntity
import com.example.model.LocalVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ScanResult(
    val videos: List<LocalVideo>,
    val discoveredFolders: List<FolderSettingEntity>
)

object MediaStoreScanner {
    private const val TAG = "MediaStoreScanner"

    suspend fun scanVideos(
        context: Context,
        folderSettings: List<FolderSettingEntity> = emptyList()
    ): ScanResult = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<LocalVideo>()
        val seenPaths = mutableSetOf<String>()
        val discoveredFolders = mutableMapOf<String, FolderSettingEntity>()

        // Map of folderPath -> isIncluded
        val folderInclusionMap = folderSettings.associate { it.folderPath to it.isIncluded }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Video.Media.DATA
            }
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val bucketColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val rawDisplayName = if (displayNameColumn >= 0) cursor.getString(displayNameColumn) else null
                    val rawTitle = if (titleColumn >= 0) cursor.getString(titleColumn) else null
                    val duration = if (durationColumn >= 0) cursor.getLong(durationColumn) else 0L
                    val size = if (sizeColumn >= 0) cursor.getLong(sizeColumn) else 0L
                    val dateAdded = if (dateAddedColumn >= 0) cursor.getLong(dateAddedColumn) else 0L
                    val width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0
                    val height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0
                    val path = if (dataColumn >= 0) cursor.getString(dataColumn) ?: "" else ""

                    // Filter out hidden, cache, or ad files
                    if (isIgnoredPath(path) || (rawDisplayName != null && rawDisplayName.startsWith("."))) {
                        continue
                    }

                    val bucketName = if (bucketColumn >= 0) cursor.getString(bucketColumn) else null
                    val channelName = resolveChannelName(bucketName, path)
                    val folderPath = resolveFolderPath(bucketName, path)

                    // Track discovered folder
                    if (!discoveredFolders.containsKey(folderPath)) {
                        discoveredFolders[folderPath] = FolderSettingEntity(
                            folderPath = folderPath,
                            folderName = channelName,
                            isIncluded = folderInclusionMap[folderPath] ?: true,
                            isCustom = false
                        )
                    }

                    // Check if this folder has been excluded by the user in Settings
                    val isIncluded = folderInclusionMap[folderPath] ?: true
                    if (!isIncluded) {
                        continue
                    }

                    val cleanTitle = cleanVideoTitle(rawTitle, rawDisplayName, path)

                    if (path.isNotEmpty()) {
                        seenPaths.add(path)
                    }

                    videoList.add(
                        LocalVideo(
                            id = id,
                            uri = contentUri,
                            title = cleanTitle,
                            displayName = rawDisplayName ?: cleanTitle,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            width = width,
                            height = height,
                            channelName = channelName,
                            path = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore: ${e.message}", e)
        }

        // Scan custom SAF folders added by the user
        val activeCustomFolders = folderSettings.filter { it.isCustom && it.isIncluded && !it.treeUri.isNullOrBlank() }
        for (customSetting in activeCustomFolders) {
            val customTreeUri = Uri.parse(customSetting.treeUri)
            scanCustomSafFolder(context, customTreeUri, customSetting.folderName, seenPaths, videoList)
        }

        ScanResult(
            videos = videoList,
            discoveredFolders = discoveredFolders.values.toList()
        )
    }

    private fun scanCustomSafFolder(
        context: Context,
        treeUri: Uri,
        folderName: String,
        seenPaths: MutableSet<String>,
        outList: MutableList<LocalVideo>
    ) {
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val docId = if (idCol >= 0) cursor.getString(idCol) else continue
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol) ?: "Video" else "Video"
                    val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val lastModified = if (modCol >= 0) cursor.getLong(modCol) else 0L

                    if (displayName.startsWith(".") || isIgnoredPath(displayName)) continue

                    val isVideo = mimeType.startsWith("video/") ||
                            displayName.endsWith(".mp4", ignoreCase = true) ||
                            displayName.endsWith(".mkv", ignoreCase = true) ||
                            displayName.endsWith(".webm", ignoreCase = true) ||
                            displayName.endsWith(".3gp", ignoreCase = true)

                    if (!isVideo) continue

                    val videoUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    val pseudoPath = "$folderName/$displayName"

                    if (seenPaths.contains(pseudoPath) || seenPaths.contains(videoUri.toString())) continue
                    seenPaths.add(pseudoPath)

                    val cleanTitle = displayName.substringBeforeLast(".")
                        .replace("_", " ")
                        .trim()

                    outList.add(
                        LocalVideo(
                            id = docId.hashCode().toLong(),
                            uri = videoUri,
                            title = cleanTitle,
                            displayName = displayName,
                            durationMs = 0L,
                            sizeBytes = size,
                            dateAddedSec = if (lastModified > 0) lastModified / 1000 else System.currentTimeMillis() / 1000,
                            width = 0,
                            height = 0,
                            channelName = folderName,
                            path = pseudoPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning custom SAF folder: ${e.message}", e)
        }
    }

    private fun resolveFolderPath(bucketName: String?, path: String): String {
        if (path.isNotBlank()) {
            val file = File(path)
            val parentPath = file.parentFile?.absolutePath
            if (!parentPath.isNullOrBlank()) {
                return parentPath
            }
        }
        return bucketName ?: "Default Folder"
    }

    private fun resolveChannelName(bucketName: String?, path: String): String {
        if (!bucketName.isNullOrBlank()) {
            return bucketName
        }
        if (path.isNotBlank()) {
            val file = File(path)
            val parent = file.parentFile?.name
            if (!parent.isNullOrBlank() && parent != "0" && parent != "emulated") {
                return parent
            }
        }
        return "Internal Storage"
    }

    private fun isIgnoredPath(path: String): Boolean {
        if (path.isBlank()) return false
        val lower = path.lowercase()

        // Filter out hidden directories and files (starting with .)
        val segments = lower.split(File.separatorChar).filter { it.isNotBlank() }
        if (segments.any { it.startsWith(".") }) return true

        // Blacklist common cache, thumbnail, ad, and asset noise directories
        val blacklistedKeywords = listOf(
            "/cache/", "/.cache/", "/app_cache/", "/temp/", "/tmp/",
            "/thumbnails/", "/.thumbnails/", "/ad/", "/ads/", "/advertisement/",
            "/analytics/", "/banner/", "/banners/"
        )
        if (blacklistedKeywords.any { lower.contains(it) }) return true

        return false
    }

    private fun cleanVideoTitle(title: String?, displayName: String?, path: String): String {
        val candidate = when {
            !title.isNullOrBlank() -> title
            !displayName.isNullOrBlank() -> displayName
            path.isNotBlank() -> File(path).nameWithoutExtension
            else -> "Untitled Video"
        }
        return candidate.replace(".mp4", "", ignoreCase = true)
            .replace(".mkv", "", ignoreCase = true)
            .replace(".webm", "", ignoreCase = true)
            .replace(".3gp", "", ignoreCase = true)
            .replace("_", " ")
            .trim()
    }
}
