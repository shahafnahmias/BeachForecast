package io.beachforecast.domain.formatters

import androidx.compose.ui.graphics.Color
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherMetric

/**
 * Compact formatters for displaying weather data in limited space
 * Used in forecast cards and widget period displays
 */
object CompactWeatherFormatter {

    /**
     * Format wind direction and speed compactly
     * Example: "NE@15km/h" or "SW@8mph"
     * Color is based on wind speed intensity
     */
    fun formatWindCompact(
        directionDegrees: Double,
        speedKmh: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): Pair<String, Color> {
        val direction = getWindDirection(directionDegrees)
        val speed = if (unitSystem == UnitSystem.METRIC) {
            "${speedKmh.toInt()}"
        } else {
            val mph = speedKmh * 0.621371
            "${mph.toInt()}"
        }

        val color = getWindColor(speedKmh)
        return "${direction}@${speed}" to color
    }

    /**
     * Format swell direction and height compactly
     * Example: "NE@40-60cm" or "SW@1-2ft"
     * Color is based on swell height and period (intensity)
     */
    fun formatSwellCompact(
        directionDegrees: Double,
        heightMeters: Double,
        periodSeconds: Double,
        unitSystem: UnitSystem = UnitSystem.METRIC
    ): Pair<String, Color> {
        val direction = getWindDirection(directionDegrees)
        val heightRange = UnitConverter.formatWaveHeightRange(heightMeters, unitSystem)

        val color = getSwellColor(heightMeters, periodSeconds)
        return "${direction}@${heightRange}" to color
    }

    /**
     * Format wave period compactly
     * Example: "🌊🕐 8s"
     */
    fun formatWavePeriodCompact(periodSeconds: Double): String {
        return "🌊🕐 ${periodSeconds.toInt()}s"
    }

    /**
     * Get wind direction abbreviation from degrees
     */
    private fun getWindDirection(degrees: Double): String {
        val normalized = degrees % 360
        return when {
            normalized >= 337.5 || normalized < 22.5 -> "N"
            normalized < 67.5 -> "NE"
            normalized < 112.5 -> "E"
            normalized < 157.5 -> "SE"
            normalized < 202.5 -> "S"
            normalized < 247.5 -> "SW"
            normalized < 292.5 -> "W"
            normalized < 337.5 -> "NW"
            else -> "N"
        }
    }

    /**
     * Get color for wind based on speed (km/h)
     * Green (calm) → Yellow → Orange → Red (dangerous)
     */
    private fun getWindColor(speedKmh: Double): Color {
        return when {
            speedKmh < 10 -> Color(0xFF4CAF50)   // Calm - Green
            speedKmh < 20 -> Color(0xFF8BC34A)   // Light - Light Green
            speedKmh < 30 -> Color(0xFFFFEB3B)   // Moderate - Yellow
            speedKmh < 40 -> Color(0xFFFF9800)   // Fresh - Orange
            speedKmh < 50 -> Color(0xFFFF5722)   // Strong - Deep Orange
            else -> Color(0xFFF44336)            // Very Strong - Red
        }
    }

    /**
     * Get color for swell based on height and period
     * Longer period + higher swell = more powerful/dangerous
     */
    private fun getSwellColor(heightMeters: Double, periodSeconds: Double): Color {
        // Calculate swell power index (combination of height and period)
        // Higher period with same height = more energy
        val powerIndex = heightMeters * (periodSeconds / 10.0)

        return when {
            powerIndex < 0.3 -> Color(0xFF4CAF50)   // Small swell - Green
            powerIndex < 0.6 -> Color(0xFF8BC34A)   // Light swell - Light Green
            powerIndex < 1.0 -> Color(0xFFFFEB3B)   // Moderate swell - Yellow
            powerIndex < 1.5 -> Color(0xFFFF9800)   // Fresh swell - Orange
            powerIndex < 2.0 -> Color(0xFFFF5722)   // Strong swell - Deep Orange
            else -> Color(0xFFF44336)               // Very strong swell - Red
        }
    }

    /**
     * Get metric icon/emoji for display
     */
    fun getMetricIcon(metric: WeatherMetric): String {
        return when (metric) {
            WeatherMetric.CLOUD_COVER -> "☁️"
            WeatherMetric.TEMPERATURE -> "🌡️"
            WeatherMetric.UV_INDEX -> "☀️"
            WeatherMetric.SWELL -> "〰️"
            WeatherMetric.WIND -> "💨"
            WeatherMetric.WAVE_PERIOD -> "🌊🕐"
        }
    }
}
