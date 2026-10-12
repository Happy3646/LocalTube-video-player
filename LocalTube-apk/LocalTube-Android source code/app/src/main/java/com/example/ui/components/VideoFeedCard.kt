package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.theme.YouTubeDurationBadge
import com.example.ui.theme.YouTubeRed
import kotlin.math.abs

@Composable
fun VideoFeedCard(
    video: LocalVideo,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    onChannelClick: ((String, Long?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Build Coil image request configured for video frame decoding
    val imageRequest = remember(video.uri) {
        ImageRequest.Builder(context)
            .data(video.uri)
            .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
            .videoFrameMillis(1000)
            .crossfade(true)
            .build()
    }

    // Avatar color derived consistently from folder name
    val avatarBgColor = remember(video.channelName) {
        val hash = abs(video.channelName.hashCode())
        val colors = listOf(
            Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
            Color(0xFF3F51B5), Color(0xFF009688), Color(0xFF4CAF50),
            Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B)
        )
        colors[hash % colors.size]
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(bottom = 16.dp)
            .testTag("video_feed_card_${video.id}")
    ) {
        // 1. Full-width 16:9 Thumbnail Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x66000000)),
                            startY = 100f
                        )
                    )
            )

            // Duration Pill in bottom-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(YouTubeDurationBadge, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Watch progress bar if partially watched
            if (video.lastWatchedPositionMs > 0 && video.durationMs > 0) {
                val progress = (video.lastWatchedPositionMs.toFloat() / video.durationMs.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = YouTubeRed,
                    trackColor = Color(0x55FFFFFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Video Info Row (Avatar + Title/Subline + 3-dot Menu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar Circle
            Box(
                modifier = Modifier
                    .then(
                        if (onChannelClick != null) {
                            Modifier.clickable { onChannelClick(video.effectiveChannelName, video.customChannelId) }
                        } else Modifier
                    )
            ) {
                ChannelAvatar(
                    name = video.effectiveChannelName,
                    iconUri = video.customChannelIconUri,
                    colorHex = video.customChannelColorHex,
                    size = 36.dp,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Two-line Title & Metadata Subline
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = video.metadataSubline,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.then(
                        if (onChannelClick != null) {
                            Modifier.clickable { onChannelClick(video.effectiveChannelName, video.customChannelId) }
                        } else Modifier
                    )
                )

                // Optional Hashtags list
                if (video.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = video.tags.joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3-dot Overflow Menu Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("video_menu_button_${video.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options for ${video.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
