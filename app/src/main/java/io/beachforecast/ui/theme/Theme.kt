package io.beachforecast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.beachforecast.domain.models.AppTheme

// ==========================================
// Stitch Design System Color Schemes
// ==========================================

// Stitch Dark Theme - Primary theme matching the Stitch design
private val StitchDarkColorScheme = darkColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchBackgroundDark,
    primaryContainer = StitchPrimaryDark,
    onPrimaryContainer = Color.White,

    secondary = StitchPrimary,
    onSecondary = StitchBackgroundDark,
    secondaryContainer = StitchSurfaceDark,
    onSecondaryContainer = Color.White,

    tertiary = StitchPrimaryLight,
    onTertiary = StitchBackgroundDark,

    background = StitchBackgroundDark,
    onBackground = StitchOnBackgroundDark,

    surface = StitchSurfaceDark,
    onSurface = StitchOnSurfaceDark,
    surfaceVariant = StitchSurfaceVariantDark,
    onSurfaceVariant = StitchTextSecondary,

    error = StitchRed,
    onError = Color.White,

    outline = StitchTextTertiary,
    outlineVariant = StitchSurfaceVariantDark
)

// Stitch Light Theme - Light variant of the Stitch design
private val StitchLightColorScheme = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = Color.White,
    primaryContainer = StitchPrimaryLight,
    onPrimaryContainer = StitchBackgroundDark,

    secondary = StitchPrimary,
    onSecondary = Color.White,
    secondaryContainer = StitchSurfaceVariantLight,
    onSecondaryContainer = StitchBackgroundDark,

    tertiary = StitchPrimaryDark,
    onTertiary = Color.White,

    background = StitchBackgroundLight,
    onBackground = StitchOnBackgroundLight,

    surface = StitchSurfaceLight,
    onSurface = StitchOnSurfaceLight,
    surfaceVariant = StitchSurfaceVariantLight,
    onSurfaceVariant = StitchTextSecondaryLight,

    error = StitchRed,
    onError = Color.White,

    outline = StitchTextSecondaryLight,
    outlineVariant = StitchSurfaceVariantLight
)

// ==========================================
// Extended Stitch Colors (for custom components)
// ==========================================
data class StitchExtendedColors(
    val textSecondary: Color,
    val textTertiary: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val activeCardBackground: Color,
    val activeCardBorder: Color,
    val amber: Color,
    val emerald: Color,
    val slate: Color,
    val isDark: Boolean
)

val LocalStitchColors = staticCompositionLocalOf {
    StitchExtendedColors(
        textSecondary = StitchTextSecondary,
        textTertiary = StitchTextTertiary,
        glassBackground = StitchGlassBackground,
        glassBorder = StitchGlassBorder,
        activeCardBackground = StitchActiveCardBackground,
        activeCardBorder = StitchActiveCardBorder,
        amber = StitchAmber,
        emerald = StitchEmerald,
        slate = StitchSlate,
        isDark = true
    )
}

private val StitchDarkExtendedColors = StitchExtendedColors(
    textSecondary = StitchTextSecondary,
    textTertiary = StitchTextTertiary,
    glassBackground = StitchGlassBackground,
    glassBorder = StitchGlassBorder,
    activeCardBackground = StitchActiveCardBackground,
    activeCardBorder = StitchActiveCardBorder,
    amber = StitchAmber,
    emerald = StitchEmerald,
    slate = StitchSlate,
    isDark = true
)

private val StitchLightExtendedColors = StitchExtendedColors(
    textSecondary = StitchTextSecondaryLight,
    textTertiary = StitchTextTertiary,
    glassBackground = Color(0x0A000000),        // 4% black
    glassBorder = Color(0x14000000),            // 8% black
    activeCardBackground = Color(0x1A0DCCF2),   // 10% primary
    activeCardBorder = Color(0x4D0DCCF2),       // 30% primary
    amber = StitchAmber,
    emerald = StitchEmerald,
    slate = StitchSlate,
    isDark = false
)

@Composable
fun SeaLevelWidgetTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val colorScheme = if (isDark) StitchDarkColorScheme else StitchLightColorScheme

    val extendedColors = if (isDark) {
        StitchDarkExtendedColors
    } else {
        StitchLightExtendedColors
    }

    CompositionLocalProvider(LocalStitchColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension to access Stitch extended colors
object StitchTheme {
    val colors: StitchExtendedColors
        @Composable
        get() = LocalStitchColors.current
}