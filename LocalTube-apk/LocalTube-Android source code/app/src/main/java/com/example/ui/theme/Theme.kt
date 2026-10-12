package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String, val subtitle: String = "") {
    SYSTEM("System default", "Follows device dark/light setting"),
    DARK("Classic Dark", "YouTube AMOLED black aesthetic"),
    LIGHT("Pure Light", "Crisp high-contrast white layout"),
    MIDNIGHT_NAVY("Midnight Navy", "Deep OLED indigo & cosmic blue"),
    SUNSET_EMBER("Sunset Ember", "Warm obsidian charcoal & ember glow")
}

val LocalTubeDarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B0000),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFF1F1F1),
    onSecondary = Color(0xFF0F0F0F),
    secondaryContainer = YouTubeDarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFF1F1F1),
    tertiary = YouTubeVerifiedBlue,
    onTertiary = Color.White,
    background = YouTubeDarkBackground,
    onBackground = YouTubeDarkTextPrimary,
    surface = YouTubeDarkSurface,
    onSurface = YouTubeDarkTextPrimary,
    surfaceVariant = YouTubeDarkSurfaceVariant,
    onSurfaceVariant = YouTubeDarkTextSecondary,
    surfaceContainer = YouTubeDarkSurfaceCard,
    surfaceContainerHigh = Color(0xFF282828),
    surfaceContainerHighest = Color(0xFF333333),
    outline = YouTubeDarkOutline,
    outlineVariant = Color(0xFF222222)
)

val LocalTubeLightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF0F0F0F),
    onSecondary = Color.White,
    secondaryContainer = YouTubeLightSurfaceVariant,
    onSecondaryContainer = Color(0xFF0F0F0F),
    tertiary = YouTubeVerifiedBlueLight,
    onTertiary = Color.White,
    background = YouTubeLightBackground,
    onBackground = YouTubeLightTextPrimary,
    surface = YouTubeLightSurface,
    onSurface = YouTubeLightTextPrimary,
    surfaceVariant = YouTubeLightSurfaceVariant,
    onSurfaceVariant = YouTubeLightTextSecondary,
    surfaceContainer = YouTubeLightSurfaceCard,
    surfaceContainerHigh = Color(0xFFEBEBEB),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    outline = YouTubeLightOutline,
    outlineVariant = Color(0xFFE5E5E5)
)

val LocalTubeMidnightNavyColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = MidnightNavyTextPrimary,
    onSecondary = MidnightNavyBackground,
    secondaryContainer = MidnightNavySurfaceVariant,
    onSecondaryContainer = MidnightNavyTextPrimary,
    tertiary = MidnightNavyAccent,
    onTertiary = Color(0xFF090E1A),
    background = MidnightNavyBackground,
    onBackground = MidnightNavyTextPrimary,
    surface = MidnightNavySurface,
    onSurface = MidnightNavyTextPrimary,
    surfaceVariant = MidnightNavySurfaceVariant,
    onSurfaceVariant = MidnightNavyTextSecondary,
    surfaceContainer = MidnightNavySurfaceCard,
    surfaceContainerHigh = Color(0xFF1A2744),
    surfaceContainerHighest = Color(0xFF24365D),
    outline = MidnightNavyOutline,
    outlineVariant = Color(0xFF1C2D4F)
)

val LocalTubeSunsetEmberColorScheme = darkColorScheme(
    primary = SunsetEmberAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B151E),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = SunsetEmberTextPrimary,
    onSecondary = SunsetEmberBackground,
    secondaryContainer = SunsetEmberSurfaceVariant,
    onSecondaryContainer = SunsetEmberTextPrimary,
    tertiary = Color(0xFFFFAB91),
    onTertiary = Color(0xFF1F0B10),
    background = SunsetEmberBackground,
    onBackground = SunsetEmberTextPrimary,
    surface = SunsetEmberSurface,
    onSurface = SunsetEmberTextPrimary,
    surfaceVariant = SunsetEmberSurfaceVariant,
    onSurfaceVariant = SunsetEmberTextSecondary,
    surfaceContainer = SunsetEmberSurfaceCard,
    surfaceContainerHigh = Color(0xFF2D1D23),
    surfaceContainerHighest = Color(0xFF3E2831),
    outline = SunsetEmberOutline,
    outlineVariant = Color(0xFF352028)
)

@Composable
fun LocalTubeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (isSystemDark) LocalTubeDarkColorScheme else LocalTubeLightColorScheme
        AppThemeMode.DARK -> LocalTubeDarkColorScheme
        AppThemeMode.LIGHT -> LocalTubeLightColorScheme
        AppThemeMode.MIDNIGHT_NAVY -> LocalTubeMidnightNavyColorScheme
        AppThemeMode.SUNSET_EMBER -> LocalTubeSunsetEmberColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
