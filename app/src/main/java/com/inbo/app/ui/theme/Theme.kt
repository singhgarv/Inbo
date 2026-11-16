package com.inbo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val InboLightColorScheme = lightColorScheme(
    primary = InboPurple,
    onPrimary = Color.White,
    primaryContainer = InboSoftPurple,
    onPrimaryContainer = InboPurple,
    secondary = InboPurple,
    onSecondary = Color.White,
    secondaryContainer = InboLightPurple,
    onSecondaryContainer = InboPurple,
    background = InboBackground,
    onBackground = InboInk,
    surface = InboCardWhite,
    onSurface = InboInk,
    surfaceVariant = InboBackground,
    onSurfaceVariant = InboMuted,
    outline = InboBorder,
    outlineVariant = InboDivider
)

// For dark theme fallback, keep the palette calm and high contrast
private val InboDarkColorScheme = darkColorScheme(
    primary = InboPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C2A4A),
    onPrimaryContainer = InboSoftPurple,
    secondary = InboPurple,
    onSecondary = Color.White,
    background = Color(0xFF14141E),
    onBackground = Color(0xFFF1F1F6),
    surface = Color(0xFF1E1E2D),
    onSurface = Color(0xFFF1F1F6),
    surfaceVariant = Color(0xFF252538),
    onSurfaceVariant = InboSubtle,
    outline = Color(0xFF32324A),
    outlineVariant = Color(0xFF2A2A3E)
)

@Composable
fun InboTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to preserve Inbo's signature soft purple & calm palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InboDarkColorScheme else InboLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
