package io.beachforecast.domain.usecases

import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.presentation.models.PeriodConditionsUiData
import io.beachforecast.presentation.models.ThreeHourPeriodUiData

/**
 * Groups hourly forecast data by days and time periods
 * Extracted from WeatherViewModel for testability
 *
 * THIS IS WHERE THE BUG WAS - now fully testable with edge cases
 */
class GroupForecastByDaysUseCase(
    private val formatUseCase: FormatWeatherForDisplayUseCase = FormatWeatherForDisplayUseCase,
    private val unitSystem: UnitSystem = UnitSystem.METRIC
) {

    /**
     * Group hourly forecast data into daily forecasts with morning/afternoon/evening periods
     *
     * @param hourlyData List of hourly forecasts with ISO timestamps
     * @return List of up to 7 days with period breakdowns
     */
    fun execute(hourlyData: List<HourlyWaveForecast>): List<DayForecastUiData> {
        if (hourlyData.isEmpty()) return emptyList()

        // Group by date - validate format first to prevent crashes
        val grouped = hourlyData
            .filter { formatUseCase.isValidIsoTimestamp(it.time) }
            .groupBy { hourly -> formatUseCase.extractDate(hourly.time) }
            .filterKeys { it.isNotEmpty() }

        return grouped.map { (date, hours) ->
            val morning = hours.filter {
                formatUseCase.extractHourFromTime(it.time) in 5..11
            }
            val afternoon = hours.filter {
                formatUseCase.extractHourFromTime(it.time) in 12..16
            }
            val evening = hours.filter {
                formatUseCase.extractHourFromTime(it.time) in 17..20
            }

            DayForecastUiData(
                date = date,
                dayName = formatUseCase.formatDayName(date),
                morningConditions = calculatePeriodConditions(morning),
                afternoonConditions = calculatePeriodConditions(afternoon),
                eveningConditions = calculatePeriodConditions(evening),
                hourlyPeriods = createHourlyPeriods(hours)
            )
        }.take(7) // Show 7 days max
    }

    /**
     * Calculate average conditions for a time period
     * Returns safe defaults if no data available
     */
    private fun calculatePeriodConditions(
        hours: List<HourlyWaveForecast>
    ): PeriodConditionsUiData {
        if (hours.isEmpty()) {
            return PeriodConditionsUiData(
                waveCategory = WaveCategory.FLAT,
                waveHeight = 0.0,
                temperature = 0.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        }

        val avgWaveHeight = hours.map { it.waveHeightMeters }.average()
        val avgTemp = hours.map { it.temperatureCelsius }.average()
        val avgCloudCover = hours.map { it.cloudCover.minCover }.average().toInt()
        val avgWindSpeed = hours.map { it.windSpeedKmh }.average()
        val avgWindDirection = hours.map { it.windDirectionDegrees }.average().toInt()
        val avgUvIndex = hours.map { it.uvIndex }.average()
        val avgSwellHeight = hours.map { it.swellHeightMeters }.average()
        val avgSwellDirection = hours.map { it.swellDirectionDegrees }.average().toInt()
        val avgSwellPeriod = hours.map { it.swellPeriodSeconds }.average()
        val avgWavePeriod = hours.map { it.wavePeriodSeconds }.average()
        val avgSeaTemp = hours.map { it.seaSurfaceTemperatureCelsius }.average()

        return PeriodConditionsUiData(
            waveCategory = WaveCategory.fromHeight(avgWaveHeight),
            waveHeight = avgWaveHeight,
            temperature = avgTemp,
            cloudCover = CloudCoverLevel.fromPercentage(avgCloudCover),
            windSpeed = avgWindSpeed,
            windDirection = avgWindDirection,
            uvIndex = avgUvIndex,
            swellHeight = avgSwellHeight,
            swellDirection = avgSwellDirection,
            swellPeriod = avgSwellPeriod,
            wavePeriod = avgWavePeriod,
            seaSurfaceTemperature = avgSeaTemp
        )
    }

    /**
     * Create 3-hour period cards for hourly view
     * Groups hours into 3-hour chunks
     */
    private fun createHourlyPeriods(hours: List<HourlyWaveForecast>): List<ThreeHourPeriodUiData> {
        if (hours.isEmpty()) return emptyList()

        // Group by 3-hour periods
        val periods = mutableListOf<ThreeHourPeriodUiData>()

        hours.chunked(3).forEach { chunk ->
            if (chunk.isNotEmpty()) {
                val firstHour = chunk.first()
                val avgWaveHeight = chunk.map { it.waveHeightMeters }.average()
                val avgTemp = chunk.map { it.temperatureCelsius }.average()
                val avgCloudCover = chunk.map { it.cloudCover.minCover }.average().toInt()

                periods.add(
                    ThreeHourPeriodUiData(
                        timeLabel = formatUseCase.formatTime(firstHour.time),
                        waveCategory = WaveCategory.fromHeight(avgWaveHeight),
                        waveHeight = avgWaveHeight,
                        waveHeightFormatted = UnitConverter.formatWaveHeightRange(avgWaveHeight, unitSystem),
                        temperature = avgTemp,
                        temperatureFormatted = WeatherFormatter.formatTemperature(avgTemp, unitSystem),
                        cloudCover = CloudCoverLevel.fromPercentage(avgCloudCover),
                        isNow = false
                    )
                )
            }
        }

        return periods
    }
}
