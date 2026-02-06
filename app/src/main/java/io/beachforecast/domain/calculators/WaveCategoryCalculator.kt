package io.beachforecast.domain.calculators

import io.beachforecast.domain.models.WaveCategory

/**
 * Pure business logic for wave category calculations
 * Single source of truth for categorization
 */
object WaveCategoryCalculator {

    /**
     * Calculate wave category from height in meters
     */
    fun calculateCategory(heightMeters: Double): WaveCategory {
        return WaveCategory.fromHeight(heightMeters)
    }

    /**
     * Calculate category with formatted height string (returns CM only)
     */
    fun calculateCategoryWithHeight(heightMeters: Double): String {
        val category = calculateCategory(heightMeters)
        return category.formatWithHeight(heightMeters)
    }

    /**
     * Extract category name from formatted string
     * Supports both legacy format "Waist (120 cm)" and new format "120cm"
     * For new format, returns the string as-is since there's no category name
     */
    fun extractCategoryName(formattedCategory: String): String {
        val parenIndex = formattedCategory.indexOf(" (")
        return if (parenIndex != -1) {
            formattedCategory.substring(0, parenIndex)
        } else {
            formattedCategory
        }
    }

    /**
     * Compare if two categories are the same (ignoring height values)
     * For legacy format, extracts and compares category names
     * For new format, compares the height strings directly
     */
    fun isSameCategory(category1: String, category2: String): Boolean {
        val name1 = extractCategoryName(category1)
        val name2 = extractCategoryName(category2)
        return name1 == name2
    }

    /**
     * Check if all forecast hours have the same category as current
     * Compares extracted category names (legacy) or height strings (new format)
     */
    fun areAllSameCategory(
        currentCategory: String,
        forecastCategories: List<String>
    ): Boolean {
        val currentName = extractCategoryName(currentCategory)
        return forecastCategories.all { forecast ->
            extractCategoryName(forecast) == currentName
        }
    }

    /**
     * Check if all wave categories are the same (recommended for new format)
     * Uses WaveCategory enum directly instead of string parsing
     */
    fun areAllSameCategoryEnum(
        currentCategory: WaveCategory,
        forecastCategories: List<WaveCategory>
    ): Boolean {
        return forecastCategories.all { it == currentCategory }
    }
}
