package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Dynamic Theme Accessors matching the current active theme package
// Automatically adjusts for Light and Dark modes via MaterialTheme.colorScheme.
val GeoPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val GeoOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val GeoPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val GeoOnPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val GeoSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondary

val GeoOnSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondary

val GeoSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val GeoOnSecondaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondaryContainer

val GeoTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val GeoOnTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onTertiary

val GeoBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val GeoSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val GeoSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val GeoOnSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val GeoOnSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val GeoBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val GeoOutline: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val GeoHeaderKasus: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (theme.isBoldTheme) {
            Color(if (isDark) theme.headerKasusDarkHex else theme.headerKasusLightHex)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    }

val GeoIsBoldTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.isBoldTheme

val GeoGlowColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return Color(if (isDark) theme.glowColorDarkHex else theme.glowColorLightHex)
    }

val GeoCardRibbonColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return Color(if (isDark) theme.cardAccentRibbonDarkHex else theme.cardAccentRibbonLightHex)
    }

val GeoBannerGradient: List<Color>
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppThemePackage.current
        val isDark = LocalIsDarkTheme.current
        return if (isDark) {
            listOf(Color(theme.bannerGradientStartDarkHex), Color(theme.bannerGradientEndDarkHex))
        } else {
            listOf(Color(theme.bannerGradientStartLightHex), Color(theme.bannerGradientEndLightHex))
        }
    }

val GeoAccentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppThemePackage.current.accent

val GeoError: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val GeoOnError: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onError

// Static fallback values for non-composable usages if any
val DefaultGeoPrimary = Color(0xFF6A7A7A)
val DefaultGeoBackground = Color(0xFFF5F7F7)
val DefaultGeoSurface = Color(0xFFFFFFFF)

// Gender Accents (consistent grammatical color convention)
val GeoMaskulinText = Color(0xFF1565C0)
val GeoMaskulinBg = Color(0xFFE0F2FE)
val GeoFemininText = Color(0xFFC2185B)
val GeoFemininBg = Color(0xFFFCE7F3)
val GeoNeutrumText = Color(0xFF15803D)
val GeoNeutrumBg = Color(0xFFDCFCE7)
val GeoPluralText = Color(0xFF6B21A8)
val GeoPluralBg = Color(0xFFF3E8FF)
