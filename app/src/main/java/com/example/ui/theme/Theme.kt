package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledNeonLime,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF142405),
    onPrimaryContainer = AmoledNeonLime,
    secondary = Color(0xFF00FF88),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF062B17),
    onSecondaryContainer = Color(0xFF80FFC4),
    tertiary = Color(0xFF00E5FF),
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = Color(0xFFF0F0F0),
    surface = AmoledSurface,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA0A0A0),
    outline = AmoledBorder,
    outlineVariant = Color(0xFF333333)
)

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color(0xFF031024),
    primaryContainer = Color(0xFF0C274C),
    onPrimaryContainer = CyberPrimary,
    secondary = CyberAccent,
    onSecondary = Color(0xFF031024),
    secondaryContainer = Color(0xFF0E3860),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color.Black,
    background = CyberBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = CyberSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = CyberBorder,
    outlineVariant = Color(0x6600D2FF)
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF431B0B),
    onPrimaryContainer = Color(0xFFFFD8CC),
    secondary = SunsetAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452B05),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFFFB923C),
    onTertiary = Color.White,
    background = SunsetBackground,
    onBackground = Color(0xFFFFF7ED),
    surface = SunsetSurface,
    onSurface = Color(0xFFFFFAF0),
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = Color(0xFFD4A394),
    outline = SunsetBorder,
    outlineVariant = Color(0x66FF6B2B)
)

private val RoseColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1024),
    onPrimaryContainer = Color(0xFFFFD2DE),
    secondary = RoseAccent,
    onSecondary = Color(0xFF4C0519),
    secondaryContainer = Color(0xFF3D1220),
    onSecondaryContainer = Color(0xFFFECDD3),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color.White,
    background = RoseBackground,
    onBackground = Color(0xFFFFF1F2),
    surface = RoseSurface,
    onSurface = Color(0xFFFFF5F7),
    surfaceVariant = RoseSurfaceVariant,
    onSurfaceVariant = Color(0xFFD8A4B6),
    outline = RoseBorder,
    outlineVariant = Color(0x66FB7185)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF063A26),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = EmeraldAccent,
    onSecondary = Color(0xFF022C1B),
    secondaryContainer = Color(0xFF09422C),
    onSecondaryContainer = Color(0xFF6EE7B7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color.Black,
    background = EmeraldBackground,
    onBackground = Color(0xFFECFDF5),
    surface = EmeraldSurface,
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = Color(0xFF86EFAC),
    outline = EmeraldBorder,
    outlineVariant = Color(0x6610B981)
)

@Composable
fun FitlitTheme(
    themeMode: FitlitThemeMode = FitlitThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme: ColorScheme = when (themeMode) {
        FitlitThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        FitlitThemeMode.LIGHT -> LightColorScheme
        FitlitThemeMode.DARK -> DarkColorScheme
        FitlitThemeMode.AMOLED_BLACK -> AmoledColorScheme
        FitlitThemeMode.CYBER_BLUE -> CyberColorScheme
        FitlitThemeMode.SUNSET_AMBER -> SunsetColorScheme
        FitlitThemeMode.ROSE_GOLD -> RoseColorScheme
        FitlitThemeMode.EMERALD -> EmeraldColorScheme
    }

    val isDarkAppearance = when (themeMode) {
        FitlitThemeMode.SYSTEM -> isSystemDark
        FitlitThemeMode.LIGHT -> false
        else -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkAppearance
            insetsController.isAppearanceLightNavigationBars = !isDarkAppearance
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
