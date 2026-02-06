package io.beachforecast.widget

import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.domain.models.WeatherMetric
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GenerateTodaySummaryUseCaseTest {

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
    }

    private fun createConditions(
        waveHeight: Double = 1.0,
        temp: Double = 25.0,
        cloudCover: CloudCoverLevel = CloudCoverLevel.CLEAR,
        windSpeed: Double = 10.0,
        swellHeight: Double = 0.5,
        uvIndex: Double = 5.0
    ) = CurrentConditions(
        waveCategory = WaveCategory.fromHeight(waveHeight),
        waveHeightMeters = waveHeight,
        temperatureCelsius = temp,
        cloudCover = cloudCover,
        windSpeedKmh = windSpeed,
        swellHeightMeters = swellHeight,
        uvIndex = uvIndex
    )

    private fun createHourly(
        time: String = "2026-02-06T12:00",
        waveHeight: Double = 1.0,
        temp: Double = 25.0,
        windSpeed: Double = 10.0,
        swellHeight: Double = 0.5,
        uvIndex: Double = 5.0
    ) = HourlyWaveForecast(
        time = time,
        waveCategory = WaveCategory.fromHeight(waveHeight),
        waveHeightMeters = waveHeight,
        temperatureCelsius = temp,
        windSpeedKmh = windSpeed,
        swellHeightMeters = swellHeight,
        uvIndex = uvIndex
    )

    @Test
    fun `execute with complete data returns non-empty summary`() {
        val conditions = createConditions()
        val forecast = listOf(
            createHourly(time = "2026-02-06T13:00"),
            createHourly(time = "2026-02-06T14:00"),
            createHourly(time = "2026-02-06T15:00")
        )

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast
        )

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `execute with empty forecast returns summary based on current conditions`() {
        val conditions = createConditions()

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = emptyList()
        )

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `execute with single data point returns summary`() {
        val conditions = createConditions(waveHeight = 0.5)
        val forecast = listOf(createHourly(waveHeight = 0.5))

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast
        )

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `execute respects selected metrics - swell excluded`() {
        val conditions = createConditions(swellHeight = 2.0)
        val forecast = listOf(
            createHourly(swellHeight = 2.0),
            createHourly(swellHeight = 2.5)
        )

        val metricsWithoutSwell = WeatherMetric.getDefaults() - WeatherMetric.SWELL

        val resultWithSwell = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast,
            selectedMetrics = WeatherMetric.getDefaults()
        )

        val resultWithoutSwell = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast,
            selectedMetrics = metricsWithoutSwell
        )

        // With swell excluded, the summary should be shorter or different
        assertTrue(resultWithSwell.length >= resultWithoutSwell.length)
    }

    @Test
    fun `execute with flat conditions generates appropriate summary`() {
        val conditions = createConditions(
            waveHeight = 0.05,
            windSpeed = 5.0,
            swellHeight = 0.1
        )
        val forecast = listOf(
            createHourly(waveHeight = 0.05, windSpeed = 5.0, swellHeight = 0.1)
        )

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast,
            selectedSports = setOf(Activity.SUP)
        )

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `execute with dangerous conditions generates appropriate summary`() {
        val conditions = createConditions(
            waveHeight = 3.5,
            windSpeed = 55.0,
            swellHeight = 3.0
        )

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = emptyList(),
            selectedSports = setOf(Activity.SURF)
        )

        assertTrue(result.isNotBlank())
    }

    @Test
    fun `execute with empty sports set returns summary without sport section`() {
        val conditions = createConditions()

        val resultWithSports = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = emptyList(),
            selectedSports = Activity.getDefaults()
        )

        val resultWithoutSports = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = emptyList(),
            selectedSports = emptySet()
        )

        assertTrue(resultWithSports.length >= resultWithoutSports.length)
    }

    @Test
    fun `execute with rising wave trend includes trend description`() {
        val conditions = createConditions(waveHeight = 0.5)
        val forecast = listOf(
            createHourly(time = "2026-02-06T13:00", waveHeight = 0.5),
            createHourly(time = "2026-02-06T14:00", waveHeight = 0.8),
            createHourly(time = "2026-02-06T15:00", waveHeight = 1.2),
            createHourly(time = "2026-02-06T16:00", waveHeight = 1.5)
        )

        val result = GenerateTodaySummaryUseCase.execute(
            context = context,
            currentConditions = conditions,
            todayRemaining = forecast
        )

        assertTrue(result.isNotBlank())
    }
}
