package io.beachforecast.data.mappers

import io.beachforecast.CombinedWeatherData
import io.beachforecast.HourlyForecast
import io.beachforecast.TideEventDto
import io.beachforecast.TimePeriod
import io.beachforecast.TomorrowForecast
import io.beachforecast.domain.models.*
import org.junit.Assert.*
import org.junit.Test

class WeatherDataMapperTest {

    // region mapToHourlyWaveForecast

    @Test
    fun `mapToHourlyWaveForecast maps all fields correctly`() {
        val apiHourly = HourlyForecast(
            time = "2026-02-06T14:00",
            waveHeight = 1.2,
            waveCategory = "Waist",
            temperature = 25.0,
            cloudCover = 45,
            windSpeed = 15.0,
            windDirection = 180,
            waveDirection = 270,
            wavePeriod = 8.5,
            swellHeight = 1.0,
            swellDirection = 250,
            swellPeriod = 10.0,
            seaTemp = 22.0,
            uvIndex = 6.0
        )

        val result = WeatherDataMapper.mapToHourlyWaveForecast(apiHourly)

        assertEquals("2026-02-06T14:00", result.time)
        assertEquals(WaveCategory.WAIST, result.waveCategory)
        assertEquals(1.2, result.waveHeightMeters, 0.001)
        assertEquals(25.0, result.temperatureCelsius, 0.001)
        assertEquals(CloudCoverLevel.PARTLY_CLOUDY, result.cloudCover)
        assertEquals(15.0, result.windSpeedKmh, 0.001)
        assertEquals(180, result.windDirectionDegrees)
        assertEquals(270, result.waveDirectionDegrees)
        assertEquals(8.5, result.wavePeriodSeconds, 0.001)
        assertEquals(1.0, result.swellHeightMeters, 0.001)
        assertEquals(250, result.swellDirectionDegrees)
        assertEquals(10.0, result.swellPeriodSeconds, 0.001)
        assertEquals(22.0, result.seaSurfaceTemperatureCelsius, 0.001)
        assertEquals(6.0, result.uvIndex, 0.001)
    }

    @Test
    fun `mapToHourlyWaveForecast uses default values when not specified`() {
        val apiHourly = HourlyForecast(
            time = "2026-02-06T08:00",
            waveHeight = 0.5,
            waveCategory = "Ankle"
        )

        val result = WeatherDataMapper.mapToHourlyWaveForecast(apiHourly)

        assertEquals("2026-02-06T08:00", result.time)
        assertEquals(WaveCategory.ANKLE, result.waveCategory)
        assertEquals(0.5, result.waveHeightMeters, 0.001)
        assertEquals(0.0, result.temperatureCelsius, 0.001)
        assertEquals(CloudCoverLevel.CLEAR, result.cloudCover)
        assertEquals(0.0, result.windSpeedKmh, 0.001)
        assertEquals(0, result.windDirectionDegrees)
        assertEquals(0, result.waveDirectionDegrees)
        assertEquals(0.0, result.wavePeriodSeconds, 0.001)
        assertEquals(0.0, result.swellHeightMeters, 0.001)
        assertEquals(0, result.swellDirectionDegrees)
        assertEquals(0.0, result.swellPeriodSeconds, 0.001)
        assertEquals(0.0, result.seaSurfaceTemperatureCelsius, 0.001)
        assertEquals(0.0, result.uvIndex, 0.001)
    }

    @Test
    fun `mapToHourlyWaveForecast maps flat waves correctly`() {
        val apiHourly = HourlyForecast(
            time = "2026-02-06T06:00",
            waveHeight = 0.1,
            waveCategory = "Flat"
        )

        val result = WeatherDataMapper.mapToHourlyWaveForecast(apiHourly)

        assertEquals(WaveCategory.FLAT, result.waveCategory)
        assertEquals(0.1, result.waveHeightMeters, 0.001)
    }

    @Test
    fun `mapToHourlyWaveForecast maps high waves correctly`() {
        val apiHourly = HourlyForecast(
            time = "2026-02-06T12:00",
            waveHeight = 3.0,
            waveCategory = "Overhead"
        )

        val result = WeatherDataMapper.mapToHourlyWaveForecast(apiHourly)

        assertEquals(WaveCategory.OVERHEAD, result.waveCategory)
        assertEquals(3.0, result.waveHeightMeters, 0.001)
    }

    // endregion

    // region mapToPeriodForecast

    @Test
    fun `mapToPeriodForecast maps Morning label to MORNING`() {
        val period = TimePeriod(
            label = "Morning",
            hours = "06:00-11:00",
            avgWaveHeight = 1.0,
            category = "Waist",
            avgTemp = 22.0,
            avgCloudCover = 20
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(TimeOfDay.MORNING, result.timeOfDay)
        assertEquals("Morning", result.label)
        assertEquals("06:00-11:00", result.hours)
        assertEquals(WaveCategory.WAIST, result.avgWaveCategory)
        assertEquals(1.0, result.avgWaveHeightMeters, 0.001)
        assertEquals(22.0, result.avgTemperatureCelsius, 0.001)
        assertEquals(CloudCoverLevel.MOSTLY_CLEAR, result.avgCloudCover)
    }

    @Test
    fun `mapToPeriodForecast maps Noon label to AFTERNOON`() {
        val period = TimePeriod(
            label = "Noon",
            hours = "12:00-17:00",
            avgWaveHeight = 1.5,
            category = "Chest",
            avgTemp = 28.0,
            avgCloudCover = 50
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(TimeOfDay.AFTERNOON, result.timeOfDay)
        assertEquals("Noon", result.label)
        assertEquals("12:00-17:00", result.hours)
    }

    @Test
    fun `mapToPeriodForecast maps Evening label to EVENING`() {
        val period = TimePeriod(
            label = "Evening",
            hours = "18:00-23:00",
            avgWaveHeight = 0.8,
            category = "Knee",
            avgTemp = 24.0,
            avgCloudCover = 70
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(TimeOfDay.EVENING, result.timeOfDay)
        assertEquals("Evening", result.label)
        assertEquals("18:00-23:00", result.hours)
        assertEquals(WaveCategory.KNEE, result.avgWaveCategory)
        assertEquals(CloudCoverLevel.CLOUDY, result.avgCloudCover)
    }

    @Test
    fun `mapToPeriodForecast maps unknown label to MORNING as default`() {
        val period = TimePeriod(
            label = "Night",
            hours = "00:00-05:00",
            avgWaveHeight = 0.5,
            category = "Ankle",
            avgTemp = 18.0,
            avgCloudCover = 5
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(TimeOfDay.MORNING, result.timeOfDay)
        assertEquals("Night", result.label)
    }

    @Test
    fun `mapToPeriodForecast maps empty label to MORNING as default`() {
        val period = TimePeriod(
            label = "",
            hours = "",
            avgWaveHeight = 0.2,
            category = "Flat",
            avgTemp = 15.0,
            avgCloudCover = 0
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(TimeOfDay.MORNING, result.timeOfDay)
    }

    // endregion

    // region mapToWeatherData

    @Test
    fun `mapToWeatherData maps current conditions correctly`() {
        val apiData = createCombinedWeatherData(
            currentWaveHeight = 1.3,
            currentTemp = 26.0,
            currentCloudCover = 40,
            currentWindSpeed = 12.0,
            currentWindDirection = 200,
            currentWaveDirection = 280,
            currentWavePeriod = 7.0,
            currentSwellHeight = 1.1,
            currentSwellDirection = 260,
            currentSwellPeriod = 9.0,
            currentSeaTemp = 23.0,
            currentUvIndex = 5.0
        )

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        val current = result.currentConditions
        assertEquals(WaveCategory.WAIST, current.waveCategory)
        assertEquals(1.3, current.waveHeightMeters, 0.001)
        assertEquals(26.0, current.temperatureCelsius, 0.001)
        assertEquals(CloudCoverLevel.PARTLY_CLOUDY, current.cloudCover)
        assertEquals(12.0, current.windSpeedKmh, 0.001)
        assertEquals(200, current.windDirectionDegrees)
        assertEquals(280, current.waveDirectionDegrees)
        assertEquals(7.0, current.wavePeriodSeconds, 0.001)
        assertEquals(1.1, current.swellHeightMeters, 0.001)
        assertEquals(260, current.swellDirectionDegrees)
        assertEquals(9.0, current.swellPeriodSeconds, 0.001)
        assertEquals(23.0, current.seaSurfaceTemperatureCelsius, 0.001)
        assertEquals(5.0, current.uvIndex, 0.001)
    }

    @Test
    fun `mapToWeatherData maps todayRemaining list`() {
        val hourly1 = HourlyForecast(
            time = "2026-02-06T15:00",
            waveHeight = 0.8,
            waveCategory = "Knee",
            temperature = 24.0,
            cloudCover = 30
        )
        val hourly2 = HourlyForecast(
            time = "2026-02-06T16:00",
            waveHeight = 1.0,
            waveCategory = "Waist",
            temperature = 23.0,
            cloudCover = 50
        )

        val apiData = createCombinedWeatherData(
            todayRemaining = listOf(hourly1, hourly2)
        )

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertEquals(2, result.todayRemaining.size)
        assertEquals("2026-02-06T15:00", result.todayRemaining[0].time)
        assertEquals(WaveCategory.KNEE, result.todayRemaining[0].waveCategory)
        assertEquals("2026-02-06T16:00", result.todayRemaining[1].time)
        assertEquals(WaveCategory.WAIST, result.todayRemaining[1].waveCategory)
    }

    @Test
    fun `mapToWeatherData maps weekData list`() {
        val weekHourly = HourlyForecast(
            time = "2026-02-07T10:00",
            waveHeight = 2.2,
            waveCategory = "Head High",
            temperature = 20.0,
            cloudCover = 80
        )

        val apiData = createCombinedWeatherData(
            weekData = listOf(weekHourly)
        )

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertEquals(1, result.weekData.size)
        assertEquals("2026-02-07T10:00", result.weekData[0].time)
        assertEquals(WaveCategory.HEAD_HIGH, result.weekData[0].waveCategory)
    }

    @Test
    fun `mapToWeatherData maps tomorrow forecast with three periods`() {
        val apiData = createCombinedWeatherData()

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertEquals(3, result.tomorrowForecast.size)
        assertEquals(TimeOfDay.MORNING, result.tomorrowForecast[0].timeOfDay)
        assertEquals(TimeOfDay.AFTERNOON, result.tomorrowForecast[1].timeOfDay)
        assertEquals(TimeOfDay.EVENING, result.tomorrowForecast[2].timeOfDay)
    }

    @Test
    fun `mapToWeatherData maps coastal location`() {
        val apiData = createCombinedWeatherData(
            coastalLatitude = 32.08,
            coastalLongitude = 34.77
        )

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertEquals(32.08, result.coastalLocation.latitude, 0.001)
        assertEquals(34.77, result.coastalLocation.longitude, 0.001)
    }

    // endregion

    // region edge cases

    @Test
    fun `mapToWeatherData handles empty todayRemaining list`() {
        val apiData = createCombinedWeatherData(todayRemaining = emptyList())

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertTrue(result.todayRemaining.isEmpty())
    }

    @Test
    fun `mapToWeatherData handles empty weekData list`() {
        val apiData = createCombinedWeatherData(weekData = emptyList())

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertTrue(result.weekData.isEmpty())
    }

    @Test
    fun `mapToWeatherData handles zero values for current conditions`() {
        val apiData = createCombinedWeatherData(
            currentWaveHeight = 0.0,
            currentTemp = 0.0,
            currentCloudCover = 0,
            currentWindSpeed = 0.0,
            currentWindDirection = 0,
            currentWaveDirection = 0,
            currentWavePeriod = 0.0,
            currentSwellHeight = 0.0,
            currentSwellDirection = 0,
            currentSwellPeriod = 0.0,
            currentSeaTemp = 0.0,
            currentUvIndex = 0.0
        )

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        val current = result.currentConditions
        assertEquals(WaveCategory.FLAT, current.waveCategory)
        assertEquals(0.0, current.waveHeightMeters, 0.001)
        assertEquals(0.0, current.temperatureCelsius, 0.001)
        assertEquals(CloudCoverLevel.CLEAR, current.cloudCover)
        assertEquals(0.0, current.windSpeedKmh, 0.001)
        assertEquals(0, current.windDirectionDegrees)
        assertEquals(0.0, current.uvIndex, 0.001)
    }

    @Test
    fun `mapToHourlyWaveForecast maps zero wave height to FLAT`() {
        val apiHourly = HourlyForecast(
            time = "2026-02-06T00:00",
            waveHeight = 0.0,
            waveCategory = "Flat"
        )

        val result = WeatherDataMapper.mapToHourlyWaveForecast(apiHourly)

        assertEquals(WaveCategory.FLAT, result.waveCategory)
        assertEquals(0.0, result.waveHeightMeters, 0.001)
    }

    @Test
    fun `mapToPeriodForecast maps zero wave height to FLAT`() {
        val period = TimePeriod(
            label = "Morning",
            hours = "06:00-11:00",
            avgWaveHeight = 0.0,
            category = "Flat",
            avgTemp = 0.0,
            avgCloudCover = 0
        )

        val result = WeatherDataMapper.mapToPeriodForecast(period)

        assertEquals(WaveCategory.FLAT, result.avgWaveCategory)
        assertEquals(0.0, result.avgWaveHeightMeters, 0.001)
        assertEquals(CloudCoverLevel.CLEAR, result.avgCloudCover)
    }

    @Test
    fun `mapToWeatherData result is not null`() {
        val apiData = createCombinedWeatherData()

        val result = WeatherDataMapper.mapToWeatherData(apiData)

        assertNotNull(result)
        assertNotNull(result.currentConditions)
        assertNotNull(result.todayRemaining)
        assertNotNull(result.weekData)
        assertNotNull(result.tomorrowForecast)
        assertNotNull(result.coastalLocation)
    }

    // endregion

    // region mapToTideEvents

    @Test
    fun `mapToTideEvents maps high and low types correctly`() {
        val dtos = listOf(
            TideEventDto(time = "2026-02-06T06:30", height = 1.5f, type = "high"),
            TideEventDto(time = "2026-02-06T12:45", height = 0.3f, type = "low")
        )

        val result = WeatherDataMapper.mapToTideEvents(dtos)

        assertEquals(2, result.size)
        assertEquals(TideType.HIGH, result[0].type)
        assertEquals(1.5f, result[0].height)
        assertEquals("2026-02-06T06:30", result[0].time)
        assertEquals(TideType.LOW, result[1].type)
        assertEquals(0.3f, result[1].height)
    }

    @Test
    fun `mapToTideEvents returns empty for null input`() {
        val result = WeatherDataMapper.mapToTideEvents(null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mapToTideEvents returns empty for empty list`() {
        val result = WeatherDataMapper.mapToTideEvents(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mapToTideEvents filters out malformed type strings`() {
        val dtos = listOf(
            TideEventDto(time = "2026-02-06T06:30", height = 1.5f, type = "high"),
            TideEventDto(time = "2026-02-06T09:00", height = 0.8f, type = "invalid"),
            TideEventDto(time = "2026-02-06T12:45", height = 0.3f, type = "low")
        )

        val result = WeatherDataMapper.mapToTideEvents(dtos)

        assertEquals(2, result.size)
        assertEquals(TideType.HIGH, result[0].type)
        assertEquals(TideType.LOW, result[1].type)
    }

    @Test
    fun `mapToTideEvents handles case-insensitive type strings`() {
        val dtos = listOf(
            TideEventDto(time = "2026-02-06T06:30", height = 1.5f, type = "HIGH"),
            TideEventDto(time = "2026-02-06T12:45", height = 0.3f, type = "Low")
        )

        val result = WeatherDataMapper.mapToTideEvents(dtos)

        assertEquals(2, result.size)
        assertEquals(TideType.HIGH, result[0].type)
        assertEquals(TideType.LOW, result[1].type)
    }

    @Test
    fun `mapToTideEvents sorts by time`() {
        val dtos = listOf(
            TideEventDto(time = "2026-02-06T18:00", height = 1.2f, type = "high"),
            TideEventDto(time = "2026-02-06T06:00", height = 0.5f, type = "low"),
            TideEventDto(time = "2026-02-06T12:00", height = 1.0f, type = "high")
        )

        val result = WeatherDataMapper.mapToTideEvents(dtos)

        assertEquals(3, result.size)
        assertEquals("2026-02-06T06:00", result[0].time)
        assertEquals("2026-02-06T12:00", result[1].time)
        assertEquals("2026-02-06T18:00", result[2].time)
    }

    // endregion

    // region helpers

    private fun createCombinedWeatherData(
        currentWaveHeight: Double = 1.0,
        currentTemp: Double = 25.0,
        currentCloudCover: Int = 30,
        currentWindSpeed: Double = 10.0,
        currentWindDirection: Int = 180,
        currentWaveDirection: Int = 270,
        currentWavePeriod: Double = 8.0,
        currentSwellHeight: Double = 0.8,
        currentSwellDirection: Int = 250,
        currentSwellPeriod: Double = 9.0,
        currentSeaTemp: Double = 22.0,
        currentUvIndex: Double = 4.0,
        todayRemaining: List<HourlyForecast> = emptyList(),
        weekData: List<HourlyForecast> = emptyList(),
        coastalLatitude: Double = 32.0,
        coastalLongitude: Double = 34.0
    ): CombinedWeatherData {
        return CombinedWeatherData(
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
            hourlyForecast = emptyList(),
            todayRemaining = todayRemaining,
            weekData = weekData,
            tomorrowForecast = TomorrowForecast(
                morning = TimePeriod(
                    label = "Morning",
                    hours = "06:00-11:00",
                    avgWaveHeight = 1.0,
                    category = "Waist",
                    avgTemp = 22.0,
                    avgCloudCover = 20
                ),
                noon = TimePeriod(
                    label = "Noon",
                    hours = "12:00-17:00",
                    avgWaveHeight = 1.5,
                    category = "Chest",
                    avgTemp = 28.0,
                    avgCloudCover = 40
                ),
                evening = TimePeriod(
                    label = "Evening",
                    hours = "18:00-23:00",
                    avgWaveHeight = 0.7,
                    category = "Knee",
                    avgTemp = 24.0,
                    avgCloudCover = 60
                )
            ),
            coastalLatitude = coastalLatitude,
            coastalLongitude = coastalLongitude
        )
    }

    // endregion
}
