package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

data class GuideChapter(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val guideChapters = listOf(
    GuideChapter(0, "Overview & Feed", "Home Feed, Recommendations & Search", Icons.Default.Home),
    GuideChapter(1, "Player Gestures", "YouTube Gestures, 2x Speed & Controls", Icons.Default.PlayArrow),
    GuideChapter(2, "Shorts Reel", "Vertical Shorts, Likes & Auto-detection", Icons.Default.Bolt),
    GuideChapter(3, "Creator Channels", "Folder Channels, Subscriptions & Custom", Icons.Default.Subscriptions),
    GuideChapter(4, "Library & Playlists", "Liked Videos, Watch History & Tags", Icons.Default.History),
    GuideChapter(5, "Button Dictionary", "Complete Visual Cheat Sheet of All Buttons", Icons.Default.HelpOutline)
)

data class ButtonDictionaryItem(
    val icon: ImageVector,
    val name: String,
    val location: String,
    val description: String,
    val category: String
)

val buttonDictionaryList = listOf(
    // Player Controls
    ButtonDictionaryItem(
        icon = Icons.Filled.ThumbUp,
        name = "Like / Liked",
        location = "Video Player, Shorts & 3-Dot Menu",
        description = "Likes the video and automatically saves it to your special 'Liked videos' playlist in the Library tab. Tapping again unlikes and removes it.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.FastForward,
        name = "Double-Tap Fast Forward (+10s)",
        location = "Right 45% of Video Player",
        description = "Double tap the right side of the screen during playback to instantly jump ahead 10 seconds with ripple animations.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.FastRewind,
        name = "Double-Tap Rewind (-10s)",
        location = "Left 45% of Video Player",
        description = "Double tap the left side of the screen during playback to jump backwards 10 seconds.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Speed,
        name = "Hold for 2x Speed / Speed Selector",
        location = "Player Screen (Hold Gesture or Speed Icon)",
        description = "Press and hold anywhere on the video to temporarily boost playback speed to 2x. Or tap the speed icon to choose 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, or 2.0x.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Fullscreen,
        name = "Swipe Up Fullscreen / Landscape",
        location = "Video Player Screen",
        description = "Swipe up on the player or tap the expand icon to lock into landscape fullscreen with cutout-safe padding. Swipe down to exit back to portrait.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.ZoomIn,
        name = "Pinch to Zoom (Fill vs Fit)",
        location = "Video Player Screen",
        description = "Pinch with two fingers outward to stretch/zoom video into full 16:9 screen fill, or pinch inward to fit original aspect ratio.",
        category = "Player & Feedback"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Bookmark,
        name = "Save to Playlist",
        location = "Player Screen & 3-Dot Menu",
        description = "Opens playlist selector to save video to 'Watch Later', 'Favorites', or custom playlists created in your Library.",
        category = "Organization & Library"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Label,
        name = "Tag Manager (#hashtags)",
        location = "Player Screen, Feed Menu & Library",
        description = "Assign custom searchable hashtags (e.g. #music, #tutorial, #camera) to group and instantly filter videos from the Home feed chip bar.",
        category = "Organization & Library"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Description,
        name = "Video Notes & Description",
        location = "Player Screen & 3-Dot Menu",
        description = "Attach personal text notes, timestamps, or descriptions to any video without editing physical storage files.",
        category = "Organization & Library"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Edit,
        name = "Rename Title (In-App)",
        location = "3-Dot Overflow Menu & Description Card",
        description = "Customize the display title of any video in the UI while keeping the original file untouched on device storage.",
        category = "Organization & Library"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Shuffle,
        name = "Shuffle Feed / Playlist",
        location = "Home Top Bar & Playlist Detail",
        description = "Re-randomizes video order for fresh discovery. In Home feed, shows a Snackbar with a 'Reset Order' button to return to recommendation sort.",
        category = "Navigation & Discovery"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Search,
        name = "Instant Search",
        location = "Home Top Bar",
        description = "Search across all video titles, tags, folder paths, and creator channel names simultaneously in real-time.",
        category = "Navigation & Discovery"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Refresh,
        name = "Scan Storage",
        location = "Home Top Bar & Library Tab",
        description = "Forces a fresh scan of MediaStore and custom folders to discover newly downloaded, recorded, or transferred videos.",
        category = "Navigation & Discovery"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Subscriptions,
        name = "Subscribe / Creator Channels",
        location = "Channels Tab & Channel Profiles",
        description = "Subscribes to a folder channel or custom channel, pinning its avatar to the top carousel and boosting its videos on your Home feed.",
        category = "Channels & Folders"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Repeat,
        name = "Shorts Loop Toggle",
        location = "Shorts Reel Action Rail",
        description = "Toggles infinite loop playback on and off for the active Short.",
        category = "Shorts Reel"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.VolumeOff,
        name = "Mute / Unmute Audio",
        location = "Shorts Reel Action Rail",
        description = "Instantly silences or restores audio volume in the Shorts reel player.",
        category = "Shorts Reel"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Share,
        name = "Share Video File",
        location = "Player, Shorts & 3-Dot Menu",
        description = "Opens the native Android system share sheet to send the video file directly to messaging apps, email, or nearby share.",
        category = "System & Settings"
    ),
    ButtonDictionaryItem(
        icon = Icons.Default.Palette,
        name = "Appearance / Theme Switcher",
        location = "Library ('You') Tab",
        description = "Switch between 5 themes: System Default, Classic Dark, Pure Light, Midnight Navy, and Sunset Ember.",
        category = "System & Settings"
    )
)

@Composable
fun GuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val pagerState = rememberPagerState(pageCount = { guideChapters.size })
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("guide_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("guide_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = YouTubeRed,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LocalTube Handbook",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Swipe left / right to turn pages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick swipe indicator badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe Pages",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Reading Progress Indicator
        val progress = (pagerState.currentPage + 1) / guideChapters.size.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = YouTubeRed,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Chapter Tab Row
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = YouTubeRed
                )
            }
        ) {
            guideChapters.forEachIndexed { index, chapter ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = chapter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (pagerState.currentPage == index) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ch. ${index + 1}: ${chapter.title}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.testTag("guide_tab_$index")
                )
            }
        }

        // Swipeable Horizontal Pager for Book Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("guide_horizontal_pager")
        ) { page ->
            when (page) {
                0 -> ChapterOneOverviewScreen()
                1 -> ChapterTwoGesturesScreen()
                2 -> ChapterThreeShortsScreen()
                3 -> ChapterFourChannelsScreen()
                4 -> ChapterFiveLibraryScreen()
                5 -> ChapterSixDictionaryScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )
            }
        }

        // Bottom Page Flipping Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("guide_prev_chapter")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous page",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                // Page number & dots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    guideChapters.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (pagerState.currentPage == i) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == i) YouTubeRed else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Page ${pagerState.currentPage + 1} of ${guideChapters.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < guideChapters.size - 1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("guide_next_chapter")
                ) {
                    Text(if (pagerState.currentPage < guideChapters.size - 1) "Next" else "Done")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (pagerState.currentPage < guideChapters.size - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.CheckCircle,
                        contentDescription = "Next page",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 1: Overview & Home Feed
// -------------------------------------------------------------
@Composable
private fun ChapterOneOverviewScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Book-styled Vector Card Header
            BookCoverHeaderCard(
                title = "Chapter 1: The YouTube-Style Local Experience",
                subtitle = "LocalTube turns your device's video storage into a private streaming platform without ads, tracking, or cloud dependencies.",
                chapterNumber = 1,
                tagline = "HOME FEED & RECOMMENDATIONS"
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Home,
                title = "Single-Column 16:9 Vertical Feed",
                description = "Each video in your Home feed renders with an edge-to-edge 16:9 preview thumbnail, precise duration badge, creator folder avatar, view count, and formatted upload date."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = "Smart Recommendation Engine",
                description = "Your feed is dynamically ranked! Videos from channels you subscribe to receive a priority boost, followed by hashtag affinity scoring (tags you watch most) and balanced with unwatched recency."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Shuffle,
                title = "1-Tap Feed Shuffle & Reset",
                description = "Tapping 'Shuffle' in the top bar instantly randomizes your entire feed for discovery. A snackbar appears with a 'Reset Order' action allowing you to restore your personalized ranking at any time."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Label,
                title = "Dynamic Hashtag Filter Chips",
                description = "Located right below the top header, the horizontal chip bar lets you filter the whole feed by tapping built-in categories (#music, #camera, #shorts) or your custom assigned tags."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Search,
                title = "Real-Time Instant Search",
                description = "Type in the top search bar to filter simultaneously across video titles, file folder paths, assigned hashtags, and custom creator channel names."
            )
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 2: Master Player & YouTube Touch Gestures
// -------------------------------------------------------------
@Composable
private fun ChapterTwoGesturesScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BookCoverHeaderCard(
                title = "Chapter 2: Master Player & YouTube Gestures",
                subtitle = "ExoPlayer (Media3) with high-precision touch gestures, speed boosting, and immersive landscape fullscreen.",
                chapterNumber = 2,
                tagline = "TOUCH GESTURES & CONTROLS"
            )
        }

        item {
            // Pure Compose Visual Gesture Map Diagram Card
            GestureVisualMapCard()
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.FastForward,
                title = "Double-Tap to Seek (±10 Seconds)",
                description = "Double-tap the right 45% of the video player to fast-forward 10 seconds. Double-tap the left 45% to rewind 10 seconds. Includes animated ripple pill indicators."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Speed,
                title = "Press & Hold for 2x Playback Speed",
                description = "Touch and hold your finger anywhere on the active video to temporarily accelerate playback to 2x. Release your finger to seamlessly return to normal speed."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Fullscreen,
                title = "Swipe Up for Landscape Fullscreen",
                description = "Swipe upwards over the player to instantly enter true fullscreen landscape mode. Swipe down to return to portrait view. Landscape controls automatically pad away from camera cutouts and notch insets."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.ZoomIn,
                title = "Pinch to Zoom (Fit vs Fill)",
                description = "Pinch outward with two fingers to zoom video into full 16:9 crop mode (eliminating black sidebars). Pinch inward to return to original aspect ratio."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Filled.ThumbUp,
                title = "Like Button & Liked Videos Sync",
                description = "Tapping the Thumb Up button in the player marks the video as Liked and automatically inserts it into your 'Liked videos' playlist in the Library."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Description,
                title = "Expandable Description & Real File Info",
                description = "Tap the description card under the player to reveal real metadata: exact file path, resolution (e.g. 1080p FHD, 4K UHD), file size, total views, and editable notes."
            )
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 3: YouTube Shorts Experience
// -------------------------------------------------------------
@Composable
private fun ChapterThreeShortsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BookCoverHeaderCard(
                title = "Chapter 3: YouTube Shorts Experience",
                subtitle = "Vertical, auto-looping reels dedicated to short-form and portrait video content.",
                chapterNumber = 3,
                tagline = "VERTICAL SHORTS REEL"
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Bolt,
                title = "Automatic Shorts Detection",
                description = "LocalTube automatically separates videos into the Shorts tab if they are formatted vertically (portrait aspect ratio) or are shorter than 65 seconds."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.PlayArrow,
                title = "Vertical Pager & Tap to Pause",
                description = "Swipe up or down to seamlessly flick between short videos. Tap anywhere in the center of the reel to pause or resume playback with a central icon pop."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Filled.ThumbUp,
                title = "Shorts Like Button & Likes Counter",
                description = "Tap the Thumb Up button on the right-hand action column to like the Short. Like counts dynamically update and the Short is saved directly to your 'Liked videos' playlist."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Repeat,
                title = "Loop Toggle & Audio Mute",
                description = "Control looping behavior with the 'Loop On/Off' button, and quickly mute or unmute audio with the volume toggle."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Info,
                title = "Shorts Details Drawer",
                description = "Tap the info icon on any Short to open the details sheet showing resolution, file size, storage directory, and tags."
            )
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 4: Folders as Creator Channels
// -------------------------------------------------------------
@Composable
private fun ChapterFourChannelsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BookCoverHeaderCard(
                title = "Chapter 4: Folders as Creator Channels",
                subtitle = "How LocalTube organizes device folders into subscribed YouTube creator profiles.",
                chapterNumber = 4,
                tagline = "CHANNELS & SUBSCRIPTIONS"
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Folder,
                title = "Folders as Channels",
                description = "Every storage directory containing videos (e.g., /Camera, /Downloads, /Movies, /WhatsApp) is automatically represented as a distinct Creator Channel with its own colored avatar."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Subscriptions,
                title = "Subscribing to Channels",
                description = "Tap 'Subscribe' on any channel to pin its avatar to the top Subscriptions carousel in the Channels tab. Subscribed channels receive boosted placement in your Home feed."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Palette,
                title = "Channel Profiles & Gradients",
                description = "Tapping any channel opens its dedicated profile page featuring a banner header, total subscriber count, aggregate watch time, and separate tabs for full Videos and Shorts."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.FolderSpecial,
                title = "Create Custom Creator Channels",
                description = "Tap 'New Channel' in the Channels tab to create virtual creator identities with custom hex colors and icons, and assign videos to them regardless of their physical folder location."
            )
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 5: Library, Playlists & Liked Videos
// -------------------------------------------------------------
@Composable
private fun ChapterFiveLibraryScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BookCoverHeaderCard(
                title = "Chapter 5: Library, Playlists & Settings",
                subtitle = "Manage history, organized playlists, custom tags, storage folders, and 5 distinct themes.",
                chapterNumber = 5,
                tagline = "LIBRARY, PLAYLISTS & THEMES"
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Filled.ThumbUp,
                title = "Special 'Liked videos' Playlist",
                description = "Prominently featured as the first playlist in your Library. All videos and shorts you like automatically gather here. Supports 'Play All' and 'Shuffle' playback."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.History,
                title = "Horizontal Watch History & Resume Bars",
                description = "Scroll through your recently watched videos with red resume progress indicators showing exactly where you left off. Tap 'Clear' to reset history at any time."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.PlaylistAdd,
                title = "Custom Playlists & Management",
                description = "Create unlimited custom playlists (e.g. 'Workouts', 'Tutorials', 'Favorites'). Re-order, add, or remove videos via the 3-dot overflow menu."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Label,
                title = "Batch Tagging & Channel Assignment",
                description = "The Library provides quick 'All Untagged' and 'Unassigned' triage queues with batch selection to tag dozens of videos at once."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Folder,
                title = "Storage Folders Manager",
                description = "Include or exclude specific folders to keep personal or unwanted videos out of your feed, or link external SD Card directories via Storage Access Framework."
            )
        }

        item {
            GuideFeatureCard(
                icon = Icons.Default.Palette,
                title = "5 Theme Color Palettes",
                description = "Choose between System Default, Classic Dark (AMOLED), Pure Light, Midnight Navy (OLED Indigo), and Sunset Ember (Obsidian Charcoal & Warm Red) in the Library Appearance settings."
            )
        }
    }
}

// -------------------------------------------------------------
// CHAPTER 6: Button-by-Button Visual Dictionary (Cheat Sheet)
// -------------------------------------------------------------
@Composable
private fun ChapterSixDictionaryScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val categories = listOf("All", "Player & Feedback", "Organization & Library", "Navigation & Discovery", "Channels & Folders", "Shorts Reel", "System & Settings")

    val filteredItems = remember(searchQuery, selectedCategory) {
        buttonDictionaryList.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BookCoverHeaderCard(
                title = "Chapter 6: Visual Button Dictionary",
                subtitle = "Complete interactive cheat sheet explaining every icon, button, and touch action in LocalTube.",
                chapterNumber = 6,
                tagline = "ICON CHEAT SHEET"
            )
        }

        item {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search button or gesture...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YouTubeRed,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictionary_search_input")
            )
        }

        // Category Filter Chips
        item {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    val idx = categories.indexOf(selectedCategory).coerceAtLeast(0)
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[idx]),
                        color = YouTubeRed
                    )
                }
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelect(category) },
                        text = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedCategory == category) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No buttons match \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredItems, key = { it.name }) { item ->
                DictionaryRowCard(item = item)
            }
        }
    }
}

// -------------------------------------------------------------
// REUSABLE CODE-DRAWN BOOK & GESTURE COMPONENTS
// -------------------------------------------------------------

@Composable
private fun BookCoverHeaderCard(
    title: String,
    subtitle: String,
    chapterNumber: Int,
    tagline: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            YouTubeRed.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(YouTubeRed)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CHAPTER $chapterNumber",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GestureVisualMapCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Player Screen Touch Zones",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Mock Video Canvas Diagram
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F0F))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left 45% (Rewind)
                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Double-Tap\n-10s Rewind",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Center 10% (Play / Pause & Hold 2x)
                    Box(
                        modifier = Modifier
                            .weight(0.2f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(YouTubeRed.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hold 2x",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = YouTubeRed
                            )
                        }
                    }

                    // Right 45% (Fast Forward)
                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Double-Tap\n+10s Forward",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "↑ Swipe Up: Landscape Fullscreen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "↔ Pinch: Zoom to Fill 16:9",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GuideFeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DictionaryRowCard(item: ButtonDictionaryItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = YouTubeRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
