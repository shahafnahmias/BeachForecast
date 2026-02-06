package io.beachforecast.domain.calculators

import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.ActivityRecommendation
import io.beachforecast.domain.models.ActivityRecommendations
import io.beachforecast.domain.models.ConditionRating
import io.beachforecast.domain.models.CurrentConditions

/**
 * Calculates activity recommendations based on current wave and weather conditions.
 *
 * Recommendation criteria:
 * - SWIM: Calm to moderate waves (< 1m), reasonable wind (< 25 km/h)
 * - SURF: Good wave height (0.5m - 3m), not too windy (< 35 km/h)
 * - KITE: Strong wind (15-40 km/h), moderate waves
 * - SUP (Stand-up Paddle): Calm conditions, low waves (< 0.5m), light wind (< 15 km/h)
 */
object ActivityRecommendationCalculator {

    fun calculate(conditions: CurrentConditions): ActivityRecommendations {
        val waveHeight = conditions.waveHeightMeters
        val windSpeed = conditions.windSpeedKmh
        val swellHeight = conditions.swellHeightMeters

        // Calculate individual activity recommendations
        val swimRec = calculateSwimRecommendation(waveHeight, windSpeed)
        val surfRec = calculateSurfRecommendation(waveHeight, windSpeed, swellHeight)
        val kiteRec = calculateKiteRecommendation(waveHeight, windSpeed)
        val supRec = calculateSupRecommendation(waveHeight, windSpeed)

        val allRecommendations = listOf(swimRec, surfRec, kiteRec, supRec)

        // Determine primary activity (best recommended)
        val primary = determinePrimaryActivity(allRecommendations, conditions)

        // Mark the primary activity
        val finalRecommendations = allRecommendations.map { rec ->
            rec.copy(isPrimary = rec.activity == primary)
        }

        // Calculate overall condition rating (surf-focused)
        val conditionRating = calculateConditionRating(conditions)

        return ActivityRecommendations(
            recommendations = finalRecommendations,
            conditionRating = conditionRating,
            primaryActivity = primary
        )
    }

    fun calculateForSports(
        conditions: CurrentConditions,
        selectedSports: Set<Activity>
    ): ActivityRecommendations {
        val baseRecommendations = calculate(conditions)
        if (selectedSports.isEmpty()) {
            return baseRecommendations
        }

        val filteredRecommendations = baseRecommendations.filterByActivities(selectedSports)
        val conditionRating = calculateSportAwareConditionRating(
            conditions = conditions,
            selectedSports = selectedSports,
            baseRating = baseRecommendations.conditionRating,
            filteredRecommendations = filteredRecommendations
        )

        return filteredRecommendations.copy(conditionRating = conditionRating)
    }

    private fun calculateSportAwareConditionRating(
        conditions: CurrentConditions,
        selectedSports: Set<Activity>,
        baseRating: ConditionRating,
        filteredRecommendations: ActivityRecommendations
    ): ConditionRating {
        val waveHeight = conditions.waveHeightMeters
        val windSpeed = conditions.windSpeedKmh

        if (waveHeight > 3.0 || windSpeed > 50) {
            return ConditionRating.DANGEROUS
        }

        if (selectedSports.size == 1 && selectedSports.contains(Activity.SURF)) {
            return baseRating
        }

        val recommendedCount = filteredRecommendations.recommendations.count { it.isRecommended }
        val selectedCount = selectedSports.size.coerceAtLeast(1)
        val ratio = recommendedCount.toDouble() / selectedCount.toDouble()

        return when {
            ratio >= 1.0 -> ConditionRating.EPIC
            ratio >= 0.75 -> ConditionRating.EXCELLENT
            ratio >= 0.5 -> ConditionRating.GOOD
            ratio >= 0.25 -> ConditionRating.FAIR
            else -> ConditionRating.POOR
        }
    }

    private fun calculateSwimRecommendation(
        waveHeight: Double,
        windSpeed: Double
    ): ActivityRecommendation {
        val isRecommended = waveHeight < 1.0 && windSpeed < 25.0

        val reason = when {
            waveHeight >= 1.5 -> "Waves too high"
            windSpeed >= 30 -> "Too windy"
            waveHeight >= 1.0 -> "Moderate waves, swim with caution"
            else -> "Good conditions for swimming"
        }

        return ActivityRecommendation(
            activity = Activity.SWIM,
            isRecommended = isRecommended,
            reason = reason
        )
    }

    private fun calculateSurfRecommendation(
        waveHeight: Double,
        windSpeed: Double,
        swellHeight: Double
    ): ActivityRecommendation {
        // Surfing needs waves but not too much wind
        val hasGoodWaves = waveHeight >= 0.5 || swellHeight >= 0.5
        val notTooFlat = waveHeight >= 0.3
        val notTooWindy = windSpeed < 35.0
        val notTooBig = waveHeight < 3.0

        val isRecommended = hasGoodWaves && notTooWindy && notTooBig

        val reason = when {
            !notTooFlat -> "Too flat for surfing"
            !notTooBig -> "Dangerous conditions"
            !notTooWindy -> "Too windy"
            waveHeight >= 1.5 -> "Great surf conditions!"
            waveHeight >= 1.0 -> "Good waves"
            waveHeight >= 0.5 -> "Small but fun waves"
            else -> "Minimal waves"
        }

        return ActivityRecommendation(
            activity = Activity.SURF,
            isRecommended = isRecommended,
            reason = reason
        )
    }

    private fun calculateKiteRecommendation(
        waveHeight: Double,
        windSpeed: Double
    ): ActivityRecommendation {
        // Kitesurfing needs wind (15-40 km/h)
        val hasEnoughWind = windSpeed >= 15.0
        val notTooWindy = windSpeed < 45.0
        val wavesNotTooBig = waveHeight < 2.5

        val isRecommended = hasEnoughWind && notTooWindy && wavesNotTooBig

        val reason = when {
            !hasEnoughWind -> "Not enough wind"
            !notTooWindy -> "Wind too strong"
            !wavesNotTooBig -> "Waves too big"
            windSpeed >= 25 -> "Perfect kite conditions!"
            else -> "Good for kiting"
        }

        return ActivityRecommendation(
            activity = Activity.KITE,
            isRecommended = isRecommended,
            reason = reason
        )
    }

    private fun calculateSupRecommendation(
        waveHeight: Double,
        windSpeed: Double
    ): ActivityRecommendation {
        // SUP needs calm conditions
        val calmWaves = waveHeight < 0.5
        val lightWind = windSpeed < 15.0

        val isRecommended = calmWaves && lightWind

        val reason = when {
            waveHeight >= 1.0 -> "Waves too high for paddleboarding"
            windSpeed >= 20 -> "Too windy for paddleboarding"
            waveHeight >= 0.5 -> "Choppy conditions"
            else -> "Perfect for paddleboarding"
        }

        return ActivityRecommendation(
            activity = Activity.SUP,
            isRecommended = isRecommended,
            reason = reason
        )
    }

    private fun determinePrimaryActivity(
        recommendations: List<ActivityRecommendation>,
        conditions: CurrentConditions
    ): Activity? {
        val recommendedActivities = recommendations.filter { it.isRecommended }

        if (recommendedActivities.isEmpty()) {
            return null
        }

        // Priority logic based on conditions
        val waveHeight = conditions.waveHeightMeters
        val windSpeed = conditions.windSpeedKmh

        return when {
            // Great surf conditions - prioritize surfing
            waveHeight >= 1.0 && windSpeed < 30 &&
                    recommendedActivities.any { it.activity == Activity.SURF } -> Activity.SURF

            // Strong wind - prioritize kiting
            windSpeed >= 20 && windSpeed < 40 &&
                    recommendedActivities.any { it.activity == Activity.KITE } -> Activity.KITE

            // Calm flat day - prioritize SAP
            waveHeight < 0.3 && windSpeed < 12 &&
                    recommendedActivities.any { it.activity == Activity.SUP } -> Activity.SUP

            // Default to swimming if recommended
            recommendedActivities.any { it.activity == Activity.SWIM } -> Activity.SWIM

            // Otherwise pick first recommended
            else -> recommendedActivities.firstOrNull()?.activity
        }
    }

    private fun calculateConditionRating(conditions: CurrentConditions): ConditionRating {
        val waveHeight = conditions.waveHeightMeters
        val windSpeed = conditions.windSpeedKmh

        // Check for dangerous conditions first
        if (waveHeight > 3.0 || windSpeed > 50) {
            return ConditionRating.DANGEROUS
        }

        // Surf-focused rating
        return when {
            waveHeight >= 2.0 && windSpeed < 25 -> ConditionRating.EPIC
            waveHeight >= 1.5 && windSpeed < 30 -> ConditionRating.EXCELLENT
            waveHeight >= 1.0 && windSpeed < 30 -> ConditionRating.GOOD
            waveHeight >= 0.5 -> ConditionRating.FAIR
            waveHeight >= 0.2 -> ConditionRating.POOR
            else -> ConditionRating.FLAT
        }
    }
}
