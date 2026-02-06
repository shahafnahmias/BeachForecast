package io.beachforecast

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for the Lambda backend response which returns both
 * Open-Meteo API responses in a single JSON object.
 */
data class ServerWeatherResponse(
    @SerializedName("marine")
    val marine: MarineWeatherResponse,
    @SerializedName("weather")
    val weather: WeatherResponse
)

data class MarineWeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("hourly")
    val hourly: MarineHourlyData,
    @SerializedName("hourly_units")
    val hourlyUnits: MarineHourlyUnits
)

data class MarineHourlyData(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("wave_height")
    val waveHeight: List<Double?>,
    @SerializedName("wave_direction")
    val waveDirection: List<Int?>?,
    @SerializedName("wave_period")
    val wavePeriod: List<Double?>?,
    @SerializedName("swell_wave_height")
    val swellWaveHeight: List<Double?>?,
    @SerializedName("swell_wave_direction")
    val swellWaveDirection: List<Int?>?,
    @SerializedName("swell_wave_period")
    val swellWavePeriod: List<Double?>?,
    @SerializedName("sea_surface_temperature")
    val seaSurfaceTemperature: List<Double?>?
)

data class MarineHourlyUnits(
    @SerializedName("wave_height")
    val waveHeight: String,
    @SerializedName("wave_direction")
    val waveDirection: String,
    @SerializedName("wave_period")
    val wavePeriod: String,
    @SerializedName("ocean_surface_temperature")
    val seaSurfaceTemperature: String
)

data class WeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("hourly")
    val hourly: WeatherHourlyData,
    @SerializedName("hourly_units")
    val hourlyUnits: WeatherHourlyUnits
)

data class WeatherHourlyData(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double?>,
    @SerializedName("cloud_cover")
    val cloudCover: List<Int?>,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double?>?,
    @SerializedName("wind_direction_10m")
    val windDirection: List<Int?>?,
    @SerializedName("uv_index")
    val uvIndex: List<Double?>?
)

data class WeatherHourlyUnits(
    @SerializedName("temperature_2m")
    val temperature: String,
    @SerializedName("cloud_cover")
    val cloudCover: String,
    @SerializedName("wind_speed_10m")
    val windSpeed: String,
    @SerializedName("wind_direction_10m")
    val windDirection: String,
    @SerializedName("uv_index")
    val uvIndex: String
)

data class TideEventDto(
    val time: String,
    val height: Float,
    val type: String  // "high" or "low"
)

data class CombinedWeatherData(
    val currentWaveHeight: Double,
    val currentTemp: Double,
    val currentCloudCover: Int,
    val currentWindSpeed: Double,
    val currentWindDirection: Int,
    val currentWaveDirection: Int,
    val currentWavePeriod: Double,
    val currentSwellHeight: Double,
    val currentSwellDirection: Int,
    val currentSwellPeriod: Double,
    val currentSeaTemp: Double,
    val currentUvIndex: Double,
    val hourlyForecast: List<HourlyForecast>,
    val todayRemaining: List<HourlyForecast>,
    val weekData: List<HourlyForecast> = emptyList(),
    val tomorrowForecast: TomorrowForecast,
    val coastalLatitude: Double,
    val coastalLongitude: Double,
    val tides: List<TideEventDto>? = null
)

data class HourlyForecast(
    val time: String,
    val waveHeight: Double,
    val waveCategory: String,
    val temperature: Double = 0.0,
    val cloudCover: Int = 0,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val waveDirection: Int = 0,
    val wavePeriod: Double = 0.0,
    val swellHeight: Double = 0.0,
    val swellDirection: Int = 0,
    val swellPeriod: Double = 0.0,
    val seaTemp: Double = 0.0,
    val uvIndex: Double = 0.0
)

data class TomorrowForecast(
    val morning: TimePeriod,
    val noon: TimePeriod,
    val evening: TimePeriod
)

data class TimePeriod(
    val label: String,
    val hours: String,
    val avgWaveHeight: Double,
    val category: String,
    val avgTemp: Double,
    val avgCloudCover: Int
)
