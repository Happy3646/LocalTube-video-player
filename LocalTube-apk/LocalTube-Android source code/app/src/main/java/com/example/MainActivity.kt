package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.components.LocalTubeBottomNav
import com.example.ui.components.LocalTubeTopBar
import com.example.ui.screens.ChannelDetailScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.YouScreen
import com.example.ui.theme.LocalTubeTheme
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.LocalTubeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LocalTubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            LocalTubeTheme(themeMode = themeMode) {
                LocalTubeApp(viewModel = viewModel, themeMode = themeMode)
            }
        }
    }
}

@Composable
fun LocalTubeApp(
    viewModel: LocalTubeViewModel,
    themeMode: com.example.ui.theme.AppThemeMode = com.example.ui.theme.AppThemeMode.SYSTEM,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Permission handling
    val targetPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, targetPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
        if (isGranted) {
            viewModel.scanDeviceVideos()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasStoragePermission) {
            permissionLauncher.launch(targetPermission)
        }
    }

    // State observation
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val feedVideos by viewModel.feedVideos.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val shortsVideos by viewModel.shortsVideos.collectAsStateWithLifecycle()
    val untaggedVideos by viewModel.untaggedVideos.collectAsStateWithLifecycle()
    val unassignedVideos by viewModel.unassignedVideos.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val customChannels by viewModel.customChannels.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val allPersistentTags by viewModel.allPersistentTags.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val likedShorts by viewModel.likedShorts.collectAsStateWithLifecycle()
    val libraryStats by viewModel.libraryStats.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val folderSettings by viewModel.folderSettings.collectAsStateWithLifecycle()
    val isFeedShuffled by viewModel.isFeedShuffled.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("localtube_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentScreen is CurrentScreen.Main && selectedTab != BottomNavTab.SHORTS) {
                LocalTubeTopBar(
                    searchQuery = searchQuery,
                    isSearchActive = isSearchActive,
                    isScanning = isScanning,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSearchActiveChange = { viewModel.setSearchActive(it) },
                    onMixShuffleClick = {
                        viewModel.shuffleFeed()
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "Home feed shuffled",
                                actionLabel = "Reset Order",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.resetFeedOrder()
                            }
                        }
                    },
                    onScanClick = {
                        if (!hasStoragePermission) {
                            permissionLauncher.launch(targetPermission)
                        } else {
                            viewModel.scanDeviceVideos()
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentScreen is CurrentScreen.Main) {
                LocalTubeBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val screen = currentScreen) {
                is CurrentScreen.Main -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!hasStoragePermission) {
                            StoragePermissionBanner(
                                onRequestPermission = { permissionLauncher.launch(targetPermission) }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                BottomNavTab.HOME -> {
                                    HomeScreen(
                                        videos = feedVideos,
                                        availableTags = allPersistentTags,
                                        selectedTag = selectedTag,
                                        isScanning = isScanning,
                                        isFeedShuffled = isFeedShuffled,
                                        playlists = playlists,
                                        allPersistentTags = allPersistentTags,
                                        customChannels = customChannels,
                                        onTagSelect = { viewModel.setSelectedTag(it) },
                                        onResetFeedOrder = { viewModel.resetFeedOrder() },
                                        onVideoClick = { video -> viewModel.playVideo(video, feedVideos) },
                                        onScanClick = {
                                            if (!hasStoragePermission) {
                                                permissionLauncher.launch(targetPermission)
                                            } else {
                                                viewModel.scanDeviceVideos()
                                            }
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onAddToPlaylist = { playlistId, videoId ->
                                            viewModel.addVideoToPlaylist(playlistId, videoId)
                                        },
                                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                                        onAddTag = { videoId, tag -> viewModel.addTagToVideo(videoId, tag) },
                                        onRemoveTag = { videoId, tag -> viewModel.removeTagFromVideo(videoId, tag) },
                                        onToggleTag = { videoId, tag -> viewModel.toggleTagForVideo(videoId, tag) },
                                        onUpdateDescription = { videoId, desc -> viewModel.updateVideoDescription(videoId, desc) },
                                        onRenameTitle = { videoId, newTitle -> viewModel.renameVideoTitle(videoId, newTitle) },
                                        onAssignChannel = { videoId, channelId -> viewModel.assignVideoToChannel(videoId, channelId) },
                                        onShareVideo = { video -> viewModel.shareVideo(context, video) }
                                    )
                                }

                                BottomNavTab.SHORTS -> {
                                    ShortsScreen(
                                        shortsList = shortsVideos,
                                        likedVideoIds = likedShorts,
                                        onToggleLike = { viewModel.toggleLikeShort(it) },
                                        onShare = { video -> viewModel.shareVideo(context, video) }
                                    )
                                }

                                BottomNavTab.CHANNELS -> {
                                    ChannelsScreen(
                                        channels = channels,
                                        allVideos = allVideos,
                                        onChannelClick = { channelName, customChannelId ->
                                            viewModel.navigateTo(CurrentScreen.ChannelDetail(channelName, customChannelId))
                                        },
                                        onPlayShuffleChannel = { list ->
                                            if (list.isNotEmpty()) {
                                                val shuffled = list.shuffled()
                                                viewModel.playVideo(shuffled.first(), shuffled)
                                            }
                                        },
                                        onVideoClick = { video -> viewModel.playVideo(video, allVideos) },
                                        onCreateChannel = { name, desc, colorHex, iconUri, vids ->
                                            viewModel.createCustomChannel(name, desc, colorHex, iconUri, vids)
                                        },
                                        onDeleteCustomChannel = { channelId ->
                                            viewModel.deleteCustomChannel(channelId)
                                        }
                                    )
                                }

                                BottomNavTab.YOU -> {
                                    YouScreen(
                                        allVideos = allVideos,
                                        untaggedVideos = untaggedVideos,
                                        unassignedVideos = unassignedVideos,
                                        watchHistory = watchHistory,
                                        playlists = playlists,
                                        allPersistentTags = allPersistentTags,
                                        customChannels = customChannels,
                                        stats = libraryStats,
                                        folderSettings = folderSettings,
                                        themeMode = themeMode,
                                        onThemeModeChange = { viewModel.setThemeMode(it) },
                                        onOpenGuide = { viewModel.navigateTo(CurrentScreen.Guide) },
                                        onVideoClick = { video -> viewModel.playVideo(video, allVideos) },
                                        onPlaylistClick = { playlist ->
                                            viewModel.navigateTo(CurrentScreen.PlaylistDetail(playlist))
                                        },
                                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                                        onClearHistory = { viewModel.clearWatchHistory() },
                                        onScanStorage = { viewModel.scanDeviceVideos() },
                                        onToggleFolder = { path, inc -> viewModel.setFolderIncluded(path, inc) },
                                        onAddCustomFolder = { uri -> viewModel.addCustomFolder(uri) },
                                        onDeleteCustomFolder = { path -> viewModel.deleteCustomFolder(path) },
                                        onAddTag = { videoId, tag -> viewModel.addTagToVideo(videoId, tag) },
                                        onRemoveTag = { videoId, tag -> viewModel.removeTagFromVideo(videoId, tag) },
                                        onToggleTag = { videoId, tag -> viewModel.toggleTagForVideo(videoId, tag) },
                                        onAssignChannel = { videoId, channelId -> viewModel.assignVideoToChannel(videoId, channelId) },
                                        onBatchAddTags = { videoIds, tags -> viewModel.batchAddTags(videoIds, tags) },
                                        onBatchAssignChannel = { videoIds, channelId -> viewModel.batchAssignChannel(videoIds, channelId) }
                                    )
                                }
                            }
                        }
                    }
                }

                is CurrentScreen.Player -> {
                    val liveVideo = allVideos.firstOrNull { it.id == screen.video.id } ?: screen.video
                    PlayerScreen(
                        currentVideo = liveVideo,
                        queue = screen.queue,
                        playlists = playlists,
                        allPersistentTags = allPersistentTags,
                        customChannels = customChannels,
                        onBack = { viewModel.navigateBack() },
                        onVideoSelected = { newVideo ->
                            viewModel.playVideo(newVideo, screen.queue)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylist = { playlistId, videoId ->
                            viewModel.addVideoToPlaylist(playlistId, videoId)
                        },
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onAddTag = { videoId, tag -> viewModel.addTagToVideo(videoId, tag) },
                        onRemoveTag = { videoId, tag -> viewModel.removeTagFromVideo(videoId, tag) },
                        onToggleTag = { videoId, tag -> viewModel.toggleTagForVideo(videoId, tag) },
                        onUpdateDescription = { videoId, desc -> viewModel.updateVideoDescription(videoId, desc) },
                        onRenameTitle = { videoId, newTitle -> viewModel.renameVideoTitle(videoId, newTitle) },
                        onAssignChannel = { videoId, channelId -> viewModel.assignVideoToChannel(videoId, channelId) },
                        onTagClick = { tag -> viewModel.filterByTagAndGoHome(tag) },
                        onShareVideo = { video -> viewModel.shareVideo(context, video) },
                        onRecordWatch = { videoId, pos, dur ->
                            viewModel.recordWatchPosition(videoId, pos, dur)
                        }
                    )
                }

                is CurrentScreen.ChannelDetail -> {
                    val customChannel = screen.customChannelId?.let { cid ->
                        customChannels.firstOrNull { it.id == cid }
                    }
                    val channelVideos = remember(screen.channelName, screen.customChannelId, allVideos) {
                        if (screen.customChannelId != null) {
                            allVideos.filter { it.customChannelId == screen.customChannelId }
                        } else {
                            allVideos.filter { it.channelName == screen.channelName }
                        }
                    }

                    ChannelDetailScreen(
                        channelName = screen.channelName,
                        customChannel = customChannel,
                        videos = channelVideos,
                        playlists = playlists,
                        allPersistentTags = allPersistentTags,
                        customChannels = customChannels,
                        onBack = { viewModel.navigateBack() },
                        onVideoClick = { video -> viewModel.playVideo(video, channelVideos) },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playVideo(list.first(), list)
                        },
                        onShuffle = { list ->
                            if (list.isNotEmpty()) viewModel.playVideo(list.first(), list)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylist = { playlistId, videoId ->
                            viewModel.addVideoToPlaylist(playlistId, videoId)
                        },
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onAddTag = { videoId, tag -> viewModel.addTagToVideo(videoId, tag) },
                        onRemoveTag = { videoId, tag -> viewModel.removeTagFromVideo(videoId, tag) },
                        onToggleTag = { videoId, tag -> viewModel.toggleTagForVideo(videoId, tag) },
                        onUpdateDescription = { videoId, desc -> viewModel.updateVideoDescription(videoId, desc) },
                        onRenameTitle = { videoId, newTitle -> viewModel.renameVideoTitle(videoId, newTitle) },
                        onAssignChannel = { videoId, channelId -> viewModel.assignVideoToChannel(videoId, channelId) },
                        onShareVideo = { video -> viewModel.shareVideo(context, video) }
                    )
                }

                is CurrentScreen.PlaylistDetail -> {
                    val playlistItems by viewModel.getPlaylistVideoIds(screen.playlist.id).collectAsStateWithLifecycle(emptyList())
                    val playlistVideos = remember(screen.playlist, allVideos, playlistItems) {
                        val videoMap = allVideos.associateBy { it.id }
                        when {
                            screen.playlist.name.equals("Liked videos", ignoreCase = true) -> {
                                if (playlistItems.isNotEmpty()) {
                                    playlistItems.mapNotNull { videoMap[it] }
                                } else {
                                    allVideos.filter { it.isFavorite }
                                }
                            }
                            screen.playlist.name.equals("Favorites", ignoreCase = true) -> {
                                allVideos.filter { it.isFavorite }
                            }
                            else -> {
                                playlistItems.mapNotNull { videoMap[it] }
                            }
                        }
                    }

                    PlaylistDetailScreen(
                        playlist = screen.playlist,
                        videos = playlistVideos,
                        allPersistentTags = allPersistentTags,
                        customChannels = customChannels,
                        onBack = { viewModel.navigateBack() },
                        onVideoClick = { video -> viewModel.playVideo(video, playlistVideos) },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playVideo(list.first(), list)
                        },
                        onShuffle = { list ->
                            if (list.isNotEmpty()) viewModel.playVideo(list.first(), list)
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist(screen.playlist.id)
                            viewModel.navigateBack()
                        },
                        onRemoveFromPlaylist = { videoId ->
                            viewModel.removeVideoFromPlaylist(screen.playlist.id, videoId)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddTag = { videoId, tag -> viewModel.addTagToVideo(videoId, tag) },
                        onRemoveTag = { videoId, tag -> viewModel.removeTagFromVideo(videoId, tag) },
                        onToggleTag = { videoId, tag -> viewModel.toggleTagForVideo(videoId, tag) },
                        onUpdateDescription = { videoId, desc -> viewModel.updateVideoDescription(videoId, desc) },
                        onAssignChannel = { videoId, channelId -> viewModel.assignVideoToChannel(videoId, channelId) },
                        onShareVideo = { video -> viewModel.shareVideo(context, video) }
                    )
                }

                is CurrentScreen.Guide -> {
                    GuideScreen(
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoragePermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF261210))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("storage_permission_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = YouTubeRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Storage Permission Required",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    text = "Grant permission to discover and play videos stored on your device.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("grant_permission_button")
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
