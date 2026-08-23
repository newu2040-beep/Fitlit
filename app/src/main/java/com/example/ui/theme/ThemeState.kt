package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class FitlitThemeMode(
    val id: String,
    val displayName: String,
    val description: String,
    val previewColor: Color,
    val previewBg: Color
) {
    SYSTEM("SYSTEM", "System Default", "Follows device appearance", LimePrimary, BackgroundLight),
    LIGHT("LIGHT", "Clean Light", "Bright lime & frosted glass", LimePrimaryDark, Color(0xFFF8FAFC)),
    DARK("DARK", "Midnight Dark", "Slate dark with neon lime", LimePrimary, Color(0xFF0F141A)),
    AMOLED_BLACK("AMOLED_BLACK", "Pure AMOLED Black", "100% OLED black battery saver", AmoledNeonLime, Color(0xFF000000)),
    CYBER_BLUE("CYBER_BLUE", "Cyber Electric Blue", "Futuristic neon blue & cyan", CyberPrimary, Color(0xFF070B14)),
    SUNSET_AMBER("SUNSET_AMBER", "Sunset Peach & Coral", "Warm embers and glowing peach", SunsetPrimary, Color(0xFF120D0A)),
    ROSE_GOLD("ROSE_GOLD", "Rose Gold & Sakura", "Luxury blush and metallic rose", RosePrimary, Color(0xFF140B10)),
    EMERALD("EMERALD", "Emerald Mint Forest", "Vibrant forest mint & jade", EmeraldPrimary, Color(0xFF04140E));

    companion object {
        fun fromId(id: String?): FitlitThemeMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
        }
    }
}
