package io.beachforecast.domain.analyzers

import io.beachforecast.domain.calculators.WaveCategoryCalculator
import io.beachforecast.domain.models.TimeOfDay

/**
 * Business logic for analyzing and grouping forecast data
 */
object ForecastAnalyzer {

    /**
     * Result of today's forecast analysis
     */
    data class TodayAnalysis(
        val isAllDayConstant: Boolean,
        val forecastCells: List<ForecastCell>
    )

    /**
     * Represents a cell in the forecast display
     */
    data class ForecastCell(
        val timeLabel: String,
        val category: String,
        val isNow: Boolean
    )

    /**
     * Analyze today's remaining forecast
     * Determines if it should show "All Day" or breakdown
     */
    fun analyzeToday(
        currentCategory: String,
        todayRemaining: List<Pair<String, String>>  // List of (time, category)
    ): TodayAnalysis {
        if (todayRemaining.isEmpty()) {
            return TodayAnalysis(isAllDayConstant = true, forecastCells = emptyList())
        }

        val allSame = WaveCategoryCalculator.areAllSameCategory(
            currentCategory,
            todayRemaining.map { it.second }
        )

        return if (allSame) {
            TodayAnalysis(isAllDayConstant = true, forecastCells = emptyList())
        } else {
            val cells = groupByChanges(todayRemaining)
            TodayAnalysis(isAllDayConstant = false, forecastCells = cells)
        }
    }

    /**
     * Group forecast by category changes
     */
    fun groupByChanges(forecast: List<Pair<String, String>>): List<ForecastCell> {
        if (forecast.isEmpty()) return emptyList()

        val cells = mutableListOf<ForecastCell>()
        var currentCategory = forecast[0].second
        var currentTimes = mutableListOf(forecast[0].first)

        for (i in 1 until forecast.size) {
            val (time, category) = forecast[i]

            if (WaveCategoryCalculator.isSameCategory(category, currentCategory)) {
                currentTimes.add(time)
            } else {
                // Add cell for completed group
                val isFirstGroup = cells.isEmpty()
                val timeLabel = formatTimeRange(
                    startTime = currentTimes.first(),
                    endTime = currentTimes.last(),
                    isFirst = isFirstGroup
                )
                cells.add(ForecastCell(timeLabel, currentCategory, isFirstGroup))

                currentCategory = category
                currentTimes = mutableListOf(time)
            }
        }

        // Add last group
        val isFirstGroup = cells.isEmpty()
        val timeLabel = formatTimeRange(
            startTime = currentTimes.first(),
            endTime = currentTimes.last(),
            isFirst = isFirstGroup
        )
        cells.add(ForecastCell(timeLabel, currentCategory, isFirstGroup))

        return cells
    }

    /**
     * Format time range for display
     */
    fun formatTimeRange(startTime: String, endTime: String, isFirst: Boolean): String {
        if (!isFirst) {
            return if (startTime == endTime) startTime else "$startTime-$endTime"
        }

        // For the first group (NOW), create a descriptive label
        val startHour = startTime.split(":")[0].toIntOrNull() ?: return "NOW"
        val endHour = endTime.split(":")[0].toIntOrNull() ?: return "NOW"

        // If it spans most of the day (6+ hours), use time-of-day labels
        if (endHour - startHour >= 6) {
            val endLabel = TimeOfDay.fromHour(endHour).displayName
            return "NOW until $endLabel"
        }

        // If it's just 1-2 hours
        if (endHour - startHour <= 2) {
            return "NOW"
        }

        // Otherwise show time range
        return "NOW-$endTime"
    }
}
