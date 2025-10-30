package com.example.naifdeneme.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MODAI Color System
 * Stitch tasarımlarından derlenen tutarlı renk paleti
 */

// ============================================
// PRIMARY COLORS (Aqua/Cyan - Ana Marka Rengi)
// ============================================
val PrimaryCyan = Color(0xFF06F9F9)        // Neon cyan (dark mode için)
val PrimarySky = Color(0xFF87CEEB)         // Pastel sky blue (light mode için)

// ============================================
// BACKGROUND COLORS
// ============================================
// Light Mode
val BackgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)
val SubtleLight = Color(0xFFF0F0F0)

// Dark Mode
val BackgroundDark = Color(0xFF121212)
val BackgroundDarkTeal = Color(0xFF0F2323)  // Su modülü için özel koyu teal
val SurfaceDark = Color(0xFF1C1C1E)
val SubtleDark = Color(0xFF2C2C2C)

// ============================================
// TEXT COLORS
// ============================================
// Light Mode
val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF8A8A8E)
val TextTertiaryLight = Color(0xFF6B7280)

// Dark Mode
val TextPrimaryDark = Color(0xFFF2F2F2)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextTertiaryDark = Color(0xFF9CA3AF)

// ============================================
// BORDER COLORS
// ============================================
val BorderLight = Color(0xFFE5E5EA)
val BorderDark = Color(0xFF373737)
val BorderDarkAlt = Color(0xFF38383A)

// ============================================
// ACCENT COLORS (Pastel - Light Mode)
// ============================================
val AccentMint = Color(0xFFA2E4B8)
val AccentSky = Color(0xFFA7D7F9)
val AccentBlush = Color(0xFFF7C5CC)
val AccentSand = Color(0xFFE7D4B5)

// ============================================
// ACCENT COLORS (Neon - Dark Mode)
// ============================================
val NeonAqua = Color(0xFF06F9F9)
val NeonMagenta = Color(0xFFF906F9)
val NeonLime = Color(0xFF06F906)
val NeonPurple = Color(0xFF9D06F9)

// ============================================
// SEMANTIC COLORS
// ============================================
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFFA726)
val ErrorRed = Color(0xFFEF5350)
val InfoBlue = Color(0xFF42A5F5)

// ============================================
// COMPONENT SPECIFIC COLORS
// ============================================
// Progress Bar
val ProgressBarBgLight = Color(0xFFF0F0F0)
val ProgressBarBgDark = Color(0xFF2C2C2E)
val ProgressBarFill = Color(0xFFA7D8F9)

// Toggle Switch
val ToggleInactive = Color(0xFFE5E5EA)
val ToggleActive = AccentMint

// Shadow Colors (for elevation)
val ShadowLight = Color(0x0D000000)  // rgba(0,0,0,0.05)
val ShadowDark = Color(0x33000000)   // rgba(0,0,0,0.2)