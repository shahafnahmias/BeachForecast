package io.beachforecast.domain.models

import org.junit.Assert.*
import org.junit.Test

class UnitSystemTest {

    @Test
    fun `fromBoolean returns METRIC for true`() {
        assertEquals(UnitSystem.METRIC, UnitSystem.fromBoolean(true))
    }

    @Test
    fun `fromBoolean returns IMPERIAL for false`() {
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.fromBoolean(false))
    }

    @Test
    fun `toBoolean returns true for METRIC`() {
        assertTrue(UnitSystem.METRIC.toBoolean())
    }

    @Test
    fun `toBoolean returns false for IMPERIAL`() {
        assertFalse(UnitSystem.IMPERIAL.toBoolean())
    }

    @Test
    fun `convertTemperature from Celsius to Fahrenheit`() {
        val celsius = 20.0
        val fahrenheit = UnitConverter.convertTemperature(celsius, UnitSystem.IMPERIAL)
        assertEquals(68.0, fahrenheit, 0.1)
    }

    @Test
    fun `convertTemperature returns same value for METRIC`() {
        val celsius = 20.0
        val result = UnitConverter.convertTemperature(celsius, UnitSystem.METRIC)
        assertEquals(celsius, result, 0.0)
    }

    @Test
    fun `convertTemperature handles freezing point correctly`() {
        val celsius = 0.0
        val fahrenheit = UnitConverter.convertTemperature(celsius, UnitSystem.IMPERIAL)
        assertEquals(32.0, fahrenheit, 0.1)
    }

    @Test
    fun `convertWaveHeight from meters to feet`() {
        val meters = 1.0
        val feet = UnitConverter.convertWaveHeight(meters, UnitSystem.IMPERIAL)
        assertEquals(3.28, feet, 0.01)
    }

    @Test
    fun `convertWaveHeight returns cm for METRIC`() {
        val meters = 1.5
        val cm = UnitConverter.convertWaveHeight(meters, UnitSystem.METRIC)
        assertEquals(150.0, cm, 0.1)
    }

    @Test
    fun `convertWindSpeed from kmh to mph`() {
        val kmh = 100.0
        val mph = UnitConverter.convertWindSpeed(kmh, UnitSystem.IMPERIAL)
        assertEquals(62.14, mph, 0.01)
    }

    @Test
    fun `convertWindSpeed returns same value for METRIC`() {
        val kmh = 50.0
        val result = UnitConverter.convertWindSpeed(kmh, UnitSystem.METRIC)
        assertEquals(kmh, result, 0.0)
    }

    @Test
    fun `getTemperatureUnit returns Celsius symbol for METRIC`() {
        assertEquals("°C", UnitConverter.getTemperatureUnit(UnitSystem.METRIC))
    }

    @Test
    fun `getTemperatureUnit returns Fahrenheit symbol for IMPERIAL`() {
        assertEquals("°F", UnitConverter.getTemperatureUnit(UnitSystem.IMPERIAL))
    }

    @Test
    fun `getWaveHeightUnit returns cm for METRIC`() {
        assertEquals("cm", UnitConverter.getWaveHeightUnit(UnitSystem.METRIC))
    }

    @Test
    fun `getWaveHeightUnit returns ft for IMPERIAL`() {
        assertEquals("ft", UnitConverter.getWaveHeightUnit(UnitSystem.IMPERIAL))
    }

    @Test
    fun `getWindSpeedUnit returns kmh for METRIC`() {
        assertEquals("km/h", UnitConverter.getWindSpeedUnit(UnitSystem.METRIC))
    }

    @Test
    fun `getWindSpeedUnit returns mph for IMPERIAL`() {
        assertEquals("mph", UnitConverter.getWindSpeedUnit(UnitSystem.IMPERIAL))
    }
}
