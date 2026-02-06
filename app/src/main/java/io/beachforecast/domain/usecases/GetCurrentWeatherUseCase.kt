package io.beachforecast.domain.usecases

import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.TideEvent
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.presentation.models.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Orchestrates fetching and transforming weather data
 * This is the main use case that coordinates all other use cases
 *
 * Pure business logic - all Android dependencies injected
 */
class GetCurrentWeatherUseCase(
    private val getSelectedBeachUseCase: GetSelectedBeachUseCase,
    private val weatherRepository: WeatherRepository,
    private val languageCode: String = "en",
    private val formatUseCase: FormatWeatherForDisplayUseCase = FormatWeatherForDisplayUseCase,
    private val analyzeTodayUseCase: AnalyzeTodayForecastUseCase = AnalyzeTodayForecastUseCase(),
    private val groupPeriodsUseCase: GroupTodayByPeriodsUseCase = GroupTodayByPeriodsUseCase()
) {

    /**
     * Execute the use case
     * @param unitSystem Unit system for formatting (default: METRIC)
     * @return WeatherResult with either Success(WeatherUiData) or Error(WeatherError)
     */
    suspend fun execute(unitSystem: UnitSystem = UnitSystem.METRIC): WeatherResult<WeatherUiData> {
        // Step 1: Get selected beach (replaces direct GPS location)
        val beach = when (val beachResult = getSelectedBeachUseCase.execute()) {
            is WeatherResult.Success -> beachResult.data
            is WeatherResult.Error -> return WeatherResult.Error(beachResult.error)
            is WeatherResult.Loading -> return WeatherResult.Loading
        }

        // Step 2: Use beach name directly (no geocoding needed)
        val cityName = beach.getLocalizedName(languageCode)

        // Step 3: Get weather data using beach coordinates
        val weatherData = when (val weatherResult = weatherRepository.getAllWeatherData(beach.latitude, beach.longitude)) {
            is WeatherResult.Success -> weatherResult.data
            is WeatherResult.Error -> return WeatherResult.Error(weatherResult.error)
            is WeatherResult.Loading -> return WeatherResult.Loading
        }

        // Step 4: Transform to UI data
        val uiData = mapToUiData(cityName, beach.latitude, beach.longitude, weatherData, unitSystem)

        return WeatherResult.Success(uiData)
    }

    /**
     * Map repository data to UI data
     * Extracted from ViewModel, now testable
     */
    private fun mapToUiData(
        cityName: String,
        latitude: Double,
        longitude: Double,
        weatherData: WeatherRepository.WeatherData,
        unitSystem: UnitSystem
    ): WeatherUiData {
        val current = weatherData.currentConditions
        val currentCategory = current.waveCategory

        return WeatherUiData(
            location = LocationUiData(
                cityName = cityName,
                latitude = latitude,
                longitude = longitude
            ),
            current = CurrentConditionsUiData(
                temperature = current.temperatureCelsius,
                temperatureFormatted = WeatherFormatter.formatTemperature(current.temperatureCelsius, unitSystem),
                cloudCover = current.cloudCover,
                waveCategory = currentCategory,
                waveHeight = current.waveHeightMeters,
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(current.waveHeightMeters, unitSystem),
                categoryWithHeight = currentCategory.formatWithHeight(current.waveHeightMeters, unitSystem),
                // Weather data
                windSpeed = current.windSpeedKmh,
                windSpeedFormatted = WeatherFormatter.formatWindSpeed(current.windSpeedKmh, unitSystem),
                windDirection = current.windDirectionDegrees,
                windDirectionFormatted = WeatherFormatter.formatWindDirection(current.windDirectionDegrees),
                uvIndex = current.uvIndex,
                uvIndexFormatted = WeatherFormatter.formatUvIndex(current.uvIndex),
                // Wave data
                waveDirection = current.waveDirectionDegrees,
                waveDirectionFormatted = WeatherFormatter.formatWaveDirection(current.waveDirectionDegrees),
                wavePeriod = current.wavePeriodSeconds,
                wavePeriodFormatted = WeatherFormatter.formatWavePeriod(current.wavePeriodSeconds),
                // Swell data
                swellHeight = current.swellHeightMeters,
                swellHeightFormatted = WeatherFormatter.formatSwellHeight(current.swellHeightMeters, unitSystem),
                swellDirection = current.swellDirectionDegrees,
                swellDirectionFormatted = WeatherFormatter.formatSwellDirection(current.swellDirectionDegrees),
                swellPeriod = current.swellPeriodSeconds,
                swellPeriodFormatted = WeatherFormatter.formatSwellPeriod(current.swellPeriodSeconds),
                // Ocean data
                seaSurfaceTemperature = current.seaSurfaceTemperatureCelsius,
                seaSurfaceTemperatureFormatted = WeatherFormatter.formatSeaTemperature(current.seaSurfaceTemperatureCelsius, unitSystem)
            ),
            todayForecast = TodayForecastUiData(
                isAllDayConstant = analyzeTodayUseCase.isAllDayConstant(
                    currentCategory,
                    weatherData.todayRemaining
                ),
                hourlyForecast = weatherData.todayRemaining.mapIndexed { index, hourly ->
                    mapHourlyToUi(hourly, unitSystem, isNow = index == 0)
                }
            ),
            todayPeriods = groupPeriodsUseCase.execute(weatherData.todayRemaining).mapIndexed { index, period ->
                ThreeHourPeriodUiData(
                    timeLabel = period.timeLabel,
                    waveCategory = period.waveCategory,
                    waveHeight = period.avgWaveHeight,
                    waveHeightFormatted = UnitConverter.formatWaveHeightRange(period.avgWaveHeight, UnitSystem.METRIC),
                    temperature = period.avgTemp,
                    temperatureFormatted = WeatherFormatter.formatTemperature(period.avgTemp, unitSystem),
                    cloudCover = io.beachforecast.domain.models.CloudCoverLevel.entries.find {
                        period.avgCloudCover in it.minCover..it.maxCover
                    } ?: io.beachforecast.domain.models.CloudCoverLevel.CLEAR,
                    isNow = index == 0,  // First period is always "Now"
                    // Additional metrics
                    windSpeed = period.avgWindSpeed,
                    windDirection = period.avgWindDirection,
                    uvIndex = period.avgUvIndex,
                    swellHeight = period.avgSwellHeight,
                    swellDirection = period.avgSwellDirection,
                    swellPeriod = period.avgSwellPeriod,
                    wavePeriod = period.avgWavePeriod
                )
            },
            weekForecast = GroupForecastByDaysUseCase(
                formatUseCase = formatUseCase,
                unitSystem = unitSystem
            ).execute(weatherData.weekData),
            weekHourly = buildWeekHourly(weatherData, unitSystem),
            tideEvents = mapTideEvents(weatherData.tideEvents, unitSystem)
        )
    }

    /**
     * Build per-day hourly data for the Trends day picker.
     * Index 0 = today (remaining hours), index 1+ = full 24h days.
     */
    private fun buildWeekHourly(
        weatherData: WeatherRepository.WeatherData,
        unitSystem: UnitSystem
    ): List<List<HourlyForecastUiData>> {
        val groupedByDay = weatherData.weekData
            .filter { formatUseCase.isValidIsoTimestamp(it.time) }
            .groupBy { formatUseCase.extractDate(it.time) }
            .values.toList()

        if (groupedByDay.isEmpty()) return emptyList()

        return groupedByDay.mapIndexed { dayIndex, hours ->
            if (dayIndex == 0) {
                // Today: use todayRemaining (already filtered to remaining hours)
                weatherData.todayRemaining.mapIndexed { index, hourly ->
                    mapHourlyToUi(hourly, unitSystem, isNow = index == 0)
                }
            } else {
                hours.map { hourly ->
                    mapHourlyToUi(hourly, unitSystem, isNow = false)
                }
            }
        }
    }

    private fun mapTideEvents(
        tideEvents: List<TideEvent>,
        unitSystem: UnitSystem
    ): List<TideEventUiData> {
        if (tideEvents.isEmpty()) return emptyList()

        val now = try {
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        } catch (e: Exception) {
            ""
        }

        // Find the index of the next upcoming tide
        val nextIndex = tideEvents.indexOfFirst { it.time > now }.takeIf { it >= 0 }

        return tideEvents.mapIndexed { index, event ->
            val heightFormatted = if (unitSystem == UnitSystem.IMPERIAL) {
                "%.1f ft".format(event.height * 3.28084f)
            } else {
                "%.2f m".format(event.height)
            }

            TideEventUiData(
                time = event.time,
                timeFormatted = formatUseCase.formatTime(event.time),
                height = heightFormatted,
                type = event.type,
                isNext = index == nextIndex
            )
        }
    }

    private fun mapHourlyToUi(
        hourly: io.beachforecast.domain.models.HourlyWaveForecast,
        unitSystem: UnitSystem,
        isNow: Boolean
    ): HourlyForecastUiData {
        return HourlyForecastUiData(
            time = hourly.time,
            timeFormatted = formatUseCase.formatTime(hourly.time),
            waveCategory = hourly.waveCategory,
            waveHeight = hourly.waveHeightMeters,
            categoryWithHeight = hourly.waveCategory.formatWithHeight(hourly.waveHeightMeters, unitSystem),
            temperature = hourly.temperatureCelsius,
            cloudCover = hourly.cloudCover,
            isNow = isNow,
            windSpeed = hourly.windSpeedKmh,
            windDirection = hourly.windDirectionDegrees,
            uvIndex = hourly.uvIndex,
            waveDirection = hourly.waveDirectionDegrees,
            wavePeriod = hourly.wavePeriodSeconds,
            swellHeight = hourly.swellHeightMeters,
            swellDirection = hourly.swellDirectionDegrees,
            swellPeriod = hourly.swellPeriodSeconds,
            seaSurfaceTemperature = hourly.seaSurfaceTemperatureCelsius
        )
    }
}
