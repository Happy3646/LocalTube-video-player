package com.example.data.repository

import android.content.Context
import com.example.data.local.CustomChannelEntity
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.FolderSettingEntity
import com.example.data.local.LikedShortEntity
import com.example.data.local.LocalTubeDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.SubscribedChannelEntity
import com.example.data.local.TagEntity
import com.example.data.local.VideoMetadataEntity
import com.example.data.local.VideoTagEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.media.MediaStoreScanner
import com.example.model.LocalVideo
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class VideoRepository(
    private val dao: LocalTubeDao
) {
    suspend fun scanVideos(context: Context): List<LocalVideo> {
        val currentFolderSettings = dao.getAllFolderSettings().first()
        val scanResult = MediaStoreScanner.scanVideos(context, currentFolderSettings)

        // Persist newly discovered folders (using IGNORE to preserve user toggles)
        if (scanResult.discoveredFolders.isNotEmpty()) {
            dao.insertFolderSettings(scanResult.discoveredFolders)
        }

        val rawVideos = scanResult.videos
        val allTags = dao.getAllVideoTags().first()
        val favorites = dao.getFavoriteVideoIds().first().toSet()
        val history = dao.getWatchHistory().first().associateBy { it.videoId }
        val metadataList = dao.getAllVideoMetadata().first().associateBy { it.videoId }
        val customChannels = dao.getAllCustomChannels().first().associateBy { it.id }

        val tagsByVideo = allTags.groupBy({ it.videoId }, { it.tag })

        return rawVideos.map { video ->
            val videoTags = tagsByVideo[video.id] ?: emptyList()
            val isFav = favorites.contains(video.id)
            val lastPos = history[video.id]?.lastPositionMs ?: 0L
            val meta = metadataList[video.id]
            val customChannel = meta?.customChannelId?.let { customChannels[it] }

            val effectiveTitle = meta?.customTitle?.ifBlank { null } ?: video.title

            video.copy(
                title = effectiveTitle,
                customTitle = meta?.customTitle,
                tags = videoTags,
                isFavorite = isFav,
                lastWatchedPositionMs = lastPos,
                viewCount = meta?.viewCount ?: 0,
                description = meta?.description ?: "",
                customChannelId = meta?.customChannelId,
                customChannelName = customChannel?.name,
                customChannelIconUri = customChannel?.iconUri,
                customChannelColorHex = customChannel?.colorHex
            )
        }
    }

    // Folder Settings
    fun getAllFolderSettings(): Flow<List<FolderSettingEntity>> = dao.getAllFolderSettings()

    suspend fun setFolderIncluded(folderPath: String, isIncluded: Boolean) {
        dao.setFolderIncluded(folderPath, isIncluded)
    }

    suspend fun addCustomFolder(context: Context, treeUri: Uri): FolderSettingEntity? {
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocId)
            var folderName = "Custom Folder"
            context.contentResolver.query(docUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        folderName = cursor.getString(nameIdx) ?: "Custom Folder"
                    }
                }
            }

            val entity = FolderSettingEntity(
                folderPath = treeUri.toString(),
                folderName = folderName,
                isIncluded = true,
                isCustom = true,
                treeUri = treeUri.toString()
            )
            dao.insertFolderSetting(entity)
            return entity
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun deleteCustomFolder(folderPath: String) {
        dao.deleteCustomFolder(folderPath)
    }

    // Playlists
    fun getPlaylists(): Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
        dao.clearPlaylist(playlistId)
    }

    fun getPlaylistVideoIds(playlistId: Long): Flow<List<Long>> = dao.getVideoIdsForPlaylist(playlistId)

    suspend fun addVideoToPlaylist(playlistId: Long, videoId: Long) {
        dao.addVideoToPlaylist(PlaylistItemEntity(playlistId = playlistId, videoId = videoId))
        val liked = dao.getPlaylistByName("Liked videos")
        if (liked?.id == playlistId) {
            dao.addFavorite(FavoriteVideoEntity(videoId = videoId))
            dao.setShortLiked(LikedShortEntity(videoId = videoId, isLiked = true))
        }
    }

    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: Long) {
        dao.removeVideoFromPlaylist(playlistId, videoId)
        val liked = dao.getPlaylistByName("Liked videos")
        if (liked?.id == playlistId) {
            dao.removeFavorite(videoId)
            dao.deleteLikedShort(videoId)
        }
    }

    suspend fun ensureDefaultPlaylists() {
        var liked = dao.getPlaylistByName("Liked videos")
        if (liked == null) {
            val id = dao.insertPlaylist(PlaylistEntity(name = "Liked videos", isDefault = true))
            liked = PlaylistEntity(id = id, name = "Liked videos", isDefault = true)
        }
        val favIds = dao.getFavoriteVideoIdsList()
        favIds.forEach { vidId ->
            dao.addVideoToPlaylist(PlaylistItemEntity(playlistId = liked.id, videoId = vidId))
        }
        if (dao.getPlaylistByName("Watch Later") == null) {
            dao.insertPlaylist(PlaylistEntity(name = "Watch Later", isDefault = true))
        }
    }

    // Persistent Tags (Pool of all tags)
    fun getAllPersistentTags(): Flow<List<String>> = combine(
        dao.getAllAppTags(),
        dao.getAllDistinctTags()
    ) { appTags, videoTags ->
        (appTags + videoTags + listOf("music", "camera", "shorts", "tutorial", "travel", "gaming", "offline"))
            .distinctBy { it.lowercase() }
            .sorted()
    }

    fun getTagsForVideo(videoId: Long): Flow<List<String>> = dao.getTagsForVideo(videoId)

    suspend fun savePersistentTag(tag: String) {
        val cleanTag = tag.trim().removePrefix("#").lowercase()
        if (cleanTag.isNotBlank()) {
            dao.insertAppTag(TagEntity(tag = cleanTag))
        }
    }

    suspend fun addTag(videoId: Long, tag: String) {
        val cleanTag = tag.trim().removePrefix("#").lowercase()
        if (cleanTag.isNotBlank()) {
            dao.insertAppTag(TagEntity(tag = cleanTag))
            dao.insertVideoTag(VideoTagEntity(videoId = videoId, tag = cleanTag))
        }
    }

    suspend fun removeTag(videoId: Long, tag: String) {
        val cleanTag = tag.trim().removePrefix("#").lowercase()
        dao.deleteVideoTag(videoId, cleanTag)
    }

    suspend fun toggleTag(videoId: Long, tag: String, isCurrentlyAssigned: Boolean) {
        if (isCurrentlyAssigned) {
            removeTag(videoId, tag)
        } else {
            addTag(videoId, tag)
        }
    }

    // Video Metadata & Views
    suspend fun recordVideoPlay(videoId: Long, durationMs: Long) {
        // Record watch history
        dao.recordWatch(
            WatchHistoryEntity(
                videoId = videoId,
                watchedAt = System.currentTimeMillis(),
                lastPositionMs = 0L,
                durationMs = durationMs
            )
        )
        // Increment view count
        val existing = dao.getVideoMetadata(videoId)
        if (existing == null) {
            dao.insertVideoMetadata(
                VideoMetadataEntity(videoId = videoId, viewCount = 1)
            )
        } else {
            dao.insertVideoMetadata(
                existing.copy(viewCount = existing.viewCount + 1)
            )
        }
    }

    suspend fun updateVideoDescription(videoId: Long, description: String) {
        val existing = dao.getVideoMetadata(videoId)
        if (existing == null) {
            dao.insertVideoMetadata(
                VideoMetadataEntity(videoId = videoId, description = description)
            )
        } else {
            dao.insertVideoMetadata(
                existing.copy(description = description)
            )
        }
    }

    suspend fun assignVideoToChannel(videoId: Long, channelId: Long?) {
        val existing = dao.getVideoMetadata(videoId)
        if (existing == null) {
            dao.insertVideoMetadata(
                VideoMetadataEntity(videoId = videoId, customChannelId = channelId)
            )
        } else {
            dao.insertVideoMetadata(
                existing.copy(customChannelId = channelId)
            )
        }
    }

    // Custom Channels
    fun getCustomChannels(): Flow<List<CustomChannelEntity>> = dao.getAllCustomChannels()

    suspend fun createCustomChannel(
        name: String,
        description: String,
        colorHex: String,
        iconUri: String?,
        assignedVideoIds: List<Long>
    ): Long {
        val channelId = dao.insertCustomChannel(
            CustomChannelEntity(
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex,
                iconUri = iconUri
            )
        )
        assignedVideoIds.forEach { vidId ->
            assignVideoToChannel(vidId, channelId)
        }
        return channelId
    }

    suspend fun deleteCustomChannel(channelId: Long) {
        dao.unassignVideosFromChannel(channelId)
        dao.deleteCustomChannel(channelId)
    }

    // Watch History
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()

    suspend fun recordWatchPosition(videoId: Long, positionMs: Long, durationMs: Long) {
        dao.recordWatch(
            WatchHistoryEntity(
                videoId = videoId,
                watchedAt = System.currentTimeMillis(),
                lastPositionMs = positionMs,
                durationMs = durationMs
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    // Favorites & Likes
    fun getFavoriteVideoIds(): Flow<List<Long>> = dao.getFavoriteVideoIds()

    suspend fun toggleFavorite(videoId: Long): Boolean {
        val isFav = dao.isFavorite(videoId)
        var liked = dao.getPlaylistByName("Liked videos")
        if (liked == null) {
            val id = dao.insertPlaylist(PlaylistEntity(name = "Liked videos", isDefault = true))
            liked = PlaylistEntity(id = id, name = "Liked videos", isDefault = true)
        }

        return if (isFav) {
            dao.removeFavorite(videoId)
            dao.deleteLikedShort(videoId)
            dao.removeVideoFromPlaylist(liked.id, videoId)
            false
        } else {
            dao.addFavorite(FavoriteVideoEntity(videoId = videoId))
            dao.setShortLiked(LikedShortEntity(videoId = videoId, isLiked = true))
            dao.addVideoToPlaylist(PlaylistItemEntity(playlistId = liked.id, videoId = videoId))
            true
        }
    }

    suspend fun renameVideoTitle(videoId: Long, newTitle: String) {
        val clean = newTitle.trim()
        val existing = dao.getVideoMetadata(videoId)
        if (existing == null) {
            dao.insertVideoMetadata(
                VideoMetadataEntity(videoId = videoId, customTitle = clean)
            )
        } else {
            dao.insertVideoMetadata(
                existing.copy(customTitle = clean)
            )
        }
    }

    suspend fun batchAddTags(videoIds: List<Long>, tags: List<String>) {
        val cleanTags = tags.map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotBlank() }
        for (tag in cleanTags) {
            dao.insertAppTag(TagEntity(tag = tag))
            for (videoId in videoIds) {
                dao.insertVideoTag(VideoTagEntity(videoId = videoId, tag = tag))
            }
        }
    }

    suspend fun batchAssignChannel(videoIds: List<Long>, channelId: Long?) {
        for (videoId in videoIds) {
            val existing = dao.getVideoMetadata(videoId)
            if (existing == null) {
                dao.insertVideoMetadata(
                    VideoMetadataEntity(videoId = videoId, customChannelId = channelId)
                )
            } else {
                dao.insertVideoMetadata(
                    existing.copy(customChannelId = channelId)
                )
            }
        }
    }

    // Subscribed Channels
    fun getAllSubscribedChannelKeys(): Flow<List<String>> = dao.getAllSubscribedChannelKeys()

    suspend fun toggleChannelSubscription(channelKey: String): Boolean {
        val isSub = dao.isChannelSubscribed(channelKey)
        if (isSub) {
            dao.unsubscribeChannel(channelKey)
            return false
        } else {
            dao.subscribeChannel(SubscribedChannelEntity(channelKey = channelKey))
            return true
        }
    }

    // Liked Shorts
    fun getLikedShorts(): Flow<List<LikedShortEntity>> = dao.getAllLikedShorts()

    suspend fun toggleShortLiked(videoId: Long): Boolean {
        return toggleFavorite(videoId)
    }
}
