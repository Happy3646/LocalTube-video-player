package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.model.LocalVideo
import com.example.ui.components.ChannelAvatar
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.ChannelItem
import kotlin.math.abs

@Composable
fun ChannelsScreen(
    channels: List<ChannelItem>,
    allVideos: List<LocalVideo>,
    onChannelClick: (String, Long?) -> Unit,
    onPlayShuffleChannel: (List<LocalVideo>) -> Unit,
    onVideoClick: (LocalVideo) -> Unit,
    onCreateChannel: (String, String, String, String?, List<Long>) -> Unit,
    onDeleteCustomChannel: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No channels yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Text("Create Custom Channel")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("channels_list"),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header item: "Create Channel" banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Channels & Folders (${channels.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        OutlinedButton(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.testTag("create_channel_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Channel", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                items(channels, key = { "${it.name}_${it.channelId ?: it.name}" }) { channel ->
                    val channelVideos = remember(channel.name, channel.channelId, allVideos) {
                        if (channel.isCustom && channel.channelId != null) {
                            allVideos.filter { it.customChannelId == channel.channelId }
                        } else {
                            allVideos.filter { it.channelName == channel.name }
                        }
                    }

                    ChannelCard(
                        channel = channel,
                        videos = channelVideos,
                        onChannelClick = { onChannelClick(channel.name, channel.channelId) },
                        onShuffleClick = { onPlayShuffleChannel(channelVideos) },
                        onVideoClick = onVideoClick,
                        onDeleteClick = if (channel.isCustom && channel.channelId != null) {
                            { onDeleteCustomChannel(channel.channelId) }
                        } else null
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = YouTubeRed,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("create_channel_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Channel",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showCreateDialog) {
        CreateChannelDialog(
            allVideos = allVideos,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, colorHex, iconUri, vids ->
                onCreateChannel(name, desc, colorHex, iconUri, vids)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ChannelCard(
    channel: ChannelItem,
    videos: List<LocalVideo>,
    onChannelClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onVideoClick: (LocalVideo) -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hash = abs(channel.name.hashCode())
    val gradientColors = remember(channel.name, channel.colorHex) {
        if (!channel.colorHex.isNullOrBlank()) {
            try {
                val parsed = Color(android.graphics.Color.parseColor(channel.colorHex))
                listOf(parsed, parsed.copy(alpha = 0.6f))
            } catch (e: Exception) {
                listOf(Color(0xFF283593), Color(0xFF1E88E5))
            }
        } else {
            val palettes = listOf(
                listOf(Color(0xFF283593), Color(0xFF1E88E5)),
                listOf(Color(0xFFAD1457), Color(0xFFE91E63)),
                listOf(Color(0xFF00695C), Color(0xFF26A69A)),
                listOf(Color(0xFFD84315), Color(0xFFFF7043)),
                listOf(Color(0xFF4527A0), Color(0xFF7E57C2)),
                listOf(Color(0xFF37474F), Color(0xFF78909C))
            )
            palettes[hash % palettes.size]
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onChannelClick)
            .testTag("channel_card_${channel.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Channel Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(Brush.horizontalGradient(gradientColors))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (channel.isCustom) "CUSTOM CREATOR" else "LOCAL FOLDER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Channel Header row: Avatar + Name + Subscribers/Videos count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Polished Avatar: photo if set, else gradient letter initial
                ChannelAvatar(
                    name = channel.name,
                    iconUri = channel.iconUri,
                    colorHex = channel.colorHex,
                    size = 46.dp,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = channel.subscriberSubline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (channel.description.isNotBlank()) {
                        Text(
                            text = channel.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete custom channel if applicable
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom channel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Shuffle button
                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.testTag("channel_shuffle_${channel.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle channel videos",
                        tint = YouTubeRed
                    )
                }
            }

            // Horizontal Preview strip of videos in this channel
            if (videos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videos.take(6)) { vid ->
                        val thumbRequest = remember(vid.uri) {
                            ImageRequest.Builder(context)
                                .data(vid.uri)
                                .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                                .videoFrameMillis(1000)
                                .crossfade(true)
                                .build()
                        }

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable { onVideoClick(vid) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                ) {
                                    AsyncImage(
                                        model = thumbRequest,
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
                                }
                                Text(
                                    text = vid.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChannelDialog(
    allVideos: List<LocalVideo>,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, colorHex: String, iconUri: String?, selectedVideos: List<Long>) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var channelDescription by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FF0000") }
    var selectedIconUri by remember { mutableStateOf<String?>(null) }
    val selectedVideoIds = remember { mutableStateListOf<Long>() }

    // Android Photo Picker launcher (zero-permission, compliant with Play Store guidelines)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedIconUri = uri.toString()
        }
    }

    val presetColors = listOf(
        "#FF0000", // YouTube Red
        "#2196F3", // Blue
        "#4CAF50", // Green
        "#9C27B0", // Purple
        "#FF9800", // Amber
        "#009688"  // Teal
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = "Create Creator Channel",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Name Field
                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    label = { Text("Channel Name") },
                    placeholder = { Text("e.g. Action Clips, Tutorials") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_channel_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Field
                OutlinedTextField(
                    value = channelDescription,
                    onValueChange = { channelDescription = it },
                    label = { Text("Description / Bio") },
                    placeholder = { Text("What is this channel about?") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_channel_bio_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar & Color Picker Row
                Text(
                    text = "Channel Icon & Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Avatar Preview
                    ChannelAvatar(
                        name = if (channelName.isNotBlank()) channelName else "Channel",
                        iconUri = selectedIconUri,
                        colorHex = selectedColorHex,
                        size = 50.dp,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Pick Custom Photo Button
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("pick_channel_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedIconUri == null) "Pick Photo" else "Change Photo", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Colors Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Assign Videos to Channel Section
                Text(
                    text = "Assign Local Videos (${selectedVideoIds.size} selected)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (allVideos.isEmpty()) {
                    Text(
                        text = "No local videos available to assign.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        items(allVideos) { video ->
                            val isChecked = selectedVideoIds.contains(video.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedVideoIds.remove(video.id)
                                        else selectedVideoIds.add(video.id)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        if (check) selectedVideoIds.add(video.id)
                                        else selectedVideoIds.remove(video.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = YouTubeRed,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${video.channelName} • ${video.formattedDuration}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (channelName.isNotBlank()) {
                        onCreate(
                            channelName.trim(),
                            channelDescription.trim(),
                            selectedColorHex,
                            selectedIconUri,
                            selectedVideoIds.toList()
                        )
                    }
                },
                enabled = channelName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                modifier = Modifier.testTag("confirm_create_channel_button")
            ) {
                Text("Create Channel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
