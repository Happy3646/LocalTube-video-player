package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.material.icons.filled.Folder
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.local.CustomChannelEntity
import com.example.data.local.FolderSettingEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.LocalVideo
import com.example.ui.components.AssignChannelDialog
import com.example.ui.components.BatchAssignChannelDialog
import com.example.ui.components.BatchTagDialog
import com.example.ui.components.ManageFoldersDialog
import com.example.ui.components.TagManagerDialog
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.semantics.Role
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.LibraryStats

enum class OrganizationTab {
    UNTAGGED,
    UNASSIGNED
}

@Composable
fun YouScreen(
    allVideos: List<LocalVideo>,
    untaggedVideos: List<LocalVideo>,
    unassignedVideos: List<LocalVideo>,
    watchHistory: List<WatchHistoryEntity>,
    playlists: List<PlaylistEntity>,
    allPersistentTags: List<String>,
    customChannels: List<CustomChannelEntity>,
    stats: LibraryStats,
    folderSettings: List<FolderSettingEntity>,
    onVideoClick: (LocalVideo) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onClearHistory: () -> Unit,
    onScanStorage: () -> Unit,
    onToggleFolder: (String, Boolean) -> Unit,
    onAddCustomFolder: (Uri) -> Unit,
    onDeleteCustomFolder: (String) -> Unit,
    onAddTag: (Long, String) -> Unit,
    onRemoveTag: (Long, String) -> Unit,
    onToggleTag: (Long, String) -> Unit,
    onAssignChannel: (Long, Long?) -> Unit,
    onBatchAddTags: (List<Long>, List<String>) -> Unit = { _, _ -> },
    onBatchAssignChannel: (List<Long>, Long?) -> Unit = { _, _ -> },
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onOpenGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }
    var showConfirmClearHistory by remember { mutableStateOf(false) }
    var showManageFoldersDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var selectedOrgTab by remember { mutableStateOf(OrganizationTab.UNTAGGED) }
    var videoIdForTagging by remember { mutableStateOf<Long?>(null) }
    var videoIdForChannelAssignment by remember { mutableStateOf<Long?>(null) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedBatchVideoIds = remember { mutableStateListOf<Long>() }
    var showBatchTagDialog by remember { mutableStateOf(false) }
    var showBatchAssignDialog by remember { mutableStateOf(false) }

    val videoForTagging = remember(videoIdForTagging, allVideos) {
        videoIdForTagging?.let { id -> allVideos.firstOrNull { it.id == id } }
    }
    val videoForChannelAssignment = remember(videoIdForChannelAssignment, allVideos) {
        videoIdForChannelAssignment?.let { id -> allVideos.firstOrNull { it.id == id } }
    }

    val historyVideos = remember(watchHistory, allVideos) {
        val videoMap = allVideos.associateBy { it.id }
        watchHistory.mapNotNull { hist ->
            videoMap[hist.videoId]?.copy(lastWatchedPositionMs = hist.lastPositionMs)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("you_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // User Profile Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "LocalTube Library",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "@device_storage • 100% Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: Organization Hub (Untagged & Unassigned Filter Hub with Batch Selection)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Organization Hub",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val currentList = if (selectedOrgTab == OrganizationTab.UNTAGGED) untaggedVideos else unassignedVideos
                    if (currentList.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                isMultiSelectMode = !isMultiSelectMode
                                if (!isMultiSelectMode) {
                                    selectedBatchVideoIds.clear()
                                }
                            }
                        ) {
                            Text(
                                text = if (isMultiSelectMode) "Done" else "Select Multiple",
                                color = if (isMultiSelectMode) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Organization Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrgFilterChip(
                        label = "Without Tags (${untaggedVideos.size})",
                        isSelected = selectedOrgTab == OrganizationTab.UNTAGGED,
                        onClick = {
                            selectedOrgTab = OrganizationTab.UNTAGGED
                            selectedBatchVideoIds.clear()
                        },
                        testTag = "org_tab_untagged"
                    )

                    OrgFilterChip(
                        label = "Without Channel (${unassignedVideos.size})",
                        isSelected = selectedOrgTab == OrganizationTab.UNASSIGNED,
                        onClick = {
                            selectedOrgTab = OrganizationTab.UNASSIGNED
                            selectedBatchVideoIds.clear()
                        },
                        testTag = "org_tab_unassigned"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-select batch actions toolbar
                if (isMultiSelectMode) {
                    val currentList = if (selectedOrgTab == OrganizationTab.UNTAGGED) untaggedVideos else unassignedVideos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    if (selectedBatchVideoIds.size == currentList.size) {
                                        selectedBatchVideoIds.clear()
                                    } else {
                                        selectedBatchVideoIds.clear()
                                        selectedBatchVideoIds.addAll(currentList.map { it.id })
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (selectedBatchVideoIds.size == currentList.size) "Deselect All" else "Select All",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${selectedBatchVideoIds.size}/${currentList.size} selected",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedOrgTab == OrganizationTab.UNTAGGED) {
                                    showBatchTagDialog = true
                                } else {
                                    showBatchAssignDialog = true
                                }
                            },
                            enabled = selectedBatchVideoIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedOrgTab == OrganizationTab.UNTAGGED) "Batch Add Tags" else "Assign Channel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                when (selectedOrgTab) {
                    OrganizationTab.UNTAGGED -> {
                        if (untaggedVideos.isEmpty()) {
                            AllOrganizedCard(message = "All videos have tags assigned!")
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(untaggedVideos) { video ->
                                    val isSelected = selectedBatchVideoIds.contains(video.id)
                                    OrgVideoCard(
                                        video = video,
                                        actionLabel = "Tag Now",
                                        actionIcon = Icons.Default.Add,
                                        isMultiSelectMode = isMultiSelectMode,
                                        isSelected = isSelected,
                                        onToggleSelect = {
                                            if (isSelected) selectedBatchVideoIds.remove(video.id)
                                            else selectedBatchVideoIds.add(video.id)
                                        },
                                        onLongClick = {
                                            isMultiSelectMode = true
                                            if (!isSelected) selectedBatchVideoIds.add(video.id)
                                        },
                                        onAction = { videoIdForTagging = video.id },
                                        onCardClick = {
                                            if (isMultiSelectMode) {
                                                if (isSelected) selectedBatchVideoIds.remove(video.id)
                                                else selectedBatchVideoIds.add(video.id)
                                            } else {
                                                onVideoClick(video)
                                            }
                                        },
                                        testTag = "untagged_item_${video.id}"
                                    )
                                }
                            }
                        }
                    }

                    OrganizationTab.UNASSIGNED -> {
                        if (unassignedVideos.isEmpty()) {
                            AllOrganizedCard(message = "All videos are organized into custom channels!")
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(unassignedVideos) { video ->
                                    val isSelected = selectedBatchVideoIds.contains(video.id)
                                    OrgVideoCard(
                                        video = video,
                                        actionLabel = "Assign Channel",
                                        actionIcon = Icons.Default.Subscriptions,
                                        isMultiSelectMode = isMultiSelectMode,
                                        isSelected = isSelected,
                                        onToggleSelect = {
                                            if (isSelected) selectedBatchVideoIds.remove(video.id)
                                            else selectedBatchVideoIds.add(video.id)
                                        },
                                        onLongClick = {
                                            isMultiSelectMode = true
                                            if (!isSelected) selectedBatchVideoIds.add(video.id)
                                        },
                                        onAction = { videoIdForChannelAssignment = video.id },
                                        onCardClick = {
                                            if (isMultiSelectMode) {
                                                if (isSelected) selectedBatchVideoIds.remove(video.id)
                                                else selectedBatchVideoIds.add(video.id)
                                            } else {
                                                onVideoClick(video)
                                            }
                                        },
                                        testTag = "unassigned_item_${video.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Watch History
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (historyVideos.isNotEmpty()) {
                        TextButton(onClick = { showConfirmClearHistory = true }) {
                            Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (historyVideos.isEmpty()) {
                    Text(
                        text = "No watch history yet. Videos you play will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(historyVideos) { vid ->
                            val thumbReq = remember(vid.uri) {
                                ImageRequest.Builder(context)
                                    .data(vid.uri)
                                    .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                                    .videoFrameMillis(1000)
                                    .crossfade(true)
                                    .build()
                            }

                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clickable { onVideoClick(vid) }
                                    .testTag("history_item_${vid.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    AsyncImage(
                                        model = thumbReq,
                                        contentDescription = vid.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color(0xCC000000), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = vid.formattedDuration,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = Color.White
                                            )
                                        )
                                    }

                                    if (vid.lastWatchedPositionMs > 0 && vid.durationMs > 0) {
                                        val p = (vid.lastWatchedPositionMs.toFloat() / vid.durationMs.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { p },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.5.dp)
                                                .align(Alignment.BottomCenter),
                                            color = YouTubeRed,
                                            trackColor = Color(0x44FFFFFF)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = vid.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${vid.effectiveChannelName} • ${vid.formattedViews}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Playlists
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    TextButton(onClick = { showCreatePlaylistDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val sortedPlaylists = remember(playlists) {
                    playlists.sortedWith(
                        compareByDescending<PlaylistEntity> { it.name.equals("Liked videos", ignoreCase = true) }
                            .thenByDescending { it.isDefault }
                            .thenBy { it.name }
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sortedPlaylists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }
        }

        // Section 4: Library Stats
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Library Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Videos",
                        value = "${stats.totalVideos}",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Storage",
                        value = stats.formattedTotalSize,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Shorts",
                        value = "${stats.shortsCount}",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Channels",
                        value = "${stats.channelsCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 5: Handbook & Interactive Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenGuide)
                    .testTag("you_open_guide_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(YouTubeRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LocalTube Handbook",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(YouTubeRed)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "GUIDE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Interactive handbook: Player gestures, Shorts, Playlists & button dictionary",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open handbook",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section 6: Offline Settings & Tools
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Offline Settings & Tools",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "User Guide & Button Dictionary",
                            subtitle = "Interactive handbook with gesture tutorials & icon reference",
                            onClick = onOpenGuide,
                            testTag = "settings_user_guide"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Default.Palette,
                            title = "Appearance",
                            subtitle = themeMode.displayName,
                            onClick = { showThemeDialog = true },
                            testTag = "settings_theme_switcher"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Default.Folder,
                            title = "Manage Video Folders",
                            subtitle = "${folderSettings.count { it.isIncluded }} of ${folderSettings.size} folders included (tap to manage)",
                            onClick = { showManageFoldersDialog = true },
                            testTag = "settings_manage_folders"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Default.Refresh,
                            title = "Rescan Device Storage",
                            subtitle = "Update video index from MediaStore and custom folders",
                            onClick = onScanStorage,
                            testTag = "settings_rescan"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Default.Delete,
                            title = "Clear Watch History",
                            subtitle = "Reset progress and recently played records",
                            onClick = { showConfirmClearHistory = true },
                            testTag = "settings_clear_history"
                        )
                    }
                }
            }
        }
    }

    // Interactive Tag Manager Dialog for "Tag Now"
    videoForTagging?.let { video ->
        TagManagerDialog(
            video = video,
            allPersistentTags = allPersistentTags,
            onDismiss = { videoIdForTagging = null },
            onAddTag = { tag -> onAddTag(video.id, tag) },
            onRemoveTag = { tag -> onRemoveTag(video.id, tag) },
            onToggleTag = { tag -> onToggleTag(video.id, tag) }
        )
    }

    // Assign Channel Dialog for "Assign Channel"
    videoForChannelAssignment?.let { video ->
        AssignChannelDialog(
            video = video,
            customChannels = customChannels,
            onDismiss = { videoIdForChannelAssignment = null },
            onAssign = { channelId -> onAssignChannel(video.id, channelId) }
        )
    }

    // Choose Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = mode == themeMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        onThemeModeChange(mode)
                                        showThemeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = YouTubeRed,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (mode.subtitle.isNotBlank()) {
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel", color = YouTubeRed)
                }
            }
        )
    }

    // Create playlist dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = "New Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistTitle,
                    onValueChange = { newPlaylistTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistTitle.isNotBlank()) {
                            onCreatePlaylist(newPlaylistTitle)
                            newPlaylistTitle = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showConfirmClearHistory) {
        AlertDialog(
            onDismissRequest = { showConfirmClearHistory = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = "Clear watch history?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "This will remove all videos from your watch history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showConfirmClearHistory = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearHistory = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showManageFoldersDialog) {
        ManageFoldersDialog(
            folderSettings = folderSettings,
            onDismiss = { showManageFoldersDialog = false },
            onToggleFolder = onToggleFolder,
            onAddCustomFolder = onAddCustomFolder,
            onDeleteCustomFolder = onDeleteCustomFolder
        )
    }

    if (showBatchTagDialog) {
        BatchTagDialog(
            selectedCount = selectedBatchVideoIds.size,
            allPersistentTags = allPersistentTags,
            onDismiss = { showBatchTagDialog = false },
            onApplyTags = { tags ->
                onBatchAddTags(selectedBatchVideoIds.toList(), tags)
                selectedBatchVideoIds.clear()
                isMultiSelectMode = false
                showBatchTagDialog = false
            }
        )
    }

    if (showBatchAssignDialog) {
        BatchAssignChannelDialog(
            selectedCount = selectedBatchVideoIds.size,
            customChannels = customChannels,
            onDismiss = { showBatchAssignDialog = false },
            onAssign = { channelId, _ ->
                onBatchAssignChannel(selectedBatchVideoIds.toList(), channelId)
                selectedBatchVideoIds.clear()
                isMultiSelectMode = false
                showBatchAssignDialog = false
            }
        )
    }
}

@Composable
private fun OrgFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun OrgVideoCard(
    video: LocalVideo,
    actionLabel: String,
    actionIcon: ImageVector,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onAction: () -> Unit,
    onCardClick: () -> Unit,
    testTag: String
) {
    val context = LocalContext.current
    val thumbReq = remember(video.uri) {
        ImageRequest.Builder(context)
            .data(video.uri)
            .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
            .videoFrameMillis(1000)
            .crossfade(true)
            .build()
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onCardClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) YouTubeRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = thumbReq,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isMultiSelectMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) YouTubeRed else Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = video.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = video.channelName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isMultiSelectMode) {
                OutlinedButton(
                    onClick = onToggleSelect,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSelected) "Deselect" else "Select",
                        fontSize = 11.sp,
                        color = if (isSelected) YouTubeRed else Color.White
                    )
                }
            } else {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AllOrganizedCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .testTag("playlist_card_${playlist.name}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        playlist.name.equals("Liked videos", ignoreCase = true) -> Icons.Default.ThumbUp
                        playlist.name == "Favorites" -> Icons.Default.Favorite
                        playlist.name == "Watch Later" -> Icons.Default.Bookmark
                        else -> Icons.Default.PlaylistPlay
                    },
                    contentDescription = null,
                    tint = if (playlist.name.equals("Liked videos", ignoreCase = true) || playlist.name == "Favorites") YouTubeRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (playlist.isDefault) "Default" else "Custom",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YouTubeRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
