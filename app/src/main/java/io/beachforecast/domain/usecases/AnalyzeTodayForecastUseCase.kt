package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory

/**
 * Analyzes today's forecast to determine if conditions are constant ("All Day")
 * Extracted from WeatherViewModel for testability
 */
class AnalyzeTodayForecastUseCase {

    /**
     * Check if wave conditions remain constant throughout the day
     *
     * @param currentCategory Current wave category
     * @param todayRemaining List of hourly forecasts for rest of today
     * @return true if all hours match the current category
     */
    fun isAllDayConstant(
        currentCategory: WaveCategory,
        todayRemaining: List<HourlyWaveForecast>
    ): Boolean {
        if (todayRemaining.isEmpty()) return true

        return todayRemaining.all { hourly ->
            hourly.waveCategory == currentCategory
        }
    }

    /**
     * Check if two wave categories are the same (by enum, not string comparison)
     */
    fun areSameCategory(category1: WaveCategory, category2: WaveCategory): Boolean {
        return category1 == category2
    }
}
