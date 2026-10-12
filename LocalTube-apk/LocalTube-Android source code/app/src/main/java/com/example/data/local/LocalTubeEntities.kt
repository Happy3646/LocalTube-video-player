package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "videoId"])
data class PlaylistItemEntity(
    val playlistId: Long,
    val videoId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_tags", primaryKeys = ["videoId", "tag"])
data class VideoTagEntity(
    val videoId: Long,
    val tag: String
)

@Entity(tableName = "app_tags")
data class TagEntity(
    @PrimaryKey
    val tag: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_metadata")
data class VideoMetadataEntity(
    @PrimaryKey
    val videoId: Long,
    val viewCount: Int = 0,
    val description: String = "",
    val customChannelId: Long? = null,
    val customTitle: String? = null
)

@Entity(tableName = "subscribed_channels")
data class SubscribedChannelEntity(
    @PrimaryKey
    val channelKey: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_channels")
data class CustomChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#FF0000",
    val iconUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val videoId: Long,
    val watchedAt: Long = System.currentTimeMillis(),
    val lastPositionMs: Long = 0L,
    val durationMs: Long = 0L
)

@Entity(tableName = "liked_shorts")
data class LikedShortEntity(
    @PrimaryKey
    val videoId: Long,
    val isLiked: Boolean = true,
    val likedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey
    val videoId: Long,
    val favoritedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folder_settings")
data class FolderSettingEntity(
    @PrimaryKey
    val folderPath: String,
    val folderName: String,
    val isIncluded: Boolean = true,
    val isCustom: Boolean = false,
    val treeUri: String? = null
)

