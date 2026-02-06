package io.beachforecast.domain.usecases

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for formatting use case
 * These are pure unit tests - fast, no Android dependencies
 */
class FormatWeatherForDisplayUseCaseTest {

    private val useCase = FormatWeatherForDisplayUseCase

    // ========== formatTime() Tests ==========

    @Test
    fun `formatTime with valid ISO timestamp returns formatted time`() {
        val result = useCase.formatTime("2024-01-15T14:00")
        assertTrue(result.contains("PM") || result.contains("pm"))
    }

    @Test
    fun `formatTime with short string returns original`() {
        val result = useCase.formatTime("14:00")
        assertEquals("14:00", result)
    }

    @Test
    fun `formatTime with invalid format falls back gracefully`() {
        val result = useCase.formatTime("invalid")
        assertEquals("invalid", result)
    }

    @Test
    fun `formatTime with empty string returns empty`() {
        val result = useCase.formatTime("")
        assertEquals("", result)
    }

    // ========== formatDayName() Tests ==========

    @Test
    fun `formatDayName with valid date returns formatted name`() {
        val result = useCase.formatDayName("2024-01-15")
        assertTrue(result.contains(","))  // Format: "Mon, Jan 15"
    }

    @Test
    fun `formatDayName with invalid date returns original`() {
        val result = useCase.formatDayName("invalid")
        assertEquals("invalid", result)
    }

    // ========== formatLastUpdated() Tests ==========

    @Test
    fun `formatLastUpdated with recent timestamp returns just now`() {
        val now = System.currentTimeMillis()
        val result = useCase.formatLastUpdated(now)
        assertEquals("Just now", result)
    }

    @Test
    fun `formatLastUpdated with 5 minutes ago returns minutes`() {
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        val result = useCase.formatLastUpdated(fiveMinutesAgo)
        assertEquals("5m ago", result)
    }

    @Test
    fun `formatLastUpdated with 2 hours ago returns hours`() {
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        val result = useCase.formatLastUpdated(twoHoursAgo)
        assertEquals("2h ago", result)
    }

    // ========== extractHourFromTime() Tests ==========

    @Test
    fun `extractHourFromTime with valid ISO timestamp returns hour`() {
        val result = useCase.extractHourFromTime("2024-01-15T14:30")
        assertEquals(14, result)
    }

    @Test
    fun `extractHourFromTime with short string returns 0`() {
        val result = useCase.extractHourFromTime("14:00")
        assertEquals(0, result)
    }

    @Test
    fun `extractHourFromTime with invalid format returns 0`() {
        val result = useCase.extractHourFromTime("invalid")
        assertEquals(0, result)
    }

    @Test
    fun `extractHourFromTime with empty string returns 0`() {
        val result = useCase.extractHourFromTime("")
        assertEquals(0, result)
    }

    // ========== isValidIsoTimestamp() Tests ==========

    @Test
    fun `isValidIsoTimestamp with valid format returns true`() {
        assertTrue(useCase.isValidIsoTimestamp("2024-01-15T14:30"))
    }

    @Test
    fun `isValidIsoTimestamp with short string returns false`() {
        assertFalse(useCase.isValidIsoTimestamp("14:30"))
    }

    @Test
    fun `isValidIsoTimestamp without T returns false`() {
        assertFalse(useCase.isValidIsoTimestamp("2024-01-15 14:30"))
    }

    @Test
    fun `isValidIsoTimestamp with empty string returns false`() {
        assertFalse(useCase.isValidIsoTimestamp(""))
    }

    // ========== extractDate() Tests ==========

    @Test
    fun `extractDate with valid ISO timestamp returns date`() {
        val result = useCase.extractDate("2024-01-15T14:30")
        assertEquals("2024-01-15", result)
    }

    @Test
    fun `extractDate with short string returns empty`() {
        val result = useCase.extractDate("14:30")
        assertEquals("", result)
    }

    @Test
    fun `extractDate with empty string returns empty`() {
        val result = useCase.extractDate("")
        assertEquals("", result)
    }

    // THIS TEST WOULD HAVE CAUGHT THE BUG!
    @Test
    fun `extractDate handles edge case of exactly 10 characters`() {
        val result = useCase.extractDate("2024-01-15")
        assertEquals("2024-01-15", result)
    }
}
