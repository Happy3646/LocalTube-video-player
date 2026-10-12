package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomChannelEntity
import com.example.data.local.FolderSettingEntity
import com.example.data.local.LocalTubeDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.repository.VideoRepository
import com.example.model.LocalVideo
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class BottomNavTab {
    HOME,
    SHORTS,
    CHANNELS,
    YOU
}

sealed interface CurrentScreen {
    data object Main : CurrentScreen
    data class Player(val video: LocalVideo, val queue: List<LocalVideo> = emptyList()) : CurrentScreen
    data class ChannelDetail(val channelName: String, val customChannelId: Long? = null) : CurrentScreen
    data class PlaylistDetail(val playlist: PlaylistEntity) : CurrentScreen
    data object Guide : CurrentScreen
}

data class ChannelItem(
    val name: String,
    val videoCount: Int,
    val totalSizeBytes: Long,
    val latestVideo: LocalVideo?,
    val isCustom: Boolean = false,
    val channelId: Long? = null,
    val description: String = "",
    val colorHex: String? = null,
    val iconUri: String? = null,
    val isSubscribed: Boolean = false
) {
    val channelKey: String
        get() = if (channelId != null) "custom_$channelId" else "folder_$name"

    val subscriberSubline: String
        get() = "$videoCount ${if (videoCount == 1) "video" else "videos"} • ${if (isSubscribed) "Subscribed" else if (isCustom) "Custom Channel" else "Local Folder Channel"}"
}

data class LibraryStats(
    val totalVideos: Int = 0,
    val totalSizeBytes: Long = 0L,
    val shortsCount: Int = 0,
    val channelsCount: Int = 0
) {
    val formattedTotalSize: String
        get() {
            val gb = totalSizeBytes / (1024.0 * 1024.0 * 1024.0)
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) String.format(Locale.US, "%.2f GB", gb)
            else String.format(Locale.US, "%.1f MB", mb)
        }
}

class LocalTubeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository

    private val _currentScreen = MutableStateFlow<CurrentScreen>(CurrentScreen.Main)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomNavTab.HOME)
    val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

    private val _allVideos = MutableStateFlow<List<LocalVideo>>(emptyList())
    val allVideos: StateFlow<List<LocalVideo>> = _allVideos.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>>
    val allPersistentTags: StateFlow<List<String>>
    val watchHistory: StateFlow<List<WatchHistoryEntity>>
    val customChannels: StateFlow<List<CustomChannelEntity>>
    val likedShorts: StateFlow<Set<Long>>
    val folderSettings: StateFlow<List<FolderSettingEntity>>
    val subscribedChannelKeys: StateFlow<Set<String>>

    private val _isFeedShuffled = MutableStateFlow(false)
    val isFeedShuffled: StateFlow<Boolean> = _isFeedShuffled.asStateFlow()
    private val _shuffleSeed = MutableStateFlow(0L)

    private val prefs = application.getSharedPreferences("localtube_preferences", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        when (prefs.getString("theme_mode", "SYSTEM")) {
            "DARK" -> AppThemeMode.DARK
            "LIGHT" -> AppThemeMode.LIGHT
            else -> AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    init {
        val db = LocalTubeDatabase.getDatabase(application)
        repository = VideoRepository(db.localTubeDao())

        subscribedChannelKeys = repository.getAllSubscribedChannelKeys()
            .map { it.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

        folderSettings = repository.getAllFolderSettings().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        playlists = repository.getPlaylists().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allPersistentTags = repository.getAllPersistentTags().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        customChannels = repository.getCustomChannels().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        watchHistory = repository.getWatchHistory().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        val _liked = MutableStateFlow<Set<Long>>(emptySet())
        likedShorts = _liked.asStateFlow()

        viewModelScope.launch {
            repository.getLikedShorts().collect { list ->
                _liked.value = list.filter { it.isLiked }.map { it.videoId }.toSet()
            }
        }

        viewModelScope.launch {
            repository.ensureDefaultPlaylists()
        }

        // Initial scan
        scanDeviceVideos()
    }

    // Filtered Feed Videos for Home with Recommendation & Watch Affinity Ranking
    val feedVideos: StateFlow<List<LocalVideo>> = combine(
        combine(_allVideos, _searchQuery, _selectedTag) { v, q, t -> Triple(v, q, t) },
        combine(subscribedChannelKeys, _isFeedShuffled, _shuffleSeed) { s, sh, seed -> Triple(s, sh, seed) }
    ) { (videos, query, tag), (subscribedKeys, isShuffled, seed) ->
        if (videos.isEmpty()) return@combine emptyList()

        // 1. Calculate Tag & Watch Affinity
        val tagAffinityMap = mutableMapOf<String, Int>()
        for (v in videos) {
            val watchWeight = (v.viewCount * 2) + (if (v.lastWatchedPositionMs > 0) 3 else 0)
            if (watchWeight > 0) {
                for (t in v.tags) {
                    val cleanT = t.lowercase()
                    tagAffinityMap[cleanT] = (tagAffinityMap[cleanT] ?: 0) + watchWeight
                }
            }
        }

        // 2. Score and rank all videos
        val scoredVideos = videos.map { v ->
            val channelKey = if (v.customChannelId != null) "custom_${v.customChannelId}" else "folder_${v.channelName}"
            val isSubscribed = subscribedKeys.contains(channelKey)

            // Subscribed channels boost: top recommendation priority
            val subscriptionScore = if (isSubscribed) 1000f else 0f

            // Tag affinity score: sum of affinity for each assigned tag
            val tagScore = v.tags.sumOf { tagAffinityMap[it.lowercase()] ?: 0 } * 2.5f

            // Favorite boost
            val favScore = if (v.isFavorite) 120f else 0f

            // Resume in-progress boost
            val resumeScore = if (v.lastWatchedPositionMs > 5000L && v.lastWatchedPositionMs < (v.durationMs - 5000L)) 150f else 0f

            // Recency normalized score
            val recencyScore = (v.dateAddedSec % 1_000_000).toFloat() / 1_000f

            val totalScore = subscriptionScore + tagScore + favScore + resumeScore + recencyScore
            v to totalScore
        }

        var result = if (isShuffled) {
            val rng = kotlin.random.Random(seed)
            scoredVideos.map { it.first }.shuffled(rng)
        } else {
            scoredVideos.sortedByDescending { it.second }.map { it.first }
        }

        // 3. Apply Tag Filter if selected
        if (tag != null) {
            val cleanTag = tag.removePrefix("#").lowercase()
            result = result.filter { video ->
                video.tags.any { it.equals(cleanTag, ignoreCase = true) } ||
                        (cleanTag == "shorts" && video.isShort) ||
                        video.effectiveChannelName.lowercase().contains(cleanTag)
            }
        }

        // 4. Apply Search Query if typed
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { video ->
                video.title.lowercase().contains(q) ||
                        video.effectiveChannelName.lowercase().contains(q) ||
                        video.description.lowercase().contains(q) ||
                        video.tags.any { it.lowercase().contains(q) }
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shorts Videos: STRICTLY tag-gated only (requires #shorts or #short tag)
    val shortsVideos: StateFlow<List<LocalVideo>> = _allVideos.map { videos ->
        videos.filter { video ->
            video.tags.any { tag ->
                tag.equals("shorts", ignoreCase = true) || tag.equals("short", ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Untagged videos for organization hub
    val untaggedVideos: StateFlow<List<LocalVideo>> = _allVideos.map { list ->
        list.filter { it.tags.isEmpty() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unassigned videos for organization hub (no custom channel assigned)
    val unassignedVideos: StateFlow<List<LocalVideo>> = _allVideos.map { list ->
        list.filter { it.customChannelId == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Channels (Custom user channels + Detected folder channels)
    val channels: StateFlow<List<ChannelItem>> = combine(
        _allVideos,
        customChannels,
        subscribedChannelKeys
    ) { videos, customList, subscribedKeys ->
        val customItems = customList.map { custom ->
            val vids = videos.filter { it.customChannelId == custom.id }
            val key = "custom_${custom.id}"
            ChannelItem(
                name = custom.name,
                videoCount = vids.size,
                totalSizeBytes = vids.sumOf { it.sizeBytes },
                latestVideo = vids.maxByOrNull { it.dateAddedSec },
                isCustom = true,
                channelId = custom.id,
                description = custom.description,
                colorHex = custom.colorHex,
                iconUri = custom.iconUri,
                isSubscribed = subscribedKeys.contains(key)
            )
        }

        val folderItems = videos.groupBy { it.channelName }.map { (folderName, group) ->
            val key = "folder_$folderName"
            ChannelItem(
                name = folderName,
                videoCount = group.size,
                totalSizeBytes = group.sumOf { it.sizeBytes },
                latestVideo = group.maxByOrNull { it.dateAddedSec },
                isCustom = false,
                channelId = null,
                description = "Offline media folder on device",
                isSubscribed = subscribedKeys.contains(key)
            )
        }.sortedByDescending { it.videoCount }

        (customItems + folderItems).sortedWith(
            compareByDescending<ChannelItem> { it.isSubscribed }
                .thenByDescending { it.videoCount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats
    val libraryStats: StateFlow<LibraryStats> = combine(
        _allVideos,
        channels
    ) { videos, channelList ->
        LibraryStats(
            totalVideos = videos.size,
            totalSizeBytes = videos.sumOf { it.sizeBytes },
            shortsCount = videos.count { it.tags.any { t -> t.equals("shorts", true) || t.equals("short", true) } },
            channelsCount = channelList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryStats())

    fun scanDeviceVideos() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val videos = repository.scanVideos(getApplication())
                _allVideos.value = videos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setFolderIncluded(folderPath: String, isIncluded: Boolean) {
        viewModelScope.launch {
            repository.setFolderIncluded(folderPath, isIncluded)
            // Rescan with updated folder inclusions
            val videos = repository.scanVideos(getApplication())
            _allVideos.value = videos
        }
    }

    fun addCustomFolder(treeUri: Uri) {
        viewModelScope.launch {
            val added = repository.addCustomFolder(getApplication(), treeUri)
            if (added != null) {
                Toast.makeText(getApplication(), "Added folder: ${added.folderName}", Toast.LENGTH_SHORT).show()
                val videos = repository.scanVideos(getApplication())
                _allVideos.value = videos
            } else {
                Toast.makeText(getApplication(), "Could not add folder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteCustomFolder(folderPath: String) {
        viewModelScope.launch {
            repository.deleteCustomFolder(folderPath)
            val videos = repository.scanVideos(getApplication())
            _allVideos.value = videos
            Toast.makeText(getApplication(), "Custom folder removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _selectedTab.value = tab
        if (_currentScreen.value !is CurrentScreen.Main) {
            _currentScreen.value = CurrentScreen.Main
        }
    }

    fun navigateTo(screen: CurrentScreen) {
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value !is CurrentScreen.Main) {
            _currentScreen.value = CurrentScreen.Main
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun filterByTagAndGoHome(tag: String) {
        _selectedTab.value = BottomNavTab.HOME
        _selectedTag.value = tag
        _currentScreen.value = CurrentScreen.Main
    }

    fun playVideo(video: LocalVideo, queue: List<LocalVideo> = emptyList()) {
        val updatedViewCount = video.viewCount + 1
        val updatedVideo = video.copy(viewCount = updatedViewCount)

        // Increment views in in-memory list
        _allVideos.value = _allVideos.value.map { v ->
            if (v.id == video.id) updatedVideo else v
        }

        val effectiveQueue = if (queue.isNotEmpty()) {
            queue.map { if (it.id == video.id) updatedVideo else it }
        } else {
            _allVideos.value
        }

        _currentScreen.value = CurrentScreen.Player(video = updatedVideo, queue = effectiveQueue)

        viewModelScope.launch {
            repository.recordVideoPlay(video.id, video.durationMs)
        }
    }

    fun playShuffleMix() {
        val list = _allVideos.value.shuffled()
        if (list.isNotEmpty()) {
            playVideo(list.first(), list)
        } else {
            Toast.makeText(getApplication(), "No videos found to play", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordWatchPosition(videoId: Long, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.recordWatchPosition(videoId, positionMs, durationMs)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            Toast.makeText(getApplication(), "Watch history cleared", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(videoId: Long) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleFavorite(videoId)
            _allVideos.value = _allVideos.value.map { v ->
                if (v.id == videoId) v.copy(isFavorite = isNowLiked) else v
            }
            val screen = _currentScreen.value
            if (screen is CurrentScreen.Player && screen.video.id == videoId) {
                _currentScreen.value = screen.copy(video = screen.video.copy(isFavorite = isNowLiked))
            }
            val msg = if (isNowLiked) "Added to Liked videos" else "Removed from Liked videos"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleLikeShort(videoId: Long) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleShortLiked(videoId)
            _allVideos.value = _allVideos.value.map { v ->
                if (v.id == videoId) v.copy(isFavorite = isNowLiked) else v
            }
            val screen = _currentScreen.value
            if (screen is CurrentScreen.Player && screen.video.id == videoId) {
                _currentScreen.value = screen.copy(video = screen.video.copy(isFavorite = isNowLiked))
            }
            val msg = if (isNowLiked) "Added to Liked videos" else "Removed from Liked videos"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun addTagToVideo(videoId: Long, tag: String) {
        viewModelScope.launch {
            repository.addTag(videoId, tag)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            // Update active player screen if applicable
            val screen = _currentScreen.value
            if (screen is CurrentScreen.Player && screen.video.id == videoId) {
                val updatedVid = updated.firstOrNull { it.id == videoId } ?: screen.video
                _currentScreen.value = screen.copy(video = updatedVid)
            }
        }
    }

    fun removeTagFromVideo(videoId: Long, tag: String) {
        viewModelScope.launch {
            repository.removeTag(videoId, tag)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            val screen = _currentScreen.value
            if (screen is CurrentScreen.Player && screen.video.id == videoId) {
                val updatedVid = updated.firstOrNull { it.id == videoId } ?: screen.video
                _currentScreen.value = screen.copy(video = updatedVid)
            }
        }
    }

    fun toggleTagForVideo(videoId: Long, tag: String) {
        val video = _allVideos.value.firstOrNull { it.id == videoId } ?: return
        val cleanTag = tag.trim().removePrefix("#").lowercase()
        val hasTag = video.tags.any { it.equals(cleanTag, ignoreCase = true) }
        if (hasTag) {
            removeTagFromVideo(videoId, cleanTag)
        } else {
            addTagToVideo(videoId, cleanTag)
        }
    }

    fun updateVideoDescription(videoId: Long, description: String) {
        viewModelScope.launch {
            repository.updateVideoDescription(videoId, description)
            _allVideos.value = _allVideos.value.map { v ->
                if (v.id == videoId) v.copy(description = description) else v
            }
            val screen = _currentScreen.value
            if (screen is CurrentScreen.Player && screen.video.id == videoId) {
                _currentScreen.value = screen.copy(video = screen.video.copy(description = description))
            }
            Toast.makeText(getApplication(), "Description updated", Toast.LENGTH_SHORT).show()
        }
    }

    fun createCustomChannel(
        name: String,
        description: String,
        colorHex: String,
        iconUri: String?,
        assignedVideoIds: List<Long>
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            repository.createCustomChannel(
                name = name,
                description = description,
                colorHex = colorHex,
                iconUri = iconUri,
                assignedVideoIds = assignedVideoIds
            )
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            Toast.makeText(getApplication(), "Channel '$name' created!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCustomChannel(channelId: Long) {
        viewModelScope.launch {
            repository.deleteCustomChannel(channelId)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            Toast.makeText(getApplication(), "Channel deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun assignVideoToChannel(videoId: Long, channelId: Long?) {
        viewModelScope.launch {
            repository.assignVideoToChannel(videoId, channelId)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            Toast.makeText(getApplication(), "Channel assignment updated", Toast.LENGTH_SHORT).show()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.createPlaylist(name.trim())
                Toast.makeText(getApplication(), "Playlist '$name' created", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            Toast.makeText(getApplication(), "Playlist deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun addVideoToPlaylist(playlistId: Long, videoId: Long) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, videoId)
            Toast.makeText(getApplication(), "Added to playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeVideoFromPlaylist(playlistId: Long, videoId: Long) {
        viewModelScope.launch {
            repository.removeVideoFromPlaylist(playlistId, videoId)
            Toast.makeText(getApplication(), "Removed from playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun getPlaylistVideoIds(playlistId: Long): kotlinx.coroutines.flow.Flow<List<Long>> {
        return repository.getPlaylistVideoIds(playlistId)
    }

    fun shuffleFeed() {
        _isFeedShuffled.value = true
        _shuffleSeed.value = System.currentTimeMillis()
        _selectedTab.value = BottomNavTab.HOME
        if (_currentScreen.value !is CurrentScreen.Main) {
            _currentScreen.value = CurrentScreen.Main
        }
    }

    fun resetFeedOrder() {
        _isFeedShuffled.value = false
    }

    fun toggleChannelSubscription(customChannelId: Long?, folderChannelName: String) {
        val channelKey = if (customChannelId != null) "custom_$customChannelId" else "folder_$folderChannelName"
        viewModelScope.launch {
            val isNowSubscribed = repository.toggleChannelSubscription(channelKey)
            val msg = if (isNowSubscribed) "Subscribed to $folderChannelName" else "Unsubscribed from $folderChannelName"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun renameVideoTitle(videoId: Long, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isBlank()) return
        _allVideos.value = _allVideos.value.map { v ->
            if (v.id == videoId) v.copy(title = clean, customTitle = clean) else v
        }
        val screen = _currentScreen.value
        if (screen is CurrentScreen.Player && screen.video.id == videoId) {
            _currentScreen.value = screen.copy(video = screen.video.copy(title = clean, customTitle = clean))
        }
        viewModelScope.launch {
            repository.renameVideoTitle(videoId, clean)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            Toast.makeText(getApplication(), "Title updated", Toast.LENGTH_SHORT).show()
        }
    }

    fun batchAddTags(videoIds: List<Long>, tags: List<String>) {
        val cleanTags = tags.map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotBlank() }
        if (cleanTags.isEmpty() || videoIds.isEmpty()) return
        val idSet = videoIds.toSet()
        _allVideos.value = _allVideos.value.map { v ->
            if (idSet.contains(v.id)) {
                val combined = (v.tags + cleanTags).distinctBy { it.lowercase() }
                v.copy(tags = combined)
            } else v
        }
        viewModelScope.launch {
            repository.batchAddTags(videoIds, cleanTags)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            Toast.makeText(getApplication(), "Tags added to ${videoIds.size} videos", Toast.LENGTH_SHORT).show()
        }
    }

    fun batchAssignChannel(videoIds: List<Long>, channelId: Long?, channelName: String? = null) {
        if (videoIds.isEmpty()) return
        val idSet = videoIds.toSet()
        _allVideos.value = _allVideos.value.map { v ->
            if (idSet.contains(v.id)) {
                v.copy(customChannelId = channelId, customChannelName = channelName)
            } else v
        }
        viewModelScope.launch {
            repository.batchAssignChannel(videoIds, channelId)
            val updated = repository.scanVideos(getApplication())
            _allVideos.value = updated
            val msg = if (channelName != null) "Assigned ${videoIds.size} videos to '$channelName'" else "Channel unassigned for ${videoIds.size} videos"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun savePersistentTag(tag: String) {
        viewModelScope.launch {
            repository.savePersistentTag(tag)
        }
    }

    fun shareVideo(context: Context, video: LocalVideo) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, video.uri)
                putExtra(Intent.EXTRA_SUBJECT, video.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share video"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
