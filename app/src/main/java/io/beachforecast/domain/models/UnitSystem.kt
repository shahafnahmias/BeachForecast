package io.beachforecast.domain.models

/**
 * Unit system for displaying measurements
 */
enum class UnitSystem {
    METRIC,    // Celsius, meters, cm, km/h
    IMPERIAL;  // Fahrenheit, feet, inches, mph

    companion object {
        fun fromBoolean(useMetric: Boolean): UnitSystem {
            return if (useMetric) METRIC else IMPERIAL
        }
    }

    fun toBoolean(): Boolean = this == METRIC
}

/**
 * Unit conversion utilities
 */
object UnitConverter {

    /**
     * Convert temperature from Celsius to the target unit system
     */
    fun convertTemperature(celsius: Double, system: UnitSystem): Double {
        return when (system) {
            UnitSystem.METRIC -> celsius
            UnitSystem.IMPERIAL -> celsiusToFahrenheit(celsius)
        }
    }

    /**
     * Convert wave height from meters to the target unit system
     */
    fun convertWaveHeight(meters: Double, system: UnitSystem): Double {
        return when (system) {
            UnitSystem.METRIC -> meters * 100 // meters to cm
            UnitSystem.IMPERIAL -> metersToFeet(meters)
        }
    }

    /**
     * Convert wind speed from km/h to the target unit system
     */
    fun convertWindSpeed(kmh: Double, system: UnitSystem): Double {
        return when (system) {
            UnitSystem.METRIC -> kmh
            UnitSystem.IMPERIAL -> kmhToMph(kmh)
        }
    }

    /**
     * Get temperature unit symbol for the system
     */
    fun getTemperatureUnit(system: UnitSystem): String {
        return when (system) {
            UnitSystem.METRIC -> "°C"
            UnitSystem.IMPERIAL -> "°F"
        }
    }

    /**
     * Get wave height unit symbol for the system
     */
    fun getWaveHeightUnit(system: UnitSystem): String {
        return when (system) {
            UnitSystem.METRIC -> "cm"
            UnitSystem.IMPERIAL -> "ft"
        }
    }

    /**
     * Get wind speed unit symbol for the system
     */
    fun getWindSpeedUnit(system: UnitSystem): String {
        return when (system) {
            UnitSystem.METRIC -> "km/h"
            UnitSystem.IMPERIAL -> "mph"
        }
    }

    /**
     * Format wave height as a range (e.g., "20-40cm" or "1-2ft")
     * Uses 20cm increments for metric and 1ft increments for imperial
     */
    fun formatWaveHeightRange(meters: Double, system: UnitSystem): String {
        return when (system) {
            UnitSystem.METRIC -> {
                val cm = (meters * 100).toInt()
                val rangeSize = 20
                val lowerBound = (cm / rangeSize) * rangeSize
                val upperBound = lowerBound + rangeSize
                "$lowerBound-${upperBound}cm"
            }
            UnitSystem.IMPERIAL -> {
                val feet = metersToFeet(meters)
                val lowerBound = feet.toInt()
                val upperBound = lowerBound + 1
                "$lowerBound-${upperBound}ft"
            }
        }
    }

    // Internal conversion functions
    private fun celsiusToFahrenheit(celsius: Double): Double {
        return celsius * 9.0 / 5.0 + 32.0
    }

    private fun metersToFeet(meters: Double): Double {
        return meters * 3.28084
    }

    private fun kmhToMph(kmh: Double): Double {
        return kmh * 0.621371
    }
}
