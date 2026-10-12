package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CustomChannelEntity
import com.example.data.local.PlaylistEntity
import com.example.model.LocalVideo
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AssignChannelDialog
import com.example.ui.components.ChannelAvatar
import com.example.ui.components.EditDescriptionDialog
import com.example.ui.components.FileInfoDialog
import com.example.ui.components.TagManagerDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.components.VideoMenuBottomSheet
import com.example.ui.theme.YouTubeRed
import kotlin.math.abs

import androidx.compose.foundation.clickable
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.components.RenameVideoDialog

enum class ChannelTab {
    VIDEOS,
    SHORTS
}

@Composable
fun ChannelDetailScreen(
    channelName: String,
    customChannel: CustomChannelEntity?,
    videos: List<LocalVideo>,
    playlists: List<PlaylistEntity>,
    allPersistentTags: List<String>,
    customChannels: List<CustomChannelEntity>,
    onBack: () -> Unit,
    onVideoClick: (LocalVideo) -> Unit,
    onPlayAll: (List<LocalVideo>) -> Unit,
    onShuffle: (List<LocalVideo>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddTag: (Long, String) -> Unit,
    onRemoveTag: (Long, String) -> Unit,
    onToggleTag: (Long, String) -> Unit,
    onUpdateDescription: (Long, String) -> Unit,
    onRenameTitle: (Long, String) -> Unit = { _, _ -> },
    onAssignChannel: (Long, Long?) -> Unit,
    onShareVideo: (LocalVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf(ChannelTab.VIDEOS) }

    val regularVideos = remember(videos) {
        videos.filter { video ->
            !video.tags.any { tag -> tag.equals("shorts", ignoreCase = true) || tag.equals("short", ignoreCase = true) }
        }
    }

    val shortsVideos = remember(videos) {
        videos.filter { video ->
            video.tags.any { tag -> tag.equals("shorts", ignoreCase = true) || tag.equals("short", ignoreCase = true) }
        }
    }

    var selectedVideoForMenu by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForTags by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForInfo by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForPlaylist by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForDesc by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForRename by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForAssign by remember { mutableStateOf<LocalVideo?>(null) }

    val hash = abs(channelName.hashCode())
    val gradientColors = remember(channelName, customChannel?.colorHex) {
        if (!customChannel?.colorHex.isNullOrBlank()) {
            try {
                val parsed = Color(android.graphics.Color.parseColor(customChannel.colorHex))
                listOf(parsed, parsed.copy(alpha = 0.6f))
            } catch (e: Exception) {
                listOf(Color(0xFF283593), Color(0xFF1E88E5))
            }
        } else {
            val palettes = listOf(
                listOf(Color(0xFF283593), Color(0xFF1E88E5)),
                listOf(Color(0xFFAD1457), Color(0xFFE91E63)),
                listOf(Color(0xFF00695C), Color(0xFF26A69A)),
                listOf(Color(0xFFD84315), Color(0xFFFF7043))
            )
            palettes[hash % palettes.size]
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("channel_detail_screen")
    ) {
        // Channel detail top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("channel_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = channelName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Channel Banner and Avatar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Brush.horizontalGradient(gradientColors))
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Polished custom avatar with photo support and fallback gradient initial
                        ChannelAvatar(
                            name = channelName,
                            iconUri = customChannel?.iconUri,
                            colorHex = customChannel?.colorHex,
                            size = 56.dp,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = channelName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@${channelName.replace(" ", "")} • ${videos.size} videos • ${if (customChannel != null) "Custom Channel" else "Folder Channel"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (customChannel?.description?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = customChannel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Play All & Shuffle Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPlayAll(videos) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).testTag("channel_play_all")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onShuffle(videos.shuffled()) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).testTag("channel_shuffle_all")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle", color = Color.White)
                        }
                    }

                    // 2-Tab Navigation (Videos & Shorts)
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = YouTubeRed
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == ChannelTab.VIDEOS,
                            onClick = { selectedTab = ChannelTab.VIDEOS },
                            text = {
                                Text(
                                    text = "Videos (${regularVideos.size})",
                                    fontWeight = if (selectedTab == ChannelTab.VIDEOS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == ChannelTab.VIDEOS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("channel_tab_videos")
                        )
                        Tab(
                            selected = selectedTab == ChannelTab.SHORTS,
                            onClick = { selectedTab = ChannelTab.SHORTS },
                            text = {
                                Text(
                                    text = "Shorts (${shortsVideos.size})",
                                    fontWeight = if (selectedTab == ChannelTab.SHORTS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == ChannelTab.SHORTS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("channel_tab_shorts")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            val displayedVideos = if (selectedTab == ChannelTab.VIDEOS) regularVideos else shortsVideos

            if (displayedVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == ChannelTab.VIDEOS)
                                "No long-form videos found in this channel."
                            else
                                "No Shorts in this channel.\nAdd the #shorts tag to any video to watch it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayedVideos, key = { it.id }) { video ->
                    VideoFeedCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onMenuClick = { selectedVideoForMenu = video }
                    )
                }
            }
        }
    }

    // Dialogs
    selectedVideoForMenu?.let { video ->
        VideoMenuBottomSheet(
            video = video,
            onDismiss = { selectedVideoForMenu = null },
            onAddToPlaylist = { selectedVideoForPlaylist = video },
            onAssignTags = { selectedVideoForTags = video },
            onRenameTitle = { selectedVideoForRename = video },
            onEditDescription = { selectedVideoForDesc = video },
            onAssignChannel = { selectedVideoForAssign = video },
            onToggleFavorite = { onToggleFavorite(video.id) },
            onShare = { onShareVideo(video) },
            onFileInfo = { selectedVideoForInfo = video }
        )
    }

    selectedVideoForRename?.let { video ->
        RenameVideoDialog(
            video = video,
            onDismiss = { selectedVideoForRename = null },
            onRename = { newTitle -> onRenameTitle(video.id, newTitle) }
        )
    }

    selectedVideoForTags?.let { video ->
        TagManagerDialog(
            video = video,
            allPersistentTags = allPersistentTags,
            onDismiss = { selectedVideoForTags = null },
            onAddTag = { tag -> onAddTag(video.id, tag) },
            onRemoveTag = { tag -> onRemoveTag(video.id, tag) },
            onToggleTag = { tag -> onToggleTag(video.id, tag) }
        )
    }

    selectedVideoForDesc?.let { video ->
        EditDescriptionDialog(
            video = video,
            onDismiss = { selectedVideoForDesc = null },
            onSaveDescription = { notes -> onUpdateDescription(video.id, notes) }
        )
    }

    selectedVideoForAssign?.let { video ->
        AssignChannelDialog(
            video = video,
            customChannels = customChannels,
            onDismiss = { selectedVideoForAssign = null },
            onAssign = { channelId -> onAssignChannel(video.id, channelId) }
        )
    }

    selectedVideoForInfo?.let { video ->
        FileInfoDialog(
            video = video,
            onDismiss = { selectedVideoForInfo = null }
        )
    }

    selectedVideoForPlaylist?.let { video ->
        AddToPlaylistDialog(
            video = video,
            playlists = playlists,
            onDismiss = { selectedVideoForPlaylist = null },
            onSelectPlaylist = { playlist -> onAddToPlaylist(playlist.id, video.id) },
            onCreatePlaylist = { name -> onCreatePlaylist(name) }
        )
    }
}
