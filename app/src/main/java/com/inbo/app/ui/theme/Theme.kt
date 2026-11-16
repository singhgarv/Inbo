package com.inbo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Custom Inbo design colors extending beyond Material3 defaults,
 * allowing full visual parity and contrast in both Light and Dark modes.
 */
data class InboExtendedColors(
    val background: Color,
    val cardSurface: Color,
    val cardSurfaceSubtle: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val accentPurple: Color,
    val softPurple: Color,
    val lightPurple: Color,
    val cardBorder: Color,
    val divider: Color,
    val successText: Color,
    val successContainer: Color,
    val isDark: Boolean
)

val LocalInboColors = staticCompositionLocalOf {
    InboLightExtendedColors
}

val InboLightExtendedColors = InboExtendedColors(
    background = InboBackground,
    cardSurface = InboCardWhite,
    cardSurfaceSubtle = Color(0xFFFAFAFD),
    textPrimary = InboInk,
    textMuted = InboMuted,
    textSubtle = InboSubtle,
    accentPurple = InboPurple,
    softPurple = InboSoftPurple,
    lightPurple = InboLightPurple,
    cardBorder = InboBorder,
    divider = InboDivider,
    successText = InboSuccess,
    successContainer = InboSuccessSoft,
    isDark = false
)

val InboDarkExtendedColors = InboExtendedColors(
    background = InboDarkBackground,
    cardSurface = InboDarkSurface,
    cardSurfaceSubtle = InboDarkSurfaceVariant,
    textPrimary = InboDarkTextPrimary,
    textMuted = InboDarkTextMuted,
    textSubtle = InboDarkTextSubtle,
    accentPurple = InboDarkPurple,
    softPurple = InboDarkSoftPurple,
    lightPurple = InboDarkLightPurple,
    cardBorder = InboDarkBorder,
    divider = InboDarkDivider,
    successText = InboDarkSuccess,
    successContainer = InboDarkSuccessSoft,
    isDark = true
)

// Material 3 Light Scheme
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

// Material 3 Dark Scheme
// Deep charcoal background, slightly lighter charcoal cards, near-white primary text,
// muted grey secondary text, refined purple accent.
private val InboDarkColorScheme = darkColorScheme(
    primary = InboDarkPurple,
    onPrimary = Color.White,
    primaryContainer = InboDarkSoftPurple,
    onPrimaryContainer = InboDarkTextPrimary,
    secondary = InboDarkPurple,
    onSecondary = Color.White,
    secondaryContainer = InboDarkLightPurple,
    onSecondaryContainer = InboDarkPurple,
    background = InboDarkBackground,
    onBackground = InboDarkTextPrimary,
    surface = InboDarkSurface,
    onSurface = InboDarkTextPrimary,
    surfaceVariant = InboDarkSurfaceVariant,
    onSurfaceVariant = InboDarkTextMuted,
    outline = InboDarkBorder,
    outlineVariant = InboDarkDivider
)

object InboTheme {
    val colors: InboExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalInboColors.current
}

@Composable
fun InboTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InboDarkColorScheme else InboLightColorScheme
    val extendedColors = if (darkTheme) InboDarkExtendedColors else InboLightExtendedColors

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

    CompositionLocalProvider(LocalInboColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
