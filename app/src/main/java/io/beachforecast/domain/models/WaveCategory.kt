package io.beachforecast.domain.models

import androidx.annotation.StringRes
import io.beachforecast.R

/**
 * Enum representing wave height categories relative to human body parts
 * @param displayName Human-readable name (e.g., "Flat", "Waist")
 * @param minHeight Minimum wave height in meters (inclusive)
 * @param maxHeight Maximum wave height in meters (exclusive)
 * @param level Numeric level for comparisons (0 = lowest, 8 = highest)
 * @param emoji Text emoji representation
 */
enum class WaveCategory(
    val displayName: String,
    @StringRes val nameRes: Int,
    val minHeight: Double,
    val maxHeight: Double,
    val level: Int,
    val emoji: String
) {
    FLAT(
        displayName = "Flat",
        nameRes = R.string.wave_flat,
        minHeight = 0.0,
        maxHeight = 0.3,
        level = 0,
        emoji = "🧍─"
    ),
    ANKLE(
        displayName = "Ankle",
        nameRes = R.string.wave_ankle,
        minHeight = 0.3,
        maxHeight = 0.6,
        level = 1,
        emoji = "🧍〰"
    ),
    KNEE(
        displayName = "Knee",
        nameRes = R.string.wave_knee,
        minHeight = 0.6,
        maxHeight = 1.0,
        level = 2,
        emoji = "🧍～"
    ),
    WAIST(
        displayName = "Waist",
        nameRes = R.string.wave_waist,
        minHeight = 1.0,
        maxHeight = 1.5,
        level = 3,
        emoji = "🧍🌊"
    ),
    CHEST(
        displayName = "Chest",
        nameRes = R.string.wave_chest,
        minHeight = 1.5,
        maxHeight = 2.0,
        level = 4,
        emoji = "🧍🌊🌊"
    ),
    HEAD_HIGH(
        displayName = "Head High",
        nameRes = R.string.wave_head_high,
        minHeight = 2.0,
        maxHeight = 2.5,
        level = 5,
        emoji = "🌊🧍"
    ),
    OVERHEAD(
        displayName = "Overhead",
        nameRes = R.string.wave_overhead,
        minHeight = 2.5,
        maxHeight = 3.5,
        level = 6,
        emoji = "🌊🧍🌊"
    ),
    DOUBLE_OVERHEAD(
        displayName = "Double Overhead",
        nameRes = R.string.wave_double_overhead,
        minHeight = 3.5,
        maxHeight = 5.0,
        level = 7,
        emoji = "🌊🌊🧍"
    ),
    TRIPLE_OVERHEAD_PLUS(
        displayName = "Triple Overhead+",
        nameRes = R.string.wave_triple_overhead,
        minHeight = 5.0,
        maxHeight = Double.MAX_VALUE,
        level = 8,
        emoji = "🌊🌊🌊🧍"
    );

    companion object {
        /**
         * Get wave category from height in meters
         */
        fun fromHeight(heightMeters: Double): WaveCategory {
            return values().first { heightMeters >= it.minHeight && heightMeters < it.maxHeight }
        }

        /**
         * Get category from display name (e.g., "Waist")
         */
        fun fromDisplayName(name: String): WaveCategory? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }
    }

    /**
     * Format height as a range using specified unit system (e.g., "20-40cm")
     */
    fun formatWithHeight(heightMeters: Double, unitSystem: UnitSystem = UnitSystem.METRIC): String {
        return UnitConverter.formatWaveHeightRange(heightMeters, unitSystem)
    }

}
