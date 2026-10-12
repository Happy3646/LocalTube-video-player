package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.abs

/**
 * Reusable channel avatar with custom photo support and graceful fallback
 * to a vibrant gradient badge with the channel's initial letter.
 * Eliminates broken or blank image placeholders.
 */
@Composable
fun ChannelAvatar(
    name: String,
    modifier: Modifier = Modifier,
    iconUri: String? = null,
    colorHex: String? = null,
    size: Dp = 36.dp,
    fontSize: TextUnit = 14.sp
) {
    var isImageError by remember(iconUri) { mutableStateOf(false) }

    val gradientColors = remember(name, colorHex) {
        if (!colorHex.isNullOrBlank()) {
            try {
                val parsed = Color(android.graphics.Color.parseColor(colorHex))
                listOf(parsed, parsed.copy(alpha = 0.7f))
            } catch (e: Exception) {
                fallbackColors(name)
            }
        } else {
            fallbackColors(name)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        if (!iconUri.isNullOrBlank() && !isImageError) {
            AsyncImage(
                model = iconUri,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                onError = { isImageError = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = if (name.isNotBlank()) name.trim().take(1).uppercase() else "C"
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

private fun fallbackColors(name: String): List<Color> {
    val hash = abs(name.hashCode())
    val palettes = listOf(
        listOf(Color(0xFFE91E63), Color(0xFFFF4081)), // Pink
        listOf(Color(0xFF9C27B0), Color(0xFFBA68C8)), // Purple
        listOf(Color(0xFF673AB7), Color(0xFF7E57C2)), // Deep Purple
        listOf(Color(0xFF2196F3), Color(0xFF42A5F5)), // Blue
        listOf(Color(0xFF009688), Color(0xFF26A69A)), // Teal
        listOf(Color(0xFF4CAF50), Color(0xFF66BB6A)), // Green
        listOf(Color(0xFFFF5722), Color(0xFFFF7043)), // Deep Orange
        listOf(Color(0xFF795548), Color(0xFF8D6E63)), // Brown
        listOf(Color(0xFF607D8B), Color(0xFF78909C)), // Blue Grey
        listOf(Color(0xFFE53935), Color(0xFFFF5252))  // YouTube Red
    )
    return palettes[hash % palettes.size]
}
