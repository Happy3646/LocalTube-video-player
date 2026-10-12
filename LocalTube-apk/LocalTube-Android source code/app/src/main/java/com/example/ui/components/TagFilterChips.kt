package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagFilterChips(
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Default built-in dynamic filters
    val defaultTags = listOf("Shorts", "Camera", "Music", "Downloads", "Favorites")
    val mergedTags = (defaultTags + availableTags).distinctBy { it.lowercase() }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tag_filter_row"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            val isSelected = selectedTag == null
            FilterChipItem(
                text = "All",
                isSelected = isSelected,
                onClick = { onTagSelect(null) },
                modifier = Modifier.testTag("tag_chip_all")
            )
        }

        items(mergedTags) { tag ->
            val displayTag = if (tag.startsWith("#")) tag else "#$tag"
            val isSelected = selectedTag?.equals(tag, ignoreCase = true) == true ||
                    selectedTag?.equals(displayTag, ignoreCase = true) == true

            FilterChipItem(
                text = displayTag,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) {
                        onTagSelect(null)
                    } else {
                        onTagSelect(tag)
                    }
                },
                modifier = Modifier.testTag("tag_chip_${tag.lowercase()}")
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
        )
    }
}
