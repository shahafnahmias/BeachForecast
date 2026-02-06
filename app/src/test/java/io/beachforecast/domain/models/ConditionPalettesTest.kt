package io.beachforecast.domain.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for ConditionPalettes
 * Note: Color value tests require Android framework (instrumented tests)
 * These tests focus on the label/classification logic
 */
class ConditionPalettesTest {

    @Test
    fun `getWindLabel returns correct labels for wind speeds`() {
        assertEquals("Calm", ConditionPalettes.getWindLabel(5.0))
        assertEquals("Light", ConditionPalettes.getWindLabel(15.0))
        assertEquals("Moderate", ConditionPalettes.getWindLabel(25.0))
        assertEquals("Fresh", ConditionPalettes.getWindLabel(35.0))
        assertEquals("Strong", ConditionPalettes.getWindLabel(45.0))
        assertEquals("Very Strong", ConditionPalettes.getWindLabel(55.0))
    }

    @Test
    fun `getWindLabel boundary conditions`() {
        assertEquals("Calm", ConditionPalettes.getWindLabel(9.9))
        assertEquals("Light", ConditionPalettes.getWindLabel(10.0))
        assertEquals("Moderate", ConditionPalettes.getWindLabel(29.9))
        assertEquals("Fresh", ConditionPalettes.getWindLabel(30.0))
    }

    @Test
    fun `getSwellLabel returns correct labels for swell heights`() {
        assertEquals("Tiny", ConditionPalettes.getSwellLabel(0.3))
        assertEquals("Small", ConditionPalettes.getSwellLabel(0.7))
        assertEquals("Medium", ConditionPalettes.getSwellLabel(1.2))
        assertEquals("Large", ConditionPalettes.getSwellLabel(1.7))
        assertEquals("Very Large", ConditionPalettes.getSwellLabel(2.5))
        assertEquals("Huge", ConditionPalettes.getSwellLabel(3.5))
    }

    @Test
    fun `getSwellLabel boundary conditions`() {
        assertEquals("Tiny", ConditionPalettes.getSwellLabel(0.49))
        assertEquals("Small", ConditionPalettes.getSwellLabel(0.5))
        assertEquals("Medium", ConditionPalettes.getSwellLabel(1.0))
        assertEquals("Large", ConditionPalettes.getSwellLabel(1.5))
    }

    @Test
    fun `getUvLabel returns correct labels for UV index`() {
        assertEquals("Low", ConditionPalettes.getUvLabel(2.0))
        assertEquals("Moderate", ConditionPalettes.getUvLabel(4.0))
        assertEquals("High", ConditionPalettes.getUvLabel(7.0))
        assertEquals("Very High", ConditionPalettes.getUvLabel(9.0))
        assertEquals("Extreme", ConditionPalettes.getUvLabel(12.0))
    }

    @Test
    fun `getUvLabel boundary conditions`() {
        assertEquals("Low", ConditionPalettes.getUvLabel(2.9))
        assertEquals("Moderate", ConditionPalettes.getUvLabel(3.0))
        assertEquals("High", ConditionPalettes.getUvLabel(6.0))
        assertEquals("Very High", ConditionPalettes.getUvLabel(8.0))
        assertEquals("Extreme", ConditionPalettes.getUvLabel(11.0))
    }

    @Test
    fun `getTempLabel returns correct labels for temperatures`() {
        assertEquals("Cold", ConditionPalettes.getTempLabel(5.0))
        assertEquals("Cool", ConditionPalettes.getTempLabel(12.0))
        assertEquals("Mild", ConditionPalettes.getTempLabel(17.0))
        assertEquals("Pleasant", ConditionPalettes.getTempLabel(22.0))
        assertEquals("Warm", ConditionPalettes.getTempLabel(27.0))
        assertEquals("Hot", ConditionPalettes.getTempLabel(32.0))
        assertEquals("Very Hot", ConditionPalettes.getTempLabel(40.0))
    }

    @Test
    fun `getTempLabel boundary conditions`() {
        assertEquals("Cold", ConditionPalettes.getTempLabel(9.9))
        assertEquals("Cool", ConditionPalettes.getTempLabel(10.0))
        assertEquals("Mild", ConditionPalettes.getTempLabel(15.0))
        assertEquals("Pleasant", ConditionPalettes.getTempLabel(20.0))
        assertEquals("Warm", ConditionPalettes.getTempLabel(25.0))
    }

}
