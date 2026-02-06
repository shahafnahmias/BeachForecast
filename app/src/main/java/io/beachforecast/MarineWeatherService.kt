package io.beachforecast

import io.beachforecast.config.ApiConfig
import io.beachforecast.data.sources.HttpClient
import io.beachforecast.data.sources.URLConnectionHttpClient
import io.beachforecast.domain.models.WaveCategory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MarineWeatherService(
    private val httpClient: HttpClient = URLConnectionHttpClient()
) {
    private val gson = Gson()


    suspend fun getCombinedWeatherData(latitude: Double, longitude: Double): CombinedWeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                // Single request to Lambda backend which fetches and merges both APIs
                val url = "${ApiConfig.SERVER_BASE_URL}${ApiConfig.WEATHER_ENDPOINT}" +
                    "?lat=$latitude&lon=$longitude&days=${ApiConfig.FORECAST_DAYS}"
                val responseJson = httpClient.get(url)
                val serverResponse = gson.fromJson(responseJson, ServerWeatherResponse::class.java)

                val marineData = serverResponse.marine
                val weatherData = serverResponse.weather

                // Find current hour index
                val currentTime = LocalDateTime.now()
                val currentHourString = currentTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 13) + ":00"

                val currentIndex = marineData.hourly.time.indexOfFirst {
                    it.startsWith(currentHourString.substring(0, 13))
                }.takeIf { it >= 0 } ?: 0

                // Combine data - use current hour's data
                val currentWaveHeight = marineData.hourly.waveHeight.getOrNull(currentIndex) ?: 0.0
                val currentTemp = weatherData.hourly.temperature.getOrNull(currentIndex) ?: 0.0
                val currentCloudCover = weatherData.hourly.cloudCover.getOrNull(currentIndex) ?: 0
                val currentWindSpeed = weatherData.hourly.windSpeed?.getOrNull(currentIndex) ?: 0.0
                val currentWindDirection = weatherData.hourly.windDirection?.getOrNull(currentIndex) ?: 0
                val currentWaveDirection = marineData.hourly.waveDirection?.getOrNull(currentIndex) ?: 0
                val currentWavePeriod = marineData.hourly.wavePeriod?.getOrNull(currentIndex) ?: 0.0
                val currentSwellHeight = marineData.hourly.swellWaveHeight?.getOrNull(currentIndex) ?: 0.0
                val currentSwellDirection = marineData.hourly.swellWaveDirection?.getOrNull(currentIndex) ?: 0
                val currentSwellPeriod = marineData.hourly.swellWavePeriod?.getOrNull(currentIndex) ?: 0.0
                val currentSeaTemp = marineData.hourly.seaSurfaceTemperature?.getOrNull(currentIndex) ?: 0.0
                val currentUvIndex = weatherData.hourly.uvIndex?.getOrNull(currentIndex) ?: 0.0

                // Get next 6 hours forecast starting from current hour
                val hourlyForecast = mutableListOf<HourlyForecast>()

                for (i in currentIndex until minOf(currentIndex + 6, marineData.hourly.time.size)) {
                    val waveHeight = marineData.hourly.waveHeight.getOrNull(i) ?: continue
                    val timeString = marineData.hourly.time[i]

                    hourlyForecast.add(
                        HourlyForecast(
                            time = timeString,
                            waveHeight = waveHeight,
                            waveCategory = WaveCategory.fromHeight(waveHeight).displayName,
                            temperature = weatherData.hourly.temperature.getOrNull(i) ?: 0.0,
                            cloudCover = weatherData.hourly.cloudCover.getOrNull(i) ?: 0,
                            windSpeed = weatherData.hourly.windSpeed?.getOrNull(i) ?: 0.0,
                            windDirection = weatherData.hourly.windDirection?.getOrNull(i) ?: 0,
                            waveDirection = marineData.hourly.waveDirection?.getOrNull(i) ?: 0,
                            wavePeriod = marineData.hourly.wavePeriod?.getOrNull(i) ?: 0.0,
                            swellHeight = marineData.hourly.swellWaveHeight?.getOrNull(i) ?: 0.0,
                            swellDirection = marineData.hourly.swellWaveDirection?.getOrNull(i) ?: 0,
                            swellPeriod = marineData.hourly.swellWavePeriod?.getOrNull(i) ?: 0.0,
                            seaTemp = marineData.hourly.seaSurfaceTemperature?.getOrNull(i) ?: 0.0,
                            uvIndex = weatherData.hourly.uvIndex?.getOrNull(i) ?: 0.0
                        )
                    )
                }

                // Get all remaining hours for today (current hour until end of day)
                val todayDate = currentTime.toLocalDate().toString()
                val todayRemaining = mutableListOf<HourlyForecast>()

                for (i in currentIndex until marineData.hourly.time.size) {
                    val timeString = marineData.hourly.time[i]
                    if (!timeString.startsWith(todayDate)) break // Stop when we hit tomorrow

                    val waveHeight = marineData.hourly.waveHeight.getOrNull(i) ?: continue

                    todayRemaining.add(
                        HourlyForecast(
                            time = timeString,
                            waveHeight = waveHeight,
                            waveCategory = WaveCategory.fromHeight(waveHeight).displayName,
                            temperature = weatherData.hourly.temperature.getOrNull(i) ?: 0.0,
                            cloudCover = weatherData.hourly.cloudCover.getOrNull(i) ?: 0,
                            windSpeed = weatherData.hourly.windSpeed?.getOrNull(i) ?: 0.0,
                            windDirection = weatherData.hourly.windDirection?.getOrNull(i) ?: 0,
                            waveDirection = marineData.hourly.waveDirection?.getOrNull(i) ?: 0,
                            wavePeriod = marineData.hourly.wavePeriod?.getOrNull(i) ?: 0.0,
                            swellHeight = marineData.hourly.swellWaveHeight?.getOrNull(i) ?: 0.0,
                            swellDirection = marineData.hourly.swellWaveDirection?.getOrNull(i) ?: 0,
                            swellPeriod = marineData.hourly.swellWavePeriod?.getOrNull(i) ?: 0.0,
                            seaTemp = marineData.hourly.seaSurfaceTemperature?.getOrNull(i) ?: 0.0,
                            uvIndex = weatherData.hourly.uvIndex?.getOrNull(i) ?: 0.0
                        )
                    )
                }

                // Get all week data (7 days)
                val weekData = mutableListOf<HourlyForecast>()
                for (i in currentIndex until marineData.hourly.time.size) {
                    val timeString = marineData.hourly.time[i]
                    val waveHeight = marineData.hourly.waveHeight.getOrNull(i) ?: continue

                    weekData.add(
                        HourlyForecast(
                            time = timeString,
                            waveHeight = waveHeight,
                            waveCategory = WaveCategory.fromHeight(waveHeight).displayName,
                            temperature = weatherData.hourly.temperature.getOrNull(i) ?: 0.0,
                            cloudCover = weatherData.hourly.cloudCover.getOrNull(i) ?: 0,
                            windSpeed = weatherData.hourly.windSpeed?.getOrNull(i) ?: 0.0,
                            windDirection = weatherData.hourly.windDirection?.getOrNull(i) ?: 0,
                            waveDirection = marineData.hourly.waveDirection?.getOrNull(i) ?: 0,
                            wavePeriod = marineData.hourly.wavePeriod?.getOrNull(i) ?: 0.0,
                            swellHeight = marineData.hourly.swellWaveHeight?.getOrNull(i) ?: 0.0,
                            swellDirection = marineData.hourly.swellWaveDirection?.getOrNull(i) ?: 0,
                            swellPeriod = marineData.hourly.swellWavePeriod?.getOrNull(i) ?: 0.0,
                            seaTemp = marineData.hourly.seaSurfaceTemperature?.getOrNull(i) ?: 0.0,
                            uvIndex = weatherData.hourly.uvIndex?.getOrNull(i) ?: 0.0
                        )
                    )
                }

                // Get tomorrow's forecast grouped by time periods
                val tomorrowForecast = getTomorrowForecast(marineData, weatherData)

                CombinedWeatherData(
                    currentWaveHeight = currentWaveHeight,
                    currentTemp = currentTemp,
                    currentCloudCover = currentCloudCover,
                    currentWindSpeed = currentWindSpeed,
                    currentWindDirection = currentWindDirection,
                    currentWaveDirection = currentWaveDirection,
                    currentWavePeriod = currentWavePeriod,
                    currentSwellHeight = currentSwellHeight,
                    currentSwellDirection = currentSwellDirection,
                    currentSwellPeriod = currentSwellPeriod,
                    currentSeaTemp = currentSeaTemp,
                    currentUvIndex = currentUvIndex,
                    hourlyForecast = hourlyForecast,
                    todayRemaining = todayRemaining,
                    weekData = weekData,
                    tomorrowForecast = tomorrowForecast,
                    coastalLatitude = marineData.latitude,
                    coastalLongitude = marineData.longitude
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch weather data for lat=%f, lon=%f", latitude, longitude)
                null
            }
        }
    }

    private fun getTomorrowForecast(marineData: MarineWeatherResponse, weatherData: WeatherResponse): TomorrowForecast {
        val currentTime = LocalDateTime.now()
        val tomorrowDate = currentTime.plusDays(1).toLocalDate()
        val tomorrowDateString = tomorrowDate.toString()

        // Filter tomorrow's data
        val tomorrowIndices = marineData.hourly.time.indices.filter {
            marineData.hourly.time[it].startsWith(tomorrowDateString)
        }

        // Group by time periods
        val morningData = tomorrowIndices.filter { idx ->
            val timeString = marineData.hourly.time[idx]
            if (timeString.length < 13) return@filter false
            val hour = timeString.substring(11, 13).toInt()
            hour in 6..11
        }

        val noonData = tomorrowIndices.filter { idx ->
            val timeString = marineData.hourly.time[idx]
            if (timeString.length < 13) return@filter false
            val hour = timeString.substring(11, 13).toInt()
            hour in 12..17
        }

        val eveningData = tomorrowIndices.filter { idx ->
            val timeString = marineData.hourly.time[idx]
            if (timeString.length < 13) return@filter false
            val hour = timeString.substring(11, 13).toInt()
            hour in 18..23
        }

        fun calculatePeriod(indices: List<Int>, label: String, hourRange: String): TimePeriod {
            val heights = indices.mapNotNull { marineData.hourly.waveHeight.getOrNull(it) }
            val temps = indices.mapNotNull { weatherData.hourly.temperature.getOrNull(it) }
            val clouds = indices.mapNotNull { weatherData.hourly.cloudCover.getOrNull(it) }

            val avgHeight = if (heights.isNotEmpty()) heights.average() else 0.0
            val avgTemp = if (temps.isNotEmpty()) temps.average() else 0.0
            val avgCloud = if (clouds.isNotEmpty()) clouds.average().toInt() else 0

            return TimePeriod(
                label = label,
                hours = hourRange,
                avgWaveHeight = avgHeight,
                category = WaveCategory.fromHeight(avgHeight).displayName,
                avgTemp = avgTemp,
                avgCloudCover = avgCloud
            )
        }

        return TomorrowForecast(
            morning = calculatePeriod(morningData, "Morning", "06:00-11:00"),
            noon = calculatePeriod(noonData, "Noon", "12:00-17:00"),
            evening = calculatePeriod(eveningData, "Evening", "18:00-23:00")
        )
    }
}
