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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = AccentMint,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = SurfaceVariantLight,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant
)

// ============================================
// DARK COLOR SCHEME
// ============================================
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = AccentSky,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = SurfaceVariantDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = BorderDark,
    outlineVariant = OutlineVariant
)

// ============================================
// MAIN THEME COMPOSABLE
// ============================================
@Composable
fun ModaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 🔥 BU PARAMETRE ARTIK KULLANILIYOR
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 🔥 darkTheme parametresini kullan (MainActivity'den gelen Flow değeri)
    val useDarkTheme = darkTheme
    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    // AppSettings'i darkTheme parametresine göre güncelle
    val appSettings = AppSettings(
        isDarkMode = useDarkTheme,
        language = "tr", // Bu MainActivity'den alınabilir isterseniz
        dynamicColor = dynamicColor
    )

    CompositionLocalProvider(
        LocalAppSettings provides appSettings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
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