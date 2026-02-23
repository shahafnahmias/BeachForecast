package io.beachforecast.widget.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import io.beachforecast.domain.models.ConditionRating

/**
 * Stitch fallback colors for pre-Android 12 devices.
 */
private val StitchDarkScheme = darkColorScheme(
    primary = Color(0xFF0DCCF2),
    onPrimary = Color(0xFF101F22),
    surface = Color(0xFF1A2E32),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A2E32),
    onSurfaceVariant = Color(0xFF90C1CB),
    background = Color(0xFF101F22),
    onBackground = Color(0xFFFFFFFF)
)

private val StitchLightScheme = lightColorScheme(
    primary = Color(0xFF0DCCF2),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101F22),
    surfaceVariant = Color(0xFFF0F7F8),
    onSurfaceVariant = Color(0xFF5A7A82),
    background = Color(0xFFF5F8F8),
    onBackground = Color(0xFF101F22)
)

private val StitchColorProviders = ColorProviders(
    light = StitchLightScheme,
    dark = StitchDarkScheme
)

/**
 * Wraps content in GlanceTheme with dynamic colors on Android 12+
 * and Stitch palette fallback on older devices.
 */
@Composable
fun BeachForecastWidgetTheme(content: @Composable () -> Unit) {
    GlanceTheme(
        colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors // Dynamic colors from wallpaper
        } else {
            StitchColorProviders
        }
    ) {
        content()
    }
}

/**
 * Semantic condition colors — these do NOT change with wallpaper.
 * They carry meaning (good/bad) and must remain consistent.
 */
object ConditionColors {
    fun forRating(ratingName: String): Color = when (ratingName) {
        ConditionRating.EPIC.name -> Color(0xFF00E676)
        ConditionRating.EXCELLENT.name -> Color(0xFF4CAF50)
        ConditionRating.GOOD.name -> Color(0xFF66BB6A)
        ConditionRating.FAIR.name -> Color(0xFFFFB300)
        ConditionRating.POOR.name -> Color(0xFFFF9800)
        ConditionRating.FLAT.name -> Color(0xFF78909C)
        ConditionRating.DANGEROUS.name -> Color(0xFFF44336)
        else -> Color(0xFF78909C)
    }
}
