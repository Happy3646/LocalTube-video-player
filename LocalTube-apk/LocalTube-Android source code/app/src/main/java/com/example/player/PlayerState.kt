package com.example.player

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill / Crop"),
    SIXTEEN_NINE("16:9")
}

data class PlayerPlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isEnded: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isFullscreen: Boolean = false,
    val areControlsVisible: Boolean = true
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
