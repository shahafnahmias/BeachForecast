package io.beachforecast.data.preferences

import io.beachforecast.domain.models.UnitSystem
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserPreferences
 * Note: These tests verify the business logic of UnitSystem conversions
 * Full DataStore integration tests require instrumented tests (androidTest)
 */
class UserPreferencesTest {

    @Test
    fun `UnitSystem fromBoolean with true returns METRIC`() {
        val result = UnitSystem.fromBoolean(true)
        assertEquals(UnitSystem.METRIC, result)
    }

    @Test
    fun `UnitSystem fromBoolean with false returns IMPERIAL`() {
        val result = UnitSystem.fromBoolean(false)
        assertEquals(UnitSystem.IMPERIAL, result)
    }

    @Test
    fun `UnitSystem METRIC toBoolean returns true`() {
        val result = UnitSystem.METRIC.toBoolean()
        assertTrue(result)
    }

    @Test
    fun `UnitSystem IMPERIAL toBoolean returns false`() {
        val result = UnitSystem.IMPERIAL.toBoolean()
        assertFalse(result)
    }

    @Test
    fun `UnitSystem roundtrip METRIC`() {
        val original = UnitSystem.METRIC
        val asBoolean = original.toBoolean()
        val restored = UnitSystem.fromBoolean(asBoolean)
        assertEquals(original, restored)
    }

    @Test
    fun `UnitSystem roundtrip IMPERIAL`() {
        val original = UnitSystem.IMPERIAL
        val asBoolean = original.toBoolean()
        val restored = UnitSystem.fromBoolean(asBoolean)
        assertEquals(original, restored)
    }
}
