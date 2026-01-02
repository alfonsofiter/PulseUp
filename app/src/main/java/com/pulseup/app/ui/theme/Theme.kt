package com.pulseup.app.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleLight,
    onPrimaryContainer = Color.White,
    secondary = InfoBlue,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = InfoBlue,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF252A41),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF303651)
)

@Composable
fun PulseUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}