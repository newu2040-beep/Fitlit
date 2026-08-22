package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LimePrimaryDark,
    onPrimary = SurfaceLight,
    primaryContainer = LimeLight,
    onPrimaryContainer = TextOnLime,
    secondary = StepsGreen,
    onSecondary = SurfaceLight,
    secondaryContainer = StepsGreenLight,
    onSecondaryContainer = TextOnLime,
    tertiary = ProteinBlue,
    onTertiary = SurfaceLight,
    tertiaryContainer = ProteinBlueLight,
    onTertiaryContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = LimeContainer,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderLight,
    outlineVariant = GlassBorderHighlight
)

private val DarkColorScheme = darkColorScheme(
    primary = LimePrimary,
    onPrimary = TextOnLime,
    primaryContainer = Color(0xFF22360E),
    onPrimaryContainer = LimeLight,
    secondary = StepsGreen,
    onSecondary = SurfaceLight,
    secondaryContainer = Color(0xFF13361E),
    onSecondaryContainer = StepsGreenLight,
    tertiary = ProteinBlue,
    onTertiary = SurfaceLight,
    background = BackgroundDark,
    onBackground = SurfaceLight,
    surface = SurfaceDark,
    onSurface = SurfaceLight,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0x334B5563),
    outlineVariant = Color(0x66374151)
)

@Composable
fun FitlitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
