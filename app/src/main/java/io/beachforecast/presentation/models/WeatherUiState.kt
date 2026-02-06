package io.beachforecast.presentation.models

import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.TideType
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.domain.models.WeatherError

/**
 * UI state for weather screens
 */
sealed class WeatherUiState {
    object Initial : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: WeatherUiData) : WeatherUiState()
    data class Error(val error: WeatherError) : WeatherUiState()
}

/**
 * UI data model for weather information
 */
data class WeatherUiData(
    val location: LocationUiData,
    val current: CurrentConditionsUiData,
    val todayForecast: TodayForecastUiData,
    val todayPeriods: List<ThreeHourPeriodUiData> = emptyList(),
    val weekForecast: List<DayForecastUiData>,
    val weekHourly: List<List<HourlyForecastUiData>> = emptyList(),
    val tideEvents: List<TideEventUiData> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class TideEventUiData(
    val time: String,
    val timeFormatted: String,
    val height: String,
    val type: TideType,
    val isNext: Boolean
)

data class LocationUiData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double
)

data class CurrentConditionsUiData(
    val temperature: Double,
    val temperatureFormatted: String,
    val cloudCover: CloudCoverLevel,
    val waveCategory: WaveCategory,
    val waveHeight: Double,
    val waveHeightFormatted: String,
    val categoryWithHeight: String,
    // New weather data
    val windSpeed: Double = 0.0,
    val windSpeedFormatted: String = "",
    val windDirection: Int = 0,
    val windDirectionFormatted: String = "",
    val uvIndex: Double = 0.0,
    val uvIndexFormatted: String = "",
    // New wave data
    val waveDirection: Int = 0,
    val waveDirectionFormatted: String = "",
    val wavePeriod: Double = 0.0,
    val wavePeriodFormatted: String = "",
    // New swell data
    val swellHeight: Double = 0.0,
    val swellHeightFormatted: String = "",
    val swellDirection: Int = 0,
    val swellDirectionFormatted: String = "",
    val swellPeriod: Double = 0.0,
    val swellPeriodFormatted: String = "",
    // New ocean data
    val seaSurfaceTemperature: Double = 0.0,
    val seaSurfaceTemperatureFormatted: String = ""
)

data class TodayForecastUiData(
    val isAllDayConstant: Boolean,
    val hourlyForecast: List<HourlyForecastUiData>
)

data class HourlyForecastUiData(
    val time: String,
    val timeFormatted: String,
    val waveCategory: WaveCategory,
    val waveHeight: Double,
    val categoryWithHeight: String,
    val temperature: Double,
    val cloudCover: CloudCoverLevel,
    val isNow: Boolean = false,
    // New weather data
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val uvIndex: Double = 0.0,
    // New wave data
    val waveDirection: Int = 0,
    val wavePeriod: Double = 0.0,
    // New swell data
    val swellHeight: Double = 0.0,
    val swellDirection: Int = 0,
    val swellPeriod: Double = 0.0,
    // New ocean data
    val seaSurfaceTemperature: Double = 0.0
)

data class DayForecastUiData(
    val date: String,
    val dayName: String,
    val morningConditions: PeriodConditionsUiData,
    val afternoonConditions: PeriodConditionsUiData,
    val eveningConditions: PeriodConditionsUiData,
    val hourlyPeriods: List<ThreeHourPeriodUiData> = emptyList()
)

data class PeriodConditionsUiData(
    val waveCategory: WaveCategory,
    val waveHeight: Double,
    val temperature: Double,
    val cloudCover: CloudCoverLevel,
    // Additional metrics for customization
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val uvIndex: Double = 0.0,
    val swellHeight: Double = 0.0,
    val swellDirection: Int = 0,
    val swellPeriod: Double = 0.0,
    val wavePeriod: Double = 0.0,
    val seaSurfaceTemperature: Double = 0.0
)

data class ThreeHourPeriodUiData(
    val timeLabel: String, // "Now", "06:00", "09:00", etc.
    val waveCategory: WaveCategory,
    val waveHeight: Double,
    val waveHeightFormatted: String, // "20-40cm"
    val temperature: Double,
    val temperatureFormatted: String,
    val cloudCover: CloudCoverLevel,
    val isNow: Boolean = false,
    // Additional metrics for customization
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val uvIndex: Double = 0.0,
    val swellHeight: Double = 0.0,
    val swellDirection: Int = 0,
    val swellPeriod: Double = 0.0,
    val wavePeriod: Double = 0.0
)
