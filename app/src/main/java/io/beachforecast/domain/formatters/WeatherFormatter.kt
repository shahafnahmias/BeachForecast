package io.beachforecast.domain.formatters

import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.PeriodForecast
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem

/**
 * Pure formatting logic for weather data
 * NO Android dependencies
 * Supports both metric and imperial units
 */
object WeatherFormatter {

    /**
     * Format temperature with unit system
     */
    fun formatTemperature(temperatureCelsius: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        val temp = UnitConverter.convertTemperature(temperatureCelsius, unitSystem)
        val unit = UnitConverter.getTemperatureUnit(unitSystem)
        return "%.0f%s".format(temp, unit)
    }

    /**
     * Format cloud cover as icon
     */
    fun formatCloudCover(cloudCoverLevel: CloudCoverLevel): String {
        return cloudCoverLevel.icon
    }

    /**
     * Format tomorrow's period forecast
     * Example: "Morning: ☀️ 22° · 40-60cm"
     */
    fun formatPeriodForecast(period: PeriodForecast, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        val cloudIcon = period.avgCloudCover.icon
        val temp = formatTemperature(period.avgTemperatureCelsius, unitSystem)
        val waveText = period.avgWaveCategory.formatWithHeight(period.avgWaveHeightMeters, unitSystem)

        return "${period.label}: $cloudIcon $temp · $waveText"
    }

    /**
     * Format multiple period forecasts
     */
    fun formatTomorrowForecast(periods: List<PeriodForecast>, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        return periods.joinToString(separator = "\n") { formatPeriodForecast(it, unitSystem) }
    }

    /**
     * Format "All Day" text
     */
    fun formatAllDayText(categoryWithHeight: String): String {
        return "$categoryWithHeight - All Day"
    }

    /**
     * Format wind speed with unit system
     */
    fun formatWindSpeed(windSpeedKmh: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        val speed = UnitConverter.convertWindSpeed(windSpeedKmh, unitSystem)
        val unit = UnitConverter.getWindSpeedUnit(unitSystem)
        return "%.1f %s".format(speed, unit)
    }

    /**
     * Format direction in degrees to compass direction
     */
    fun formatDirection(degrees: Int): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees + 22.5) / 45.0).toInt() % 8
        return "${directions[index]} ($degrees°)"
    }

    /**
     * Format wind direction with arrow
     */
    fun formatWindDirection(degrees: Int): String {
        return formatDirection(degrees)
    }

    /**
     * Format wave direction
     */
    fun formatWaveDirection(degrees: Int): String {
        return formatDirection(degrees)
    }

    /**
     * Format swell direction
     */
    fun formatSwellDirection(degrees: Int): String {
        return formatDirection(degrees)
    }

    /**
     * Format UV index with risk level
     */
    fun formatUvIndex(uvIndex: Double): String {
        val level = when {
            uvIndex < 3 -> "Low"
            uvIndex < 6 -> "Moderate"
            uvIndex < 8 -> "High"
            uvIndex < 11 -> "Very High"
            else -> "Extreme"
        }
        return "%.1f ($level)".format(uvIndex)
    }

    /**
     * Format wave period in seconds
     */
    fun formatWavePeriod(periodSeconds: Double): String {
        return "%.1fs".format(periodSeconds)
    }

    /**
     * Format swell period in seconds
     */
    fun formatSwellPeriod(periodSeconds: Double): String {
        return "%.1fs".format(periodSeconds)
    }

    /**
     * Format swell height as a range with unit system
     */
    fun formatSwellHeight(heightMeters: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        return UnitConverter.formatWaveHeightRange(heightMeters, unitSystem)
    }

    /**
     * Format sea surface temperature with unit system
     */
    fun formatSeaTemperature(temperatureCelsius: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        val temp = UnitConverter.convertTemperature(temperatureCelsius, unitSystem)
        val unit = UnitConverter.getTemperatureUnit(unitSystem)
        return "%.1f%s".format(temp, unit)
    }

    /**
     * Format wind speed compactly with compass direction for widget vitals
     * Returns "12 km/h NW" (no parentheses, no degree symbol)
     */
    fun formatWindCompact(speedKmh: Double, directionDegrees: Int, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        val speed = UnitConverter.convertWindSpeed(speedKmh, unitSystem)
        val unit = UnitConverter.getWindSpeedUnit(unitSystem)
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((directionDegrees + 22.5) / 45.0).toInt() % 8
        val compass = directions[index]
        return "%.0f %s %s".format(speed, unit, compass)
    }

    /**
     * Format swell compactly for widget vitals
     * Returns "0.5m 6s" (metric) or "1.6ft 6s" (imperial)
     */
    fun formatSwellCompact(heightMeters: Double, periodSeconds: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "%.1fm %.0fs".format(heightMeters, periodSeconds)
            UnitSystem.IMPERIAL -> "%.1fft %.0fs".format(heightMeters * 3.28084, periodSeconds)
        }
    }

    /**
     * Format UV index compactly for widget vitals
     * Returns "6 High" (integer + risk label, no parentheses)
     */
    fun formatUvCompact(uvIndex: Double): String {
        val level = when {
            uvIndex < 3 -> "Low"
            uvIndex < 6 -> "Moderate"
            uvIndex < 8 -> "High"
            uvIndex < 11 -> "Very High"
            else -> "Extreme"
        }
        return "%.0f %s".format(uvIndex, level)
    }
}
