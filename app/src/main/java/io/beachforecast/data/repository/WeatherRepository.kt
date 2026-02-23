package io.beachforecast.data.repository

import io.beachforecast.MarineWeatherService
import io.beachforecast.data.mappers.WeatherDataMapper
import io.beachforecast.domain.models.WeatherError
import io.beachforecast.domain.models.WeatherResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository with error handling
 * Returns Result type with detailed error information
 */
class WeatherRepository(
    private val marineWeatherService: MarineWeatherService = MarineWeatherService()
) {

    companion object {
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val mutex = Mutex()
    private var cachedData: WeatherData? = null
    private var cachedTimestamp: Long = 0L
    private var cachedLatitude: Double? = null
    private var cachedLongitude: Double? = null

    /**
     * Weather data model for repository layer
     */
    data class WeatherData(
        val currentConditions: io.beachforecast.domain.models.CurrentConditions,
        val todayRemaining: List<io.beachforecast.domain.models.HourlyWaveForecast>,
        val weekData: List<io.beachforecast.domain.models.HourlyWaveForecast>,
        val tomorrowForecast: List<io.beachforecast.domain.models.PeriodForecast>,
        val coastalLocation: CoastalLocation,
        val tideEvents: List<io.beachforecast.domain.models.TideEvent> = emptyList()
    )

    data class CoastalLocation(
        val latitude: Double,
        val longitude: Double
    )

    /**
     * Get all weather data with detailed error handling
     * Uses cache to prevent duplicate concurrent requests
     */
    suspend fun getAllWeatherData(
        latitude: Double,
        longitude: Double
    ): WeatherResult<WeatherRepository.WeatherData> {
        return mutex.withLock {
            try {
                // Check if we have valid cached data for same location
                val now = System.currentTimeMillis()
                if (cachedData != null &&
                    cachedLatitude == latitude &&
                    cachedLongitude == longitude &&
                    (now - cachedTimestamp) < CACHE_VALIDITY_MS
                ) {
                    cachedData?.let { return WeatherResult.Success(it) }
                }

                // Validate coordinates
                if (!isValidCoordinate(latitude, longitude)) {
                    return WeatherResult.Error(WeatherError.InvalidCoordinates)
                }

                val apiData = marineWeatherService.getCombinedWeatherData(latitude, longitude)

                if (apiData == null) {
                    return WeatherResult.Error(WeatherError.NoDataAvailable)
                }

                val mappedData = WeatherDataMapper.mapToWeatherData(apiData)

                // Cache the data
                cachedData = mappedData
                cachedTimestamp = now
                cachedLatitude = latitude
                cachedLongitude = longitude

                WeatherResult.Success(mappedData)

            } catch (e: UnknownHostException) {
                WeatherResult.Error(WeatherError.NoInternet)

            } catch (e: SocketTimeoutException) {
                WeatherResult.Error(WeatherError.NetworkTimeout)

            } catch (e: IOException) {
                WeatherResult.Error(WeatherError.NoInternet)

            } catch (e: com.google.gson.JsonParseException) {
                WeatherResult.Error(WeatherError.DataParsingError)

            } catch (e: IllegalArgumentException) {
                WeatherResult.Error(WeatherError.DataParsingError)

            } catch (e: Exception) {
                Timber.e(e, "Unexpected error fetching weather")
                WeatherResult.Error(WeatherError.Unknown(e))
            }
        }
    }

    /**
     * Validate coordinates are within valid ranges
     */
    private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Check if coordinates are likely near coast (within 100km)
     * This is a simple heuristic - for production, use proper coastal detection
     */
    private fun isNearCoast(latitude: Double, longitude: Double): Boolean {
        // TODO: Implement proper coastal proximity check
        // For now, just validate ranges
        return isValidCoordinate(latitude, longitude)
    }
}
