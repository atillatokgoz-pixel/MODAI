package com.example.naifdeneme.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.naifdeneme.PreferencesManager
import kotlinx.coroutines.flow.first

/**
 * MODAI Theme System
 * Light ve Dark mode + Dil desteği
 */

// ============================================
// APP SETTINGS HOLDER
// ============================================
data class AppSettings(
    val isDarkMode: Boolean = false,
    val language: String = "tr",
    val dynamicColor: Boolean = true
)

val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

// ============================================
// LIGHT COLOR SCHEME
// ============================================
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = PrimarySky,
    onPrimary = TextPrimaryLight,
    primaryContainer = AccentSky,
    onPrimaryContainer = TextPrimaryLight,

    // Secondary colors (Mint accent)
    secondary = AccentMint,
    onSecondary = TextPrimaryLight,
    secondaryContainer = AccentMint,
    onSecondaryContainer = TextPrimaryLight,

    // Tertiary colors (Blush accent)
    tertiary = AccentBlush,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = AccentBlush,
    onTertiaryContainer = TextPrimaryLight,

    // Background
    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    // Surface
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SubtleLight,
    onSurfaceVariant = TextSecondaryLight,

    // Border
    outline = BorderLight,
    outlineVariant = SubtleLight,

    // Error
    error = ErrorRed,
    onError = SurfaceLight,
    errorContainer = ErrorRed,
    onErrorContainer = TextPrimaryLight
)

// ============================================
// DARK COLOR SCHEME
// ============================================
private val DarkColorScheme = darkColorScheme(
    // Primary colors (Neon cyan)
    primary = PrimaryCyan,
    onPrimary = BackgroundDark,
    primaryContainer = NeonAqua,
    onPrimaryContainer = TextPrimaryDark,

    // Secondary colors (Lime)
    secondary = NeonLime,
    onSecondary = BackgroundDark,
    secondaryContainer = NeonLime,
    onSecondaryContainer = TextPrimaryDark,

    // Tertiary colors (Magenta)
    tertiary = NeonMagenta,
    onTertiary = BackgroundDark,
    tertiaryContainer = NeonMagenta,
    onTertiaryContainer = TextPrimaryDark,

    // Background
    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    // Surface
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SubtleDark,
    onSurfaceVariant = TextSecondaryDark,

    // Border
    outline = BorderDark,
    outlineVariant = SubtleDark,

    // Error
    error = ErrorRed,
    onError = BackgroundDark,
    errorContainer = ErrorRed,
    onErrorContainer = TextPrimaryDark
)

// ============================================
// MAIN THEME COMPOSABLE
// ============================================
@Composable
fun ModaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }

    // App settings state
    var appSettings by remember { mutableStateOf(AppSettings()) }

    // Load settings from DataStore
    LaunchedEffect(Unit) {
        val darkMode = preferencesManager.isDarkMode.first()
        val language = preferencesManager.language.first()
        val dynamicColorSetting = preferencesManager.dynamicColor.first()

        appSettings = AppSettings(
            isDarkMode = darkMode,
            language = language,
            dynamicColor = dynamicColorSetting
        )
    }

    // Determine actual theme to use
    val useDarkTheme = appSettings.isDarkMode
    val useDynamicColor = dynamicColor && appSettings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamicColor && useDarkTheme -> {
            dynamicDarkColorScheme(context)
        }
        useDynamicColor && !useDarkTheme -> {
            dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalAppSettings provides appSettings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ModaiTypography,
            content = content
        )
    }
}

// ============================================
// TEMA EXTENSION FUNCTIONS (Kolay erişim için)
// ============================================

/**
 * Theme'den renkler için extension properties
 * Kullanım: MaterialTheme.colorScheme.textSecondary
 */

// Text colors
val androidx.compose.material3.ColorScheme.textSecondary: Color
    @Composable get() = if (LocalAppSettings.current.isDarkMode) TextSecondaryDark else TextSecondaryLight

val androidx.compose.material3.ColorScheme.textTertiary: Color
    @Composable get() = if (LocalAppSettings.current.isDarkMode) TextTertiaryDark else TextTertiaryLight

// Background colors
val androidx.compose.material3.ColorScheme.subtle: Color
    @Composable get() = if (LocalAppSettings.current.isDarkMode) SubtleDark else SubtleLight

val androidx.compose.material3.ColorScheme.progressBarBg: Color
    @Composable get() = if (LocalAppSettings.current.isDarkMode) ProgressBarBgDark else ProgressBarBgLight

val androidx.compose.material3.ColorScheme.progressBarFill: Color
    get() = ProgressBarFill

// Accent colors (Light mode)
val androidx.compose.material3.ColorScheme.accentMint: Color
    get() = AccentMint

val androidx.compose.material3.ColorScheme.accentSky: Color
    get() = AccentSky

val androidx.compose.material3.ColorScheme.accentBlush: Color
    get() = AccentBlush

val androidx.compose.material3.ColorScheme.accentSand: Color
    get() = AccentSand

// Neon colors (Dark mode)
val androidx.compose.material3.ColorScheme.neonAqua: Color
    get() = NeonAqua

val androidx.compose.material3.ColorScheme.neonMagenta: Color
    get() = NeonMagenta

val androidx.compose.material3.ColorScheme.neonLime: Color
    get() = NeonLime

val androidx.compose.material3.ColorScheme.neonPurple: Color
    get() = NeonPurple

// ============================================
// PUBLIC COLOR PROPERTIES (Cross-module erişim için)
// ============================================

/**
 * Cross-module erişim için public color properties
 * Kullanım: import com.example.modai.ui.theme.neonAqua
 */

// Accent colors (Light mode) - PUBLIC
val accentMint: Color
    get() = AccentMint

val accentSky: Color
    get() = AccentSky

val accentBlush: Color
    get() = AccentBlush

val accentSand: Color
    get() = AccentSand

// Neon colors (Dark mode) - PUBLIC
val neonAqua: Color
    get() = NeonAqua

val neonMagenta: Color
    get() = NeonMagenta

val neonLime: Color
    get() = NeonLime

val neonPurple: Color
    get() = NeonPurple

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Mevcut tema ayarlarını almak için utility fonksiyon
 */
@Composable
fun currentAppSettings(): AppSettings {
    return LocalAppSettings.current
}

/**
 * Koyu tema kullanılıyor mu?
 */
@Composable
fun isDarkThemeEnabled(): Boolean {
    return LocalAppSettings.current.isDarkMode
}

/**
 * Mevcut dil
 */
@Composable
fun currentLanguage(): String {
    return LocalAppSettings.current.language
}