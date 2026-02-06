package io.beachforecast.domain.models

import io.beachforecast.R

/**
 * Theme colors for widget
 * Provides color values for each theme that work in RemoteViews
 */
data class WidgetThemeColors(
    val background: Int,
    val cardBackground: Int,
    val cardDrawableRes: Int,
    val glassCardDrawableRes: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val tertiaryText: Int,
    val accent: Int,
    val recommendedColor: Int,
    val notRecommendedColor: Int
) {
    companion object {
        fun fromTheme(theme: AppTheme): WidgetThemeColors {
            return when (theme) {
                // System defaults to dark for widgets
                AppTheme.SYSTEM,
                AppTheme.DARK -> WidgetThemeColors(
                    background = 0xFF101F22.toInt(),      // StitchBackgroundDark
                    cardBackground = 0xFF1A2E32.toInt(), // StitchSurfaceDark
                    cardDrawableRes = R.drawable.rounded_card_dark,
                    glassCardDrawableRes = R.drawable.rounded_card_glass_dark,
                    primaryText = 0xFFFFFFFF.toInt(),     // White
                    secondaryText = 0xFF90C1CB.toInt(),  // StitchTextSecondary
                    tertiaryText = 0xFF5A7A82.toInt(),   // StitchTextTertiary
                    accent = 0xFF0DCCF2.toInt(),          // StitchPrimary
                    recommendedColor = 0xFF4CAF50.toInt(),   // Green
                    notRecommendedColor = 0xFF5A7A82.toInt() // Muted
                )
                // Stitch Light theme
                AppTheme.LIGHT -> WidgetThemeColors(
                    background = 0xFFF5F8F8.toInt(),     // StitchBackgroundLight
                    cardBackground = 0xFFFFFFFF.toInt(), // StitchSurfaceLight
                    cardDrawableRes = R.drawable.rounded_card_light,
                    glassCardDrawableRes = R.drawable.rounded_card_glass_light,
                    primaryText = 0xFF101F22.toInt(),    // StitchOnBackgroundLight
                    secondaryText = 0xFF5A7A82.toInt(), // StitchTextSecondaryLight
                    tertiaryText = 0xFF5A7A82.toInt(),  // StitchTextTertiary
                    accent = 0xFF0DCCF2.toInt(),         // StitchPrimary
                    recommendedColor = 0xFF2E7D32.toInt(),   // Darker green
                    notRecommendedColor = 0xFF90A4AE.toInt() // Grey
                )
            }
        }
    }
}
