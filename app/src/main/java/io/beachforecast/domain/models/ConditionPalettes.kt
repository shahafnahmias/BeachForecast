package io.beachforecast.domain.models

import android.graphics.Color

/**
 * Color palettes for different weather conditions
 * Each palette indicates intensity/severity of the condition
 */
object ConditionPalettes {

    /**
     * Wind speed color palette (km/h)
     * Light winds: Calm, gentle
     * Moderate winds: Good conditions
     * Strong winds: Challenging
     * Very strong: Dangerous
     */
    fun getWindColor(windSpeedKmh: Double): Int {
        return when {
            windSpeedKmh < 10 -> Color.parseColor("#4CAF50")  // Light - Green (Calm)
            windSpeedKmh < 20 -> Color.parseColor("#8BC34A")  // Light breeze - Light green
            windSpeedKmh < 30 -> Color.parseColor("#FFC107")  // Moderate - Amber
            windSpeedKmh < 40 -> Color.parseColor("#FF9800")  // Fresh - Orange
            windSpeedKmh < 50 -> Color.parseColor("#FF5722")  // Strong - Deep orange
            else -> Color.parseColor("#F44336")              // Very strong - Red
        }
    }

    fun getWindLabel(windSpeedKmh: Double): String {
        return when {
            windSpeedKmh < 10 -> "Calm"
            windSpeedKmh < 20 -> "Light"
            windSpeedKmh < 30 -> "Moderate"
            windSpeedKmh < 40 -> "Fresh"
            windSpeedKmh < 50 -> "Strong"
            else -> "Very Strong"
        }
    }

    /**
     * Swell height color palette (meters)
     * Small swell: Easy conditions
     * Medium swell: Typical
     * Large swell: Advanced
     * Very large: Expert only
     */
    fun getSwellColor(swellHeightMeters: Double): Int {
        return when {
            swellHeightMeters < 0.5 -> Color.parseColor("#2196F3")  // Tiny - Light blue
            swellHeightMeters < 1.0 -> Color.parseColor("#03A9F4")  // Small - Blue
            swellHeightMeters < 1.5 -> Color.parseColor("#00BCD4")  // Medium - Cyan
            swellHeightMeters < 2.0 -> Color.parseColor("#FFC107")  // Large - Amber
            swellHeightMeters < 3.0 -> Color.parseColor("#FF9800")  // Very large - Orange
            else -> Color.parseColor("#FF5722")                    // Huge - Deep orange
        }
    }

    fun getSwellLabel(swellHeightMeters: Double): String {
        return when {
            swellHeightMeters < 0.5 -> "Tiny"
            swellHeightMeters < 1.0 -> "Small"
            swellHeightMeters < 1.5 -> "Medium"
            swellHeightMeters < 2.0 -> "Large"
            swellHeightMeters < 3.0 -> "Very Large"
            else -> "Huge"
        }
    }

    /**
     * UV Index color palette
     * Low: Safe
     * Moderate: Caution
     * High: Protection needed
     * Very high/Extreme: Danger
     */
    fun getUvColor(uvIndex: Double): Int {
        return when {
            uvIndex < 3 -> Color.parseColor("#4CAF50")   // Low - Green
            uvIndex < 6 -> Color.parseColor("#FFEB3B")   // Moderate - Yellow
            uvIndex < 8 -> Color.parseColor("#FF9800")   // High - Orange
            uvIndex < 11 -> Color.parseColor("#F44336")  // Very high - Red
            else -> Color.parseColor("#9C27B0")          // Extreme - Purple
        }
    }

    fun getUvLabel(uvIndex: Double): String {
        return when {
            uvIndex < 3 -> "Low"
            uvIndex < 6 -> "Moderate"
            uvIndex < 8 -> "High"
            uvIndex < 11 -> "Very High"
            else -> "Extreme"
        }
    }

    /**
     * Temperature color palette (Celsius)
     * Cold: Blue tones
     * Cool: Light blue
     * Mild: Green
     * Warm: Orange
     * Hot: Red
     */
    fun getTempColor(tempCelsius: Double): Int {
        return when {
            tempCelsius < 10 -> Color.parseColor("#2196F3")  // Cold - Blue
            tempCelsius < 15 -> Color.parseColor("#03A9F4")  // Cool - Light blue
            tempCelsius < 20 -> Color.parseColor("#4CAF50")  // Mild - Green
            tempCelsius < 25 -> Color.parseColor("#8BC34A")  // Pleasant - Light green
            tempCelsius < 30 -> Color.parseColor("#FFC107")  // Warm - Amber
            tempCelsius < 35 -> Color.parseColor("#FF9800")  // Hot - Orange
            else -> Color.parseColor("#F44336")              // Very hot - Red
        }
    }

    fun getTempLabel(tempCelsius: Double): String {
        return when {
            tempCelsius < 10 -> "Cold"
            tempCelsius < 15 -> "Cool"
            tempCelsius < 20 -> "Mild"
            tempCelsius < 25 -> "Pleasant"
            tempCelsius < 30 -> "Warm"
            tempCelsius < 35 -> "Hot"
            else -> "Very Hot"
        }
    }

    /**
     * Get a lighter version of a color for backgrounds
     */
    fun getLighterColor(color: Int, alpha: Int = 40): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
