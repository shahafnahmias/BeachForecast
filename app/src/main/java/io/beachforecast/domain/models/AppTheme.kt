package io.beachforecast.domain.models

import androidx.annotation.StringRes
import io.beachforecast.R

/**
 * Available app themes - Stitch design system
 */
enum class AppTheme(
    val displayName: String,
    @StringRes val nameRes: Int
) {
    SYSTEM("System", R.string.theme_system),
    DARK("Dark", R.string.theme_dark),
    LIGHT("Light", R.string.theme_light);

    companion object {
        fun fromString(value: String?): AppTheme {
            // Map legacy theme names to new ones
            return when (value) {
                "SYSTEM" -> SYSTEM
                "DARK", "OCEAN_BLUE", "PURPLE" -> DARK
                "LIGHT" -> LIGHT
                else -> SYSTEM
            }
        }
    }
}
