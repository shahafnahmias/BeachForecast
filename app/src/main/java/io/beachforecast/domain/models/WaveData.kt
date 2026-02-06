package io.beachforecast.domain.models

import androidx.compose.runtime.Stable

/**
 * Domain model for current wave and weather conditions
 */
@Stable
data class CurrentConditions(
    val waveCategory: WaveCategory,
    val waveHeightMeters: Double,
    val temperatureCelsius: Double,
    val cloudCover: CloudCoverLevel,
    val windSpeedKmh: Double = 0.0,
    val windDirectionDegrees: Int = 0,
    val waveDirectionDegrees: Int = 0,
    val wavePeriodSeconds: Double = 0.0,
    val swellHeightMeters: Double = 0.0,
    val swellDirectionDegrees: Int = 0,
    val swellPeriodSeconds: Double = 0.0,
    val seaSurfaceTemperatureCelsius: Double = 0.0,
    val uvIndex: Double = 0.0
)

/**
 * Domain model for hourly forecast
 */
@Stable
data class HourlyWaveForecast(
    val time: String,  // ISO timestamp format
    val waveCategory: WaveCategory,
    val waveHeightMeters: Double,
    val temperatureCelsius: Double = 0.0,
    val cloudCover: CloudCoverLevel = CloudCoverLevel.CLEAR,
    val windSpeedKmh: Double = 0.0,
    val windDirectionDegrees: Int = 0,
    val waveDirectionDegrees: Int = 0,
    val wavePeriodSeconds: Double = 0.0,
    val swellHeightMeters: Double = 0.0,
    val swellDirectionDegrees: Int = 0,
    val swellPeriodSeconds: Double = 0.0,
    val seaSurfaceTemperatureCelsius: Double = 0.0,
    val uvIndex: Double = 0.0
)

/**
 * Domain model for a period's aggregated forecast
 */
data class PeriodForecast(
    val timeOfDay: TimeOfDay,
    val label: String,
    val hours: String,
    val avgWaveCategory: WaveCategory,
    val avgWaveHeightMeters: Double,
    val avgTemperatureCelsius: Double,
    val avgCloudCover: CloudCoverLevel
)

/**
 * Complete forecast data for display
 */
data class CompleteForecastData(
    val currentConditions: CurrentConditions,
    val todayRemainingHours: List<HourlyWaveForecast>,
    val tomorrowPeriods: List<PeriodForecast>,
    val locationName: String
)
