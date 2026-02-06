package io.beachforecast.domain.calculators

import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.ActivityRecommendations
import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.presentation.models.PeriodConditionsUiData

/**
 * Calculates best day per sport from weekly forecast data.
 * Extracted from ForecastScreen for reuse on HomeScreen.
 */
object BestDayCalculator {

    data class DayAverageConditions(
        val waveCategory: WaveCategory,
        val waveHeight: Double,
        val temperature: Double,
        val cloudCover: CloudCoverLevel,
        val windSpeed: Double,
        val windDirection: Int,
        val uvIndex: Double,
        val swellHeight: Double,
        val swellDirection: Int,
        val swellPeriod: Double,
        val wavePeriod: Double,
        val seaSurfaceTemperature: Double
    )

    fun calculateDayAverageConditions(day: DayForecastUiData): DayAverageConditions {
        val periods = listOf(
            day.morningConditions,
            day.afternoonConditions,
            day.eveningConditions
        )

        val validPeriods = periods.filter { it.waveHeight > 0.0 || it.temperature > 0.0 }
        if (validPeriods.isEmpty()) {
            return DayAverageConditions(
                waveCategory = WaveCategory.FLAT,
                waveHeight = 0.0,
                temperature = 0.0,
                cloudCover = CloudCoverLevel.CLEAR,
                windSpeed = 0.0,
                windDirection = 0,
                uvIndex = 0.0,
                swellHeight = 0.0,
                swellDirection = 0,
                swellPeriod = 0.0,
                wavePeriod = 0.0,
                seaSurfaceTemperature = 0.0
            )
        }

        fun avg(selector: (PeriodConditionsUiData) -> Double): Double =
            validPeriods.map(selector).average()

        val avgWaveHeight = avg { it.waveHeight }

        return DayAverageConditions(
            waveCategory = WaveCategory.fromHeight(avgWaveHeight),
            waveHeight = avgWaveHeight,
            temperature = avg { it.temperature },
            cloudCover = validPeriods
                .map { it.cloudCover.minCover }
                .average()
                .toInt()
                .let { CloudCoverLevel.fromPercentage(it) },
            windSpeed = avg { it.windSpeed },
            windDirection = validPeriods
                .map { it.windDirection }
                .average()
                .toInt(),
            uvIndex = avg { it.uvIndex },
            swellHeight = avg { it.swellHeight },
            swellDirection = validPeriods
                .map { it.swellDirection }
                .average()
                .toInt(),
            swellPeriod = avg { it.swellPeriod },
            wavePeriod = avg { it.wavePeriod },
            seaSurfaceTemperature = avg { it.seaSurfaceTemperature }
        )
    }

    fun calculateDayActivityRecommendations(
        day: DayForecastUiData,
        selectedSports: Set<Activity>
    ): ActivityRecommendations {
        val avg = calculateDayAverageConditions(day)
        val conditions = toCurrentConditions(avg)
        return ActivityRecommendationCalculator.calculate(conditions)
            .filterByActivities(selectedSports)
    }

    /**
     * Finds the best day of the week for each selected sport.
     * Returns a map of Activity -> best day index (0-6).
     * Only includes sports that have at least one recommended day.
     */
    fun findBestDayPerSport(
        weekForecast: List<DayForecastUiData>,
        selectedSports: Set<Activity>
    ): Map<Activity, Int> {
        if (weekForecast.isEmpty() || selectedSports.isEmpty()) return emptyMap()

        val result = mutableMapOf<Activity, Int>()

        for (sport in selectedSports) {
            var bestIndex = -1
            var bestScore = -Double.MAX_VALUE

            weekForecast.forEachIndexed { index, day ->
                val avg = calculateDayAverageConditions(day)
                val conditions = toCurrentConditions(avg)

                val recommendations = ActivityRecommendationCalculator.calculate(conditions)
                val rec = recommendations.recommendations.find { it.activity == sport } ?: return@forEachIndexed
                if (!rec.isRecommended) return@forEachIndexed

                val score = when (sport) {
                    Activity.SURF -> avg.waveHeight
                    Activity.KITE -> avg.windSpeed
                    Activity.SWIM -> -avg.waveHeight
                    Activity.SUP -> -(avg.waveHeight + avg.windSpeed)
                }

                if (score > bestScore) {
                    bestScore = score
                    bestIndex = index
                }
            }

            if (bestIndex >= 0) {
                result[sport] = bestIndex
            }
        }

        return result
    }

    private fun toCurrentConditions(avg: DayAverageConditions): CurrentConditions {
        return CurrentConditions(
            waveCategory = avg.waveCategory,
            waveHeightMeters = avg.waveHeight,
            temperatureCelsius = avg.temperature,
            cloudCover = avg.cloudCover,
            windSpeedKmh = avg.windSpeed,
            windDirectionDegrees = avg.windDirection,
            waveDirectionDegrees = 0,
            wavePeriodSeconds = avg.wavePeriod,
            swellHeightMeters = avg.swellHeight,
            swellDirectionDegrees = avg.swellDirection,
            swellPeriodSeconds = avg.swellPeriod,
            seaSurfaceTemperatureCelsius = avg.seaSurfaceTemperature,
            uvIndex = avg.uvIndex
        )
    }
}
