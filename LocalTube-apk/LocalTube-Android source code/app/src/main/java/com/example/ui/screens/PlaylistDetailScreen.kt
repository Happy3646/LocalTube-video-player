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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomChannelEntity
import com.example.data.local.PlaylistEntity
import com.example.model.LocalVideo
import com.example.ui.components.AssignChannelDialog
import com.example.ui.components.EditDescriptionDialog
import com.example.ui.components.FileInfoDialog
import com.example.ui.components.TagManagerDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.components.VideoMenuBottomSheet
import com.example.ui.theme.YouTubeRed

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    videos: List<LocalVideo>,
    allPersistentTags: List<String>,
    customChannels: List<CustomChannelEntity>,
    onBack: () -> Unit,
    onVideoClick: (LocalVideo) -> Unit,
    onPlayAll: (List<LocalVideo>) -> Unit,
    onShuffle: (List<LocalVideo>) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveFromPlaylist: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddTag: (Long, String) -> Unit,
    onRemoveTag: (Long, String) -> Unit,
    onToggleTag: (Long, String) -> Unit,
    onUpdateDescription: (Long, String) -> Unit,
    onAssignChannel: (Long, Long?) -> Unit,
    onShareVideo: (LocalVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedVideoForMenu by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForTags by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForInfo by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForDesc by remember { mutableStateOf<LocalVideo?>(null) }
    var selectedVideoForAssign by remember { mutableStateOf<LocalVideo?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("playlist_detail_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("playlist_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            if (!playlist.isDefault) {
                IconButton(onClick = onDeletePlaylist, modifier = Modifier.testTag("playlist_delete_button")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete playlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${videos.size} ${if (videos.size == 1) "video" else "videos"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (videos.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onPlayAll(videos) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).testTag("playlist_play_all")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play All", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onShuffle(videos.shuffled()) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).testTag("playlist_shuffle_all")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shuffle", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        Text(
                            text = "No videos in this playlist yet. Add videos from the feed via the 3-dot menu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(videos, key = { it.id }) { video ->
                VideoFeedCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onMenuClick = { selectedVideoForMenu = video }
                )
            }
        }
    }

    selectedVideoForMenu?.let { video ->
        VideoMenuBottomSheet(
            video = video,
            onDismiss = { selectedVideoForMenu = null },
            onAddToPlaylist = {},
            onAssignTags = { selectedVideoForTags = video },
            onEditDescription = { selectedVideoForDesc = video },
            onAssignChannel = { selectedVideoForAssign = video },
            onToggleFavorite = { onToggleFavorite(video.id) },
            onShare = { onShareVideo(video) },
            onFileInfo = { selectedVideoForInfo = video }
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
}
