package com.example.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private fun buildLightColorScheme(theme: AppThemePackage) = if (theme.hasCustomPalette) {
    lightColorScheme(
        primary = Color(theme.primaryLightHex),
        onPrimary = Color(theme.onPrimaryLightHex),
        primaryContainer = Color(theme.primaryContainerLightHex),
        onPrimaryContainer = Color(theme.onPrimaryContainerLightHex),
        secondary = Color(theme.secondaryLightHex),
        onSecondary = Color(theme.onSecondaryLightHex),
        secondaryContainer = Color(theme.secondaryContainerLightHex),
        onSecondaryContainer = Color(theme.onSecondaryContainerLightHex),
        tertiary = Color(theme.tertiaryLightHex),
        onTertiary = Color(theme.onTertiaryLightHex),
        tertiaryContainer = Color(theme.tertiaryContainerLightHex),
        onTertiaryContainer = Color(theme.onTertiaryContainerLightHex),
        error = Color(theme.errorLightHex),
        onError = Color(theme.onErrorLightHex),
        errorContainer = Color(theme.errorContainerLightHex),
        onErrorContainer = Color(theme.onErrorContainerLightHex),
        background = Color(theme.backgroundLightHex),
        onBackground = Color(theme.onBackgroundLightHex),
        surface = Color(theme.surfaceLightHex),
        onSurface = Color(theme.onSurfaceLightHex),
        surfaceVariant = Color(theme.surfaceVariantLightHex),
        onSurfaceVariant = Color(theme.onSurfaceVariantLightHex),
        outline = Color(theme.outlineLightHex),
        outlineVariant = Color(theme.borderHex)
    )
} else {
    lightColorScheme(
        primary = theme.primary,
        onPrimary = Color.White,
        primaryContainer = theme.primaryContainer,
        onPrimaryContainer = theme.onPrimaryContainer,
        secondary = theme.secondary,
        onSecondary = Color.White,
        secondaryContainer = theme.primaryContainer,
        onSecondaryContainer = theme.onPrimaryContainer,
        tertiary = theme.accent,
        onTertiary = Color.White,
        tertiaryContainer = theme.surfaceVariant,
        onTertiaryContainer = theme.onSurfaceVariant,
        background = theme.background,
        onBackground = theme.onSurface,
        surface = theme.surface,
        onSurface = theme.onSurface,
        surfaceVariant = theme.surfaceVariant,
        onSurfaceVariant = theme.onSurfaceVariant,
        outline = theme.outline,
        outlineVariant = theme.border,
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

private fun buildDarkColorScheme(theme: AppThemePackage) = if (theme.hasCustomPalette) {
    darkColorScheme(
        primary = Color(theme.primaryDarkHex),
        onPrimary = Color(theme.onPrimaryDarkHex),
        primaryContainer = Color(theme.primaryContainerDarkHex),
        onPrimaryContainer = Color(theme.onPrimaryContainerDarkHex),
        secondary = Color(theme.secondaryDarkHex),
        onSecondary = Color(theme.onSecondaryDarkHex),
        secondaryContainer = Color(theme.secondaryContainerDarkHex),
        onSecondaryContainer = Color(theme.onSecondaryContainerDarkHex),
        tertiary = Color(theme.tertiaryDarkHex),
        onTertiary = Color(theme.onTertiaryDarkHex),
        tertiaryContainer = Color(theme.tertiaryContainerDarkHex),
        onTertiaryContainer = Color(theme.onTertiaryContainerDarkHex),
        error = Color(theme.errorDarkHex),
        onError = Color(theme.onErrorDarkHex),
        errorContainer = Color(theme.errorContainerDarkHex),
        onErrorContainer = Color(theme.onErrorContainerDarkHex),
        background = Color(theme.backgroundDarkHex),
        onBackground = Color(theme.onBackgroundDarkHex),
        surface = Color(theme.surfaceDarkHex),
        onSurface = Color(theme.onSurfaceDarkHex),
        surfaceVariant = Color(theme.surfaceVariantDarkHex),
        onSurfaceVariant = Color(theme.onSurfaceVariantDarkHex),
        outline = Color(theme.outlineDarkHex),
        outlineVariant = if (theme.isBoldTheme) Color(theme.outlineDarkHex).copy(alpha = 0.35f) else Color(0xFF4C453D)
    )
} else {
    darkColorScheme(
        primary = theme.accent,
        onPrimary = Color(0xFF1E1A16),
        primaryContainer = theme.primary,
        onPrimaryContainer = Color(0xFFFFDBCF),
        secondary = theme.secondary,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF2C2824),
        onSecondaryContainer = Color(0xFFE8E2DC),
        tertiary = theme.swatch,
        onTertiary = Color(0xFF1E1A16),
        background = Color(0xFF141311),
        onBackground = Color(0xFFE8E2DC),
        surface = Color(0xFF1C1B18),
        onSurface = Color(0xFFE8E2DC),
        surfaceVariant = Color(0xFF2B2824),
        onSurfaceVariant = Color(0xFFCFC4BA),
        outline = Color(0xFF988E84),
        outlineVariant = Color(0xFF4C453D),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

@Composable
fun MyApplicationTheme(
    selectedTheme: AppThemePackage = AppThemePackage.SCHIEFER,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(selectedTheme)
        else -> buildLightColorScheme(selectedTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                var ctx = view.context
                while (ctx is android.content.ContextWrapper && ctx !is Activity) {
                    ctx = ctx.baseContext
                }
                val window = (ctx as? Activity)?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                }
            } catch (_: Throwable) {
                // Safe ignore for tests and custom context wrappers
            }
        }
    }

    CompositionLocalProvider(
        LocalAppThemePackage provides selectedTheme,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
