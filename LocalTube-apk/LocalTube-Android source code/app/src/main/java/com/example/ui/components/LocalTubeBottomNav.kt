package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.BottomNavTab

@Composable
fun LocalTubeBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets.navigationBars
    ) {
        // Tab 1: Home
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME,
            onClick = { onTabSelected(BottomNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        // Tab 2: Shorts
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.SHORTS,
            onClick = { onTabSelected(BottomNavTab.SHORTS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomNavTab.SHORTS) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                    contentDescription = "Shorts",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Shorts",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("nav_tab_shorts")
        )

        // Tab 3: Channels
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.CHANNELS,
            onClick = { onTabSelected(BottomNavTab.CHANNELS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomNavTab.CHANNELS) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                    contentDescription = "Channels",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Channels",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("nav_tab_channels")
        )

        // Tab 4: You
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.YOU,
            onClick = { onTabSelected(BottomNavTab.YOU) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomNavTab.YOU) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "You Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "You",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            ),
            modifier = Modifier.testTag("nav_tab_you")
        )
    }
}
