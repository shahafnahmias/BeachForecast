package io.beachforecast.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ConditionRatingWidgetColorTest {

    @Test
    fun `getWidgetColor returns distinct color for each rating`() {
        val colors = ConditionRating.entries.map { it.getWidgetColor() }
        assertEquals("All 7 ratings should produce unique colors", 7, colors.distinct().size)
    }

    @Test
    fun `EPIC returns bright green`() {
        assertEquals(0xFF00E676.toInt(), ConditionRating.EPIC.getWidgetColor())
    }

    @Test
    fun `EXCELLENT returns green`() {
        assertEquals(0xFF4CAF50.toInt(), ConditionRating.EXCELLENT.getWidgetColor())
    }

    @Test
    fun `GOOD returns lighter green`() {
        assertEquals(0xFF66BB6A.toInt(), ConditionRating.GOOD.getWidgetColor())
    }

    @Test
    fun `FAIR returns amber`() {
        assertEquals(0xFFFFB300.toInt(), ConditionRating.FAIR.getWidgetColor())
    }

    @Test
    fun `POOR returns orange`() {
        assertEquals(0xFFFF9800.toInt(), ConditionRating.POOR.getWidgetColor())
    }

    @Test
    fun `FLAT returns blue-grey`() {
        assertEquals(0xFF78909C.toInt(), ConditionRating.FLAT.getWidgetColor())
    }

    @Test
    fun `DANGEROUS returns red`() {
        assertEquals(0xFFF44336.toInt(), ConditionRating.DANGEROUS.getWidgetColor())
    }

    @Test
    fun `all colors have full alpha`() {
        for (rating in ConditionRating.entries) {
            val color = rating.getWidgetColor()
            val alpha = (color.toLong() and 0xFF000000) shr 24
            assertEquals("${rating.name} should have full alpha (255)", 255L, alpha)
        }
    }
}
