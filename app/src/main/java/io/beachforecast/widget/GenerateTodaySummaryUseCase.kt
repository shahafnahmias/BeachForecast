package io.beachforecast.widget

import android.content.Context
import io.beachforecast.R
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.ConditionRating
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.domain.models.WeatherMetric

/**
 * Generates a natural language summary of today's forecast
 * Includes wave trends, swell conditions, and weather summary
 *
 * Lives in widget package because it requires Context for string resources
 * and is only used by WidgetUpdateWorker.
 */
object GenerateTodaySummaryUseCase {

    fun execute(
        context: Context,
        currentConditions: CurrentConditions,
        todayRemaining: List<HourlyWaveForecast>,
        selectedMetrics: Set<WeatherMetric> = WeatherMetric.getDefaults(),
        selectedSports: Set<Activity> = Activity.getDefaults()
    ): String {
        val sportSummary = generateSportSummary(context, currentConditions, selectedSports)
        val waveTrend = generateWaveTrend(context, currentConditions.waveCategory, todayRemaining)
        val swellDescription = if (selectedMetrics.contains(WeatherMetric.SWELL)) {
            generateSwellDescription(context, currentConditions.swellHeightMeters, todayRemaining)
        } else {
            ""
        }
        val weatherDescription = generateWeatherDescription(context, currentConditions, todayRemaining, selectedMetrics)

        return listOf(sportSummary, waveTrend, swellDescription, weatherDescription)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    private fun generateSportSummary(
        context: Context,
        currentConditions: CurrentConditions,
        selectedSports: Set<Activity>
    ): String {
        if (selectedSports.isEmpty()) {
            return ""
        }

        val recommendations = ActivityRecommendationCalculator
            .calculateForSports(currentConditions, selectedSports)

        val primaryRecommendation = recommendations.primaryRecommendation
        val primaryActivity = primaryRecommendation?.activity ?: selectedSports.firstOrNull() ?: return ""
        val isRecommended = primaryRecommendation?.isRecommended == true
        val isGreat = isRecommended && recommendations.conditionRating in setOf(
            ConditionRating.EPIC,
            ConditionRating.EXCELLENT
        )

        val stringRes = when (primaryActivity) {
            Activity.SURF -> when {
                !isRecommended -> R.string.summary_not_ideal_for_surfing
                isGreat -> R.string.summary_great_for_surfing
                else -> R.string.summary_good_for_surfing
            }
            Activity.SWIM -> when {
                !isRecommended -> R.string.summary_not_ideal_for_swimming
                isGreat -> R.string.summary_great_for_swimming
                else -> R.string.summary_good_for_swimming
            }
            Activity.KITE -> when {
                !isRecommended -> R.string.summary_not_ideal_for_kiting
                isGreat -> R.string.summary_great_for_kiting
                else -> R.string.summary_good_for_kiting
            }
            Activity.SUP -> when {
                !isRecommended -> R.string.summary_not_ideal_for_sup
                isGreat -> R.string.summary_great_for_sup
                else -> R.string.summary_good_for_sup
            }
        }

        return context.getString(stringRes)
    }

    private fun generateWaveTrend(context: Context, currentCategory: WaveCategory, forecast: List<HourlyWaveForecast>): String {
        if (forecast.isEmpty()) {
            return context.getString(R.string.summary_waves_remain, context.getString(currentCategory.nameRes).lowercase())
        }

        val categories = forecast.map { it.waveCategory }.distinct()

        // All same category
        if (categories.size == 1 && categories.first() == currentCategory) {
            return context.getString(R.string.summary_waves_remain_throughout, context.getString(currentCategory.nameRes).lowercase())
        }

        // Check trend direction
        val firstHalfAvg = forecast.take(forecast.size / 2).map { it.waveHeightMeters }.average()
        val secondHalfAvg = forecast.takeLast(forecast.size / 2).map { it.waveHeightMeters }.average()
        val currentHeight = currentCategory.minHeight

        return when {
            // Rising trend
            secondHalfAvg > firstHalfAvg && secondHalfAvg > currentHeight -> {
                val peakCategory = forecast.maxByOrNull { it.waveHeightMeters }?.waveCategory ?: currentCategory
                context.getString(
                    R.string.summary_waves_build,
                    context.getString(currentCategory.nameRes).lowercase(),
                    context.getString(peakCategory.nameRes).lowercase()
                )
            }
            // Falling trend
            secondHalfAvg < firstHalfAvg && secondHalfAvg < currentHeight -> {
                val endCategory = forecast.lastOrNull()?.waveCategory ?: currentCategory
                context.getString(
                    R.string.summary_waves_drop,
                    context.getString(currentCategory.nameRes).lowercase(),
                    context.getString(endCategory.nameRes).lowercase()
                )
            }
            // Fluctuating
            categories.size > 2 -> {
                val min = forecast.minByOrNull { it.waveHeightMeters }?.waveCategory ?: currentCategory
                val max = forecast.maxByOrNull { it.waveHeightMeters }?.waveCategory ?: currentCategory
                context.getString(
                    R.string.summary_waves_fluctuate,
                    context.getString(min.nameRes).lowercase(),
                    context.getString(max.nameRes).lowercase()
                )
            }
            // Relatively stable
            else -> {
                context.getString(R.string.summary_waves_minor_variations, context.getString(currentCategory.nameRes).lowercase())
            }
        }
    }

    private fun generateSwellDescription(context: Context, currentSwellHeight: Double, forecast: List<HourlyWaveForecast>): String {
        val avgSwellHeight = if (forecast.isNotEmpty()) {
            forecast.map { it.swellHeightMeters }.average()
        } else {
            currentSwellHeight
        }

        return when {
            avgSwellHeight < 0.3 -> context.getString(R.string.summary_minimal_swell)
            avgSwellHeight < 0.8 -> context.getString(R.string.summary_light_swell)
            avgSwellHeight < 1.5 -> context.getString(R.string.summary_moderate_swell)
            avgSwellHeight < 2.5 -> context.getString(R.string.summary_good_swell)
            else -> context.getString(R.string.summary_strong_swell)
        }
    }

    private fun generateWeatherDescription(
        context: Context,
        currentConditions: CurrentConditions,
        forecast: List<HourlyWaveForecast>,
        selectedMetrics: Set<WeatherMetric>
    ): String {
        val parts = mutableListOf<String>()

        // Cloud cover summary - only if selected
        if (selectedMetrics.contains(WeatherMetric.CLOUD_COVER)) {
            val avgCloudCover = if (forecast.isNotEmpty()) {
                forecast.map { it.cloudCover.minCover }.average().toInt()
            } else {
                currentConditions.cloudCover.minCover
            }

            val cloudDescription = when {
                avgCloudCover < 20 -> context.getString(R.string.summary_sunny)
                avgCloudCover < 40 -> context.getString(R.string.summary_mostly_sunny)
                avgCloudCover < 60 -> context.getString(R.string.summary_partly_cloudy)
                avgCloudCover < 80 -> context.getString(R.string.summary_mostly_cloudy)
                else -> context.getString(R.string.summary_overcast)
            }
            parts.add(cloudDescription)
        }

        // UV index consideration - only if selected
        if (selectedMetrics.contains(WeatherMetric.UV_INDEX)) {
            val avgUvIndex = if (forecast.isNotEmpty()) {
                forecast.map { it.uvIndex }.average()
            } else {
                currentConditions.uvIndex
            }

            val uvWarning = when {
                avgUvIndex >= 8 -> context.getString(R.string.summary_with_very_high_uv)
                avgUvIndex >= 6 -> context.getString(R.string.summary_with_high_uv)
                avgUvIndex >= 3 -> ""
                else -> ""
            }
            if (uvWarning.isNotBlank()) {
                parts.add(uvWarning)
            }
        }

        // Wind consideration - only if selected
        if (selectedMetrics.contains(WeatherMetric.WIND)) {
            val avgWindSpeed = if (forecast.isNotEmpty()) {
                forecast.map { it.windSpeedKmh }.average()
            } else {
                currentConditions.windSpeedKmh
            }

            val windDescription = when {
                avgWindSpeed > 40 -> context.getString(R.string.summary_strong_winds)
                avgWindSpeed > 25 -> context.getString(R.string.summary_moderate_winds)
                avgWindSpeed > 15 -> ""
                else -> context.getString(R.string.summary_light_winds)
            }
            if (windDescription.isNotBlank()) {
                parts.add(windDescription)
            }
        }

        // Temperature - only if selected
        if (selectedMetrics.contains(WeatherMetric.TEMPERATURE)) {
            val avgTemp = if (forecast.isNotEmpty()) {
                forecast.map { it.temperatureCelsius }.average()
            } else {
                currentConditions.temperatureCelsius
            }

            val tempDescription = when {
                avgTemp > 30 -> context.getString(R.string.summary_hot_conditions)
                avgTemp > 25 -> context.getString(R.string.summary_warm_conditions)
                avgTemp < 10 -> context.getString(R.string.summary_cold_conditions)
                avgTemp < 15 -> context.getString(R.string.summary_cool_conditions)
                else -> ""
            }
            if (tempDescription.isNotBlank()) {
                parts.add(tempDescription)
            }
        }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ") + "."
        } else {
            ""
        }
    }
}
