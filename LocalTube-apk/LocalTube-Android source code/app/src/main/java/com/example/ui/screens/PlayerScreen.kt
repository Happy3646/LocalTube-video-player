@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.local.CustomChannelEntity
import com.example.data.local.PlaylistEntity
import com.example.model.LocalVideo
import com.example.player.AspectRatioMode
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AssignChannelDialog
import com.example.ui.components.ChannelAvatar
import com.example.ui.components.EditDescriptionDialog
import com.example.ui.components.FileInfoDialog
import com.example.ui.components.RenameVideoDialog
import com.example.ui.components.TagManagerDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.components.VideoMenuBottomSheet
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@Composable
fun PlayerScreen(
    currentVideo: LocalVideo,
    queue: List<LocalVideo>,
    playlists: List<PlaylistEntity>,
    allPersistentTags: List<String>,
    customChannels: List<CustomChannelEntity>,
    onBack: () -> Unit,
    onVideoSelected: (LocalVideo) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddTag: (Long, String) -> Unit,
    onRemoveTag: (Long, String) -> Unit,
    onToggleTag: (Long, String) -> Unit,
    onUpdateDescription: (Long, String) -> Unit,
    onRenameTitle: (Long, String) -> Unit = { _, _ -> },
    onAssignChannel: (Long, Long?) -> Unit,
    onTagClick: (String) -> Unit,
    onShareVideo: (LocalVideo) -> Unit,
    onRecordWatch: (Long, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(currentVideo.durationMs) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.FIT) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Description expansion
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    // Dialog state
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTargetVideo by remember { mutableStateOf<LocalVideo?>(null) }
    var showAssignChannelDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDoubleTapSkipLeft by remember { mutableStateOf(false) }
    var showDoubleTapSkipRight by remember { mutableStateOf(false) }
    var overflowVideoForMenu by remember { mutableStateOf<LocalVideo?>(null) }

    // ExoPlayer creation
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load media item
    LaunchedEffect(currentVideo.uri) {
        val mediaItem = MediaItem.fromUri(currentVideo.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (currentVideo.lastWatchedPositionMs > 0 && currentVideo.lastWatchedPositionMs < currentVideo.durationMs - 3000) {
            exoPlayer.seekTo(currentVideo.lastWatchedPositionMs)
        }
        exoPlayer.play()
        isPlaying = true
    }

    // Sync playback speed
    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    // Update position and record watch history periodically
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                currentPositionMs = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0) {
                    durationMs = dur
                }
                onRecordWatch(currentVideo.id, currentPositionMs, durationMs)
            }
            delay(500)
        }
    }

    // Controls auto-hide timer
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying) {
            delay(3500)
            areControlsVisible = false
        }
    }

    // Handle system bars and orientation on fullscreen toggle
    LaunchedEffect(isFullscreen) {
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Cleanup when leaving player
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                val window = act.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            exoPlayer.release()
        }
    }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    val relatedVideos = remember(queue, currentVideo.id) {
        queue.filter { it.id != currentVideo.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("player_screen")
    ) {
        // Player Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFullscreen) Modifier.fillMaxSize()
                    else Modifier
                        .statusBarsPadding()
                        .aspectRatio(16f / 9f)
                )
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.4f) {
                                val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                                showDoubleTapSkipLeft = true
                            } else if (offset.x > width * 0.6f) {
                                val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                                showDoubleTapSkipRight = true
                            }
                        },
                        onTap = {
                            areControlsVisible = !areControlsVisible
                        }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = when (aspectRatioMode) {
                            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { pv ->
                    pv.resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Double tap -10s indicator
            if (showDoubleTapSkipLeft) {
                LaunchedEffect(Unit) {
                    delay(700)
                    showDoubleTapSkipLeft = false
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 32.dp)
                        .size(80.dp)
                        .background(Color(0x88000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Replay10, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Text("-10s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Double tap +10s indicator
            if (showDoubleTapSkipRight) {
                LaunchedEffect(Unit) {
                    delay(700)
                    showDoubleTapSkipRight = false
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp)
                        .size(80.dp)
                        .background(Color(0x88000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Text("+10s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Controls Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(
                                horizontal = if (isFullscreen) 16.dp else 8.dp,
                                vertical = if (isFullscreen) 10.dp else 6.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isFullscreen) isFullscreen = false else onBack()
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = currentVideo.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                aspectRatioMode = when (aspectRatioMode) {
                                    AspectRatioMode.FIT -> AspectRatioMode.FILL
                                    AspectRatioMode.FILL -> AspectRatioMode.SIXTEEN_NINE
                                    AspectRatioMode.SIXTEEN_NINE -> AspectRatioMode.FIT
                                }
                            },
                            modifier = Modifier.testTag("player_aspect_ratio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Aspect ratio: ${aspectRatioMode.label}",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showSpeedDialog = true },
                            modifier = Modifier.testTag("player_speed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback speed",
                                tint = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(if (isFullscreen) 48.dp else 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val pos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(pos)
                                currentPositionMs = pos
                            },
                            modifier = Modifier.size(48.dp).testTag("player_skip_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Skip back 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    exoPlayer.play()
                                    isPlaying = true
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0x55000000), CircleShape)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val pos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs)
                                exoPlayer.seekTo(pos)
                                currentPositionMs = pos
                            },
                            modifier = Modifier.size(48.dp).testTag("player_skip_forward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Skip forward 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(
                                horizontal = if (isFullscreen) 20.dp else 12.dp,
                                vertical = if (isFullscreen) 12.dp else 4.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeElapsed = formatTime(currentPositionMs)
                            val totalTime = formatTime(durationMs)
                            Text(
                                text = "$timeElapsed / $totalTime",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )

                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.size(36.dp).testTag("player_fullscreen_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }

                        val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                        Slider(
                            value = progress,
                            onValueChange = { frac ->
                                val target = (frac * durationMs).toLong()
                                currentPositionMs = target
                                exoPlayer.seekTo(target)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = YouTubeRed,
                                activeTrackColor = YouTubeRed,
                                inactiveTrackColor = Color(0x66FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .testTag("player_seek_bar")
                        )
                    }
                }
            }
        }

        // Portrait mode Details, Description Card, Actions & Related Videos
        if (!isFullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("player_details_column"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 60.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Title
                        Text(
                            text = currentVideo.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Clickable Active Tag Chips directly beneath player title
                        if (currentVideo.tags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                currentVideo.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { onTagClick(tag) }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .testTag("player_tag_chip_$tag")
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Collapsible / Expandable YouTube-style Video Details & Description Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .testTag("video_description_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Summary row (Views • Date • Resolution)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentVideo.formattedViews,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                    Text(
                                        text = " • ${currentVideo.formattedDate}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (isDescriptionExpanded) "Show less" else "...more",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                    Icon(
                                        imageVector = if (isDescriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (isDescriptionExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Notes / Description content
                                    Text(
                                        text = if (currentVideo.description.isNotBlank()) currentVideo.description
                                        else "No description or notes added yet.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (currentVideo.description.isNotBlank()) MaterialTheme.colorScheme.onBackground
                                            else Color.Gray
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Technical Metadata Specs (Duration, Resolution, Size, Path)
                                    Text(
                                        text = "Technical Details:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Resolution: ${currentVideo.resolutionLabel}\n" +
                                                "• Duration: ${currentVideo.formattedDuration}\n" +
                                                "• Size: ${currentVideo.formattedSize}\n" +
                                                "• Path: ${currentVideo.path}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons inside description card
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showDescriptionDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.testTag("edit_notes_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Edit Notes", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                renameTargetVideo = currentVideo
                                                showRenameDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.testTag("rename_title_button_card")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Rename", fontSize = 12.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = { showTagDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.testTag("edit_tags_button_card")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Label,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Manage Tags", fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Channel info pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { showAssignChannelDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            ChannelAvatar(
                                name = currentVideo.effectiveChannelName,
                                iconUri = currentVideo.customChannelIconUri,
                                colorHex = currentVideo.customChannelColorHex,
                                size = 36.dp,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentVideo.effectiveChannelName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (currentVideo.customChannelName != null) "Custom Creator Channel" else "Local Folder Channel",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Switch",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = YouTubeRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons Bar (Like, Save, Tags, Notes, Share)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerActionButton(
                                icon = if (currentVideo.isFavorite) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                label = if (currentVideo.isFavorite) "Liked" else "Like",
                                tint = if (currentVideo.isFavorite) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                                onClick = { onToggleFavorite(currentVideo.id) },
                                testTag = "player_action_like"
                            )

                            PlayerActionButton(
                                icon = Icons.Default.Bookmark,
                                label = "Save",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { showPlaylistDialog = true },
                                testTag = "player_action_save"
                            )

                            PlayerActionButton(
                                icon = Icons.Default.Label,
                                label = "Tags",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { showTagDialog = true },
                                testTag = "player_action_tags"
                            )

                            PlayerActionButton(
                                icon = Icons.Default.Description,
                                label = "Notes",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { showDescriptionDialog = true },
                                testTag = "player_action_description"
                            )

                            PlayerActionButton(
                                icon = Icons.Default.Share,
                                label = "Share",
                                tint = MaterialTheme.colorScheme.onSurface,
                                onClick = { onShareVideo(currentVideo) },
                                testTag = "player_action_share"
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Up Next / Related Videos header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${relatedVideos.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Related Videos List
                items(relatedVideos, key = { it.id }) { vid ->
                    VideoFeedCard(
                        video = vid,
                        onClick = { onVideoSelected(vid) },
                        onMenuClick = { overflowVideoForMenu = vid }
                    )
                }
            }
        }
    }

    // Speed Selector Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = "Playback speed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackSpeed = speed
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                    color = if (playbackSpeed == speed) YouTubeRed else MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Interactive Tag Manager Dialog with 1-tap chip cloud
    if (showTagDialog) {
        TagManagerDialog(
            video = currentVideo,
            allPersistentTags = allPersistentTags,
            onDismiss = { showTagDialog = false },
            onAddTag = { tag -> onAddTag(currentVideo.id, tag) },
            onRemoveTag = { tag -> onRemoveTag(currentVideo.id, tag) },
            onToggleTag = { tag -> onToggleTag(currentVideo.id, tag) }
        )
    }

    // Edit Description / Notes Dialog
    if (showDescriptionDialog) {
        EditDescriptionDialog(
            video = currentVideo,
            onDismiss = { showDescriptionDialog = false },
            onSaveDescription = { notes -> onUpdateDescription(currentVideo.id, notes) }
        )
    }

    // Assign to Channel Dialog
    if (showAssignChannelDialog) {
        AssignChannelDialog(
            video = currentVideo,
            customChannels = customChannels,
            onDismiss = { showAssignChannelDialog = false },
            onAssign = { channelId -> onAssignChannel(currentVideo.id, channelId) }
        )
    }

    // Save to Playlist Dialog
    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            video = currentVideo,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onSelectPlaylist = { p -> onAddToPlaylist(p.id, currentVideo.id) },
            onCreatePlaylist = { name -> onCreatePlaylist(name) }
        )
    }

    // Overflow menu for related videos
    overflowVideoForMenu?.let { vid ->
        VideoMenuBottomSheet(
            video = vid,
            onDismiss = { overflowVideoForMenu = null },
            onAddToPlaylist = { showPlaylistDialog = true },
            onAssignTags = { showTagDialog = true },
            onRenameTitle = {
                renameTargetVideo = vid
                showRenameDialog = true
            },
            onEditDescription = { showDescriptionDialog = true },
            onAssignChannel = { showAssignChannelDialog = true },
            onToggleFavorite = { onToggleFavorite(vid.id) },
            onShare = { onShareVideo(vid) },
            onFileInfo = { showInfoDialog = true }
        )
    }

    if (showRenameDialog) {
        val target = renameTargetVideo ?: currentVideo
        RenameVideoDialog(
            video = target,
            onDismiss = {
                showRenameDialog = false
                renameTargetVideo = null
            },
            onRename = { newTitle ->
                onRenameTitle(target.id, newTitle)
            }
        )
    }

    if (showInfoDialog) {
        overflowVideoForMenu?.let { vid ->
            FileInfoDialog(
                video = vid,
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}

@Composable
private fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
