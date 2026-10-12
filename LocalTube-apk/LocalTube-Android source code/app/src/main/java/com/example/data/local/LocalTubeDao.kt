package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTubeDao {

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY isDefault DESC, createdAt ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?

    @Query("DELETE FROM playlists WHERE id = :id AND isDefault = 0")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT videoId FROM playlist_items WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getVideoIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVideoToPlaylist(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    // Persistent Tags (Pool of all tags)
    @Query("SELECT tag FROM app_tags ORDER BY tag ASC")
    fun getAllAppTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppTag(tag: TagEntity)

    @Query("DELETE FROM app_tags WHERE tag = :tag")
    suspend fun deleteAppTag(tag: String)

    // Video-specific Tags
    @Query("SELECT tag FROM video_tags WHERE videoId = :videoId")
    fun getTagsForVideo(videoId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT tag FROM video_tags ORDER BY tag ASC")
    fun getAllDistinctTags(): Flow<List<String>>

    @Query("SELECT * FROM video_tags")
    fun getAllVideoTags(): Flow<List<VideoTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideoTag(tagEntity: VideoTagEntity)

    @Query("DELETE FROM video_tags WHERE videoId = :videoId AND tag = :tag")
    suspend fun deleteVideoTag(videoId: Long, tag: String)

    @Query("DELETE FROM video_tags WHERE videoId = :videoId")
    suspend fun clearTagsForVideo(videoId: Long)

    // Video Metadata (Views, Description, Custom Channel)
    @Query("SELECT * FROM video_metadata WHERE videoId = :videoId")
    suspend fun getVideoMetadata(videoId: Long): VideoMetadataEntity?

    @Query("SELECT * FROM video_metadata")
    fun getAllVideoMetadata(): Flow<List<VideoMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoMetadata(metadata: VideoMetadataEntity)

    @Query("UPDATE video_metadata SET viewCount = viewCount + 1 WHERE videoId = :videoId")
    suspend fun incrementViewCount(videoId: Long): Int

    @Query("UPDATE video_metadata SET description = :description WHERE videoId = :videoId")
    suspend fun updateDescription(videoId: Long, description: String)

    @Query("UPDATE video_metadata SET customTitle = :customTitle WHERE videoId = :videoId")
    suspend fun updateCustomTitle(videoId: Long, customTitle: String?)

    @Query("UPDATE video_metadata SET customChannelId = :channelId WHERE videoId = :videoId")
    suspend fun setCustomChannel(videoId: Long, channelId: Long?)

    @Query("UPDATE video_metadata SET customChannelId = :channelId WHERE videoId IN (:videoIds)")
    suspend fun batchSetCustomChannel(videoIds: List<Long>, channelId: Long?)

    // Subscribed Channels
    @Query("SELECT channelKey FROM subscribed_channels")
    fun getAllSubscribedChannelKeys(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun subscribeChannel(entity: SubscribedChannelEntity)

    @Query("DELETE FROM subscribed_channels WHERE channelKey = :channelKey")
    suspend fun unsubscribeChannel(channelKey: String)

    @Query("SELECT EXISTS(SELECT 1 FROM subscribed_channels WHERE channelKey = :channelKey)")
    suspend fun isChannelSubscribed(channelKey: String): Boolean

    // Custom Channels
    @Query("SELECT * FROM custom_channels ORDER BY createdAt DESC")
    fun getAllCustomChannels(): Flow<List<CustomChannelEntity>>

    @Query("SELECT * FROM custom_channels WHERE id = :channelId")
    suspend fun getCustomChannel(channelId: Long): CustomChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomChannel(channel: CustomChannelEntity): Long

    @Query("DELETE FROM custom_channels WHERE id = :channelId")
    suspend fun deleteCustomChannel(channelId: Long)

    @Query("UPDATE video_metadata SET customChannelId = NULL WHERE customChannelId = :channelId")
    suspend fun unassignVideosFromChannel(channelId: Long)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 50")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordWatch(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun removeFromHistory(videoId: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    // Liked Shorts
    @Query("SELECT * FROM liked_shorts")
    fun getAllLikedShorts(): Flow<List<LikedShortEntity>>

    @Query("SELECT isLiked FROM liked_shorts WHERE videoId = :videoId")
    suspend fun isShortLiked(videoId: Long): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setShortLiked(entity: LikedShortEntity)

    @Query("DELETE FROM liked_shorts WHERE videoId = :videoId")
    suspend fun deleteLikedShort(videoId: Long)

    // Favorites
    @Query("SELECT videoId FROM favorite_videos ORDER BY favoritedAt DESC")
    fun getFavoriteVideoIds(): Flow<List<Long>>

    @Query("SELECT videoId FROM favorite_videos")
    suspend fun getFavoriteVideoIdsList(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE videoId = :videoId")
    suspend fun removeFavorite(videoId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE videoId = :videoId)")
    suspend fun isFavorite(videoId: Long): Boolean

    // Folder Settings (Include / Exclude Folders)
    @Query("SELECT * FROM folder_settings ORDER BY isCustom DESC, folderName ASC")
    fun getAllFolderSettings(): Flow<List<FolderSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderSetting(setting: FolderSettingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolderSettings(settings: List<FolderSettingEntity>)

    @Query("UPDATE folder_settings SET isIncluded = :isIncluded WHERE folderPath = :folderPath")
    suspend fun setFolderIncluded(folderPath: String, isIncluded: Boolean)

    @Query("DELETE FROM folder_settings WHERE folderPath = :folderPath AND isCustom = 1")
    suspend fun deleteCustomFolder(folderPath: String)
}
