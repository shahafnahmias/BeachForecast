package io.beachforecast.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRecommendationsTest {

    @Test
    fun `filterByActivities returns only selected sports`() {
        val recommendations = ActivityRecommendations(
            recommendations = listOf(
                ActivityRecommendation(Activity.SWIM, isRecommended = true, isPrimary = false),
                ActivityRecommendation(Activity.SURF, isRecommended = true, isPrimary = true),
                ActivityRecommendation(Activity.KITE, isRecommended = false, isPrimary = false),
                ActivityRecommendation(Activity.SUP, isRecommended = false, isPrimary = false)
            ),
            conditionRating = ConditionRating.GOOD,
            primaryActivity = Activity.SURF
        )

        val filtered = recommendations.filterByActivities(setOf(Activity.SWIM, Activity.KITE))

        val activities = filtered.recommendations.map { it.activity }.toSet()
        assertEquals(setOf(Activity.SWIM, Activity.KITE), activities)
    }

    @Test
    fun `filterByActivities recalculates primary when original primary not selected`() {
        val recommendations = ActivityRecommendations(
            recommendations = listOf(
                ActivityRecommendation(Activity.SWIM, isRecommended = true, isPrimary = false),
                ActivityRecommendation(Activity.SURF, isRecommended = true, isPrimary = true),
                ActivityRecommendation(Activity.KITE, isRecommended = false, isPrimary = false),
                ActivityRecommendation(Activity.SUP, isRecommended = false, isPrimary = false)
            ),
            conditionRating = ConditionRating.GOOD,
            primaryActivity = Activity.SURF
        )

        val filtered = recommendations.filterByActivities(setOf(Activity.SWIM, Activity.KITE))

        assertEquals(Activity.SWIM, filtered.primaryActivity)
        assertTrue(filtered.recommendations.first { it.activity == Activity.SWIM }.isPrimary)
    }

    @Test
    fun `getDefaults returns all activities`() {
        val defaults = Activity.getDefaults()

        assertEquals(setOf(Activity.SWIM, Activity.SURF, Activity.KITE, Activity.SUP), defaults)
    }
}
