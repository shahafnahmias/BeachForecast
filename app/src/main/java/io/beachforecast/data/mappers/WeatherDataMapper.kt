package io.beachforecast.data.mappers

import io.beachforecast.CombinedWeatherData
import io.beachforecast.HourlyForecast
import io.beachforecast.TideEventDto
import io.beachforecast.TimePeriod
import io.beachforecast.domain.calculators.WaveCategoryCalculator
import io.beachforecast.domain.models.*

/**
 * Maps API data models to domain models
 * Keeps data layer separate from domain layer
 */
object WeatherDataMapper {

    /**
     * Map CombinedWeatherData (from API) to domain WeatherRepository.WeatherData
     */
    fun mapToWeatherData(apiData: CombinedWeatherData): io.beachforecast.data.repository.WeatherRepository.WeatherData {
        val currentConditions = CurrentConditions(
            waveCategory = WaveCategory.fromHeight(apiData.currentWaveHeight),
            waveHeightMeters = apiData.currentWaveHeight,
            temperatureCelsius = apiData.currentTemp,
            cloudCover = CloudCoverLevel.fromPercentage(apiData.currentCloudCover),
            windSpeedKmh = apiData.currentWindSpeed,
            windDirectionDegrees = apiData.currentWindDirection,
            waveDirectionDegrees = apiData.currentWaveDirection,
            wavePeriodSeconds = apiData.currentWavePeriod,
            swellHeightMeters = apiData.currentSwellHeight,
            swellDirectionDegrees = apiData.currentSwellDirection,
            swellPeriodSeconds = apiData.currentSwellPeriod,
            seaSurfaceTemperatureCelsius = apiData.currentSeaTemp,
            uvIndex = apiData.currentUvIndex
        )

        val todayRemaining = apiData.todayRemaining.map { mapToHourlyWaveForecast(it) }
        val weekData = apiData.weekData.map { mapToHourlyWaveForecast(it) }

        val tomorrowForecast = listOf(
            mapToPeriodForecast(apiData.tomorrowForecast.morning),
            mapToPeriodForecast(apiData.tomorrowForecast.noon),
            mapToPeriodForecast(apiData.tomorrowForecast.evening)
        )

        return io.beachforecast.data.repository.WeatherRepository.WeatherData(
            currentConditions = currentConditions,
            todayRemaining = todayRemaining,
            weekData = weekData,
            tomorrowForecast = tomorrowForecast,
            coastalLocation = io.beachforecast.data.repository.WeatherRepository.CoastalLocation(
                latitude = apiData.coastalLatitude,
                longitude = apiData.coastalLongitude
            ),
            tideEvents = mapToTideEvents(apiData.tides)
        )
    }

    /**
     * Map HourlyForecast (API) to HourlyWaveForecast (domain)
     */
    fun mapToHourlyWaveForecast(apiHourly: HourlyForecast): HourlyWaveForecast {
        return HourlyWaveForecast(
            time = apiHourly.time,
            waveCategory = WaveCategory.fromHeight(apiHourly.waveHeight),
            waveHeightMeters = apiHourly.waveHeight,
            temperatureCelsius = apiHourly.temperature,
            cloudCover = CloudCoverLevel.fromPercentage(apiHourly.cloudCover),
            windSpeedKmh = apiHourly.windSpeed,
            windDirectionDegrees = apiHourly.windDirection,
            waveDirectionDegrees = apiHourly.waveDirection,
            wavePeriodSeconds = apiHourly.wavePeriod,
            swellHeightMeters = apiHourly.swellHeight,
            swellDirectionDegrees = apiHourly.swellDirection,
            swellPeriodSeconds = apiHourly.swellPeriod,
            seaSurfaceTemperatureCelsius = apiHourly.seaTemp,
            uvIndex = apiHourly.uvIndex
        )
    }

    /**
     * Map TideEventDto list to TideEvent domain models.
     * Filters out malformed entries and sorts by time.
     */
    fun mapToTideEvents(dtos: List<TideEventDto>?): List<TideEvent> {
        if (dtos.isNullOrEmpty()) return emptyList()

        return dtos.mapNotNull { dto ->
            val type = when (dto.type.lowercase()) {
                "high" -> TideType.HIGH
                "low" -> TideType.LOW
                else -> null
            }
            type?.let {
                TideEvent(
                    time = dto.time,
                    height = dto.height,
                    type = it
                )
            }
        }.sortedBy { it.time }
    }

    /**
     * Map TimePeriod (API) to PeriodForecast (domain)
     */
    fun mapToPeriodForecast(apiPeriod: TimePeriod): PeriodForecast {
        // Determine TimeOfDay from label
        val timeOfDay = when (apiPeriod.label) {
            "Morning" -> TimeOfDay.MORNING
            "Noon" -> TimeOfDay.AFTERNOON
            "Evening" -> TimeOfDay.EVENING
            else -> TimeOfDay.MORNING
        }

        return PeriodForecast(
            timeOfDay = timeOfDay,
            label = apiPeriod.label,
            hours = apiPeriod.hours,
            avgWaveCategory = WaveCategory.fromHeight(apiPeriod.avgWaveHeight),
            avgWaveHeightMeters = apiPeriod.avgWaveHeight,
            avgTemperatureCelsius = apiPeriod.avgTemp,
            avgCloudCover = CloudCoverLevel.fromPercentage(apiPeriod.avgCloudCover)
        )
    }
}
