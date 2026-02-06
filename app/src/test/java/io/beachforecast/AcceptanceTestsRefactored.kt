package io.beachforecast

import io.beachforecast.domain.calculators.WaveCategoryCalculator
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import org.junit.Test
import org.junit.Assert.*

/**
 * Refactored acceptance tests using new domain models
 */
class AcceptanceTestsRefactored {

    // Helper to create test forecast data using domain models
    private fun createHourlyForecast(time: String, waveHeight: Double): HourlyWaveForecast {
        return HourlyWaveForecast(
            time = time,
            waveHeightMeters = waveHeight,
            waveCategory = WaveCategory.fromHeight(waveHeight)
        )
    }

    /**
     * AC1 - Same category all day should show All Day text and hide breakdown
     */
    @Test
    fun `AC1 - Same category all day should show All Day text and hide breakdown`() {
        // Given
        val currentHeight = 1.2
        val currentCategory = WaveCategory.fromHeight(currentHeight)

        val todayRemaining = listOf(
            createHourlyForecast("13:00", 1.20),  // Waist (120 cm)
            createHourlyForecast("14:00", 1.25),  // Waist (125 cm)
            createHourlyForecast("15:00", 1.18),  // Waist (118 cm)
            createHourlyForecast("16:00", 1.30),  // Waist (130 cm)
            createHourlyForecast("17:00", 1.22),  // Waist (122 cm)
        )

        // Convert to WaveCategory enum list for comparison
        val forecastCategories = todayRemaining.map { it.waveCategory }

        // When - check if all same category using enum comparison
        val allSameCategory = WaveCategoryCalculator.areAllSameCategoryEnum(
            currentCategory,
            forecastCategories
        )

        // Then
        assertTrue("Should detect all hours are same category", allSameCategory)
        assertEquals("Current category should be Waist", WaveCategory.WAIST, currentCategory)

        // Verify all forecast items are indeed "Waist" category
        todayRemaining.forEach { forecast ->
            assertEquals("Each hour should be Waist category", WaveCategory.WAIST, forecast.waveCategory)
        }
    }

    /**
     * AC2 - Different categories during day should show breakdown cells
     */
    @Test
    fun `AC2 - Different categories during day should show breakdown cells`() {
        // Given
        val currentHeight = 1.2
        val currentCategory = WaveCategory.fromHeight(currentHeight)

        val todayRemaining = listOf(
            createHourlyForecast("13:00", 1.20),  // Waist (120 cm)
            createHourlyForecast("14:00", 1.25),  // Waist (125 cm)
            createHourlyForecast("15:00", 1.18),  // Waist (118 cm)
            createHourlyForecast("16:00", 0.95),  // Knee (95 cm) - CHANGE
            createHourlyForecast("17:00", 0.85),  // Knee (85 cm)
        )

        val forecastCategories = todayRemaining.map { it.waveCategory }

        // When - check if all same category using enum comparison
        val allSameCategory = WaveCategoryCalculator.areAllSameCategoryEnum(
            currentCategory,
            forecastCategories
        )

        // Then
        assertFalse("Should detect categories differ", allSameCategory)

        // Verify the change point
        val categoryNames = todayRemaining.map { it.waveCategory }.distinct()
        assertTrue("Should have Waist hours", categoryNames.contains(WaveCategory.WAIST))
        assertTrue("Should have Knee hours", categoryNames.contains(WaveCategory.KNEE))
    }

    /**
     * AC3 - Multiple category changes should show all changes in breakdown
     */
    @Test
    fun `AC3 - Multiple category changes should show all changes in breakdown`() {
        // Given
        val currentHeight = 0.2
        val currentCategory = WaveCategory.fromHeight(currentHeight)

        val todayRemaining = listOf(
            createHourlyForecast("13:00", 0.20),  // Flat (20 cm)
            createHourlyForecast("14:00", 0.25),  // Flat (25 cm)
            createHourlyForecast("15:00", 0.45),  // Ankle (45 cm) - CHANGE 1
            createHourlyForecast("16:00", 0.50),  // Ankle (50 cm)
            createHourlyForecast("17:00", 0.55),  // Ankle (55 cm)
            createHourlyForecast("18:00", 0.75),  // Knee (75 cm) - CHANGE 2
            createHourlyForecast("19:00", 0.80),  // Knee (80 cm)
        )

        val forecastCategories = todayRemaining.map { it.waveCategory }

        // When - check if all same category using enum comparison
        val allSameCategory = WaveCategoryCalculator.areAllSameCategoryEnum(
            currentCategory,
            forecastCategories
        )

        // Then
        assertFalse("Should detect categories differ", allSameCategory)

        // Verify we have 3 distinct categories
        val uniqueCategories = todayRemaining.map { it.waveCategory }.distinct()
        assertEquals("Should have 3 distinct categories", 3, uniqueCategories.size)
        assertTrue("Should have Flat", uniqueCategories.contains(WaveCategory.FLAT))
        assertTrue("Should have Ankle", uniqueCategories.contains(WaveCategory.ANKLE))
        assertTrue("Should have Knee", uniqueCategories.contains(WaveCategory.KNEE))
    }

    /**
     * AC4 - Wave category enum works correctly
     */
    @Test
    fun `AC4 - WaveCategory enum categorizes heights correctly`() {
        assertEquals(WaveCategory.FLAT, WaveCategory.fromHeight(0.2))
        assertEquals(WaveCategory.ANKLE, WaveCategory.fromHeight(0.5))
        assertEquals(WaveCategory.KNEE, WaveCategory.fromHeight(0.8))
        assertEquals(WaveCategory.WAIST, WaveCategory.fromHeight(1.2))
        assertEquals(WaveCategory.CHEST, WaveCategory.fromHeight(1.8))
        assertEquals(WaveCategory.HEAD_HIGH, WaveCategory.fromHeight(2.2))
        assertEquals(WaveCategory.OVERHEAD, WaveCategory.fromHeight(3.0))
        assertEquals(WaveCategory.DOUBLE_OVERHEAD, WaveCategory.fromHeight(4.5))
        assertEquals(WaveCategory.TRIPLE_OVERHEAD_PLUS, WaveCategory.fromHeight(6.0))
    }

    /**
     * AC5 - Category extraction works with both legacy and new formats
     */
    @Test
    fun `AC5 - Category name extraction works`() {
        // Legacy format still supported
        assertEquals("Waist", WaveCategoryCalculator.extractCategoryName("Waist (120 cm)"))
        assertEquals("Flat", WaveCategoryCalculator.extractCategoryName("Flat (20 cm)"))
        assertEquals("Head High", WaveCategoryCalculator.extractCategoryName("Head High (220 cm)"))

        // New format - returns the string as-is since there's no category name
        assertEquals("120cm", WaveCategoryCalculator.extractCategoryName("120cm"))
        assertEquals("45cm", WaveCategoryCalculator.extractCategoryName("45cm"))
    }

    /**
     * AC6 - Same category comparison works with enum-based approach
     */
    @Test
    fun `AC6 - Same category comparison using enum`() {
        // Using enum comparison (recommended for new format)
        assertTrue(
            WaveCategoryCalculator.areAllSameCategoryEnum(
                WaveCategory.WAIST,
                listOf(WaveCategory.WAIST, WaveCategory.WAIST)
            )
        )

        assertFalse(
            WaveCategoryCalculator.areAllSameCategoryEnum(
                WaveCategory.WAIST,
                listOf(WaveCategory.WAIST, WaveCategory.KNEE)
            )
        )

        // Legacy string comparison still works for backwards compatibility
        assertTrue(
            WaveCategoryCalculator.isSameCategory(
                "Waist (120 cm)",
                "Waist (125 cm)"
            )
        )
    }
}
