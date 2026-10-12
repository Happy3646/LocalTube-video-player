package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomChannelEntity
import com.example.data.local.PlaylistEntity
import com.example.model.LocalVideo
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AssignChannelDialog
import com.example.ui.components.EditDescriptionDialog
import com.example.ui.components.FileInfoDialog
import com.example.ui.components.RenameVideoDialog
import com.example.ui.components.TagFilterChips
import com.example.ui.components.TagManagerDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.components.VideoMenuBottomSheet
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    videos: List<LocalVideo>,
    availableTags: List<String>,
    selectedTag: String?,
    isScanning: Boolean,
    isFeedShuffled: Boolean = false,
    playlists: List<PlaylistEntity>,
    allPersistentTags: List<String>,
    customChannels: List<CustomChannelEntity>,
    onTagSelect: (String?) -> Unit,
    onResetFeedOrder: () -> Unit = {},
    onVideoClick: (LocalVideo) -> Unit,
    onScanClick: () -> Unit,
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
    var selectedVideoIdForMenu by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForTags by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForInfo by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForPlaylist by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForDesc by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForRename by remember { mutableStateOf<Long?>(null) }
    var selectedVideoIdForAssign by remember { mutableStateOf<Long?>(null) }

    val selectedVideoForMenu = remember(selectedVideoIdForMenu, videos) {
        selectedVideoIdForMenu?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForTags = remember(selectedVideoIdForTags, videos) {
        selectedVideoIdForTags?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForInfo = remember(selectedVideoIdForInfo, videos) {
        selectedVideoIdForInfo?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForPlaylist = remember(selectedVideoIdForPlaylist, videos) {
        selectedVideoIdForPlaylist?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForDesc = remember(selectedVideoIdForDesc, videos) {
        selectedVideoIdForDesc?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForRename = remember(selectedVideoIdForRename, videos) {
        selectedVideoIdForRename?.let { id -> videos.firstOrNull { it.id == id } }
    }
    val selectedVideoForAssign = remember(selectedVideoIdForAssign, videos) {
        selectedVideoIdForAssign?.let { id -> videos.firstOrNull { it.id == id } }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen")
    ) {
        // Horizontal Filter Chips below header
        TagFilterChips(
            availableTags = availableTags,
            selectedTag = selectedTag,
            onTagSelect = onTagSelect
        )

        // Shuffled Banner
        if (isFeedShuffled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Feed is shuffled randomly",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onResetFeedOrder,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reset", color = YouTubeRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (videos.isEmpty()) {
            EmptyLibraryState(
                isScanning = isScanning,
                onScanClick = onScanClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Strictly 1 video per row feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_video_feed"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoFeedCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onMenuClick = { selectedVideoIdForMenu = video.id }
                    )
                }
            }
        }
    }

    // Bottom Sheet & Dialogs
    selectedVideoForMenu?.let { video ->
        VideoMenuBottomSheet(
            video = video,
            onDismiss = { selectedVideoIdForMenu = null },
            onAddToPlaylist = { selectedVideoIdForPlaylist = video.id },
            onAssignTags = { selectedVideoIdForTags = video.id },
            onRenameTitle = { selectedVideoIdForRename = video.id },
            onEditDescription = { selectedVideoIdForDesc = video.id },
            onAssignChannel = { selectedVideoIdForAssign = video.id },
            onToggleFavorite = { onToggleFavorite(video.id) },
            onShare = { onShareVideo(video) },
            onFileInfo = { selectedVideoIdForInfo = video.id }
        )
    }

    selectedVideoForRename?.let { video ->
        RenameVideoDialog(
            video = video,
            onDismiss = { selectedVideoIdForRename = null },
            onRename = { newTitle -> onRenameTitle(video.id, newTitle) }
        )
    }

    selectedVideoForTags?.let { video ->
        TagManagerDialog(
            video = video,
            allPersistentTags = allPersistentTags,
            onDismiss = { selectedVideoIdForTags = null },
            onAddTag = { tag -> onAddTag(video.id, tag) },
            onRemoveTag = { tag -> onRemoveTag(video.id, tag) },
            onToggleTag = { tag -> onToggleTag(video.id, tag) }
        )
    }

    selectedVideoForDesc?.let { video ->
        EditDescriptionDialog(
            video = video,
            onDismiss = { selectedVideoIdForDesc = null },
            onSaveDescription = { notes -> onUpdateDescription(video.id, notes) }
        )
    }

    selectedVideoForAssign?.let { video ->
        AssignChannelDialog(
            video = video,
            customChannels = customChannels,
            onDismiss = { selectedVideoIdForAssign = null },
            onAssign = { channelId -> onAssignChannel(video.id, channelId) }
        )
    }

    selectedVideoForInfo?.let { video ->
        FileInfoDialog(
            video = video,
            onDismiss = { selectedVideoIdForInfo = null }
        )
    }

    selectedVideoForPlaylist?.let { video ->
        AddToPlaylistDialog(
            video = video,
            playlists = playlists,
            onDismiss = { selectedVideoIdForPlaylist = null },
            onSelectPlaylist = { playlist -> onAddToPlaylist(playlist.id, video.id) },
            onCreatePlaylist = { name -> onCreatePlaylist(name) }
        )
    }
}

@Composable
private fun EmptyLibraryState(
    isScanning: Boolean,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = YouTubeRed,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Offline Videos Found",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LocalTube plays offline videos stored on your device storage. Make sure your video folders are enabled in Settings, or tap Scan Device below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("empty_scan_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Scan Device")
            }
        }
    }
}
