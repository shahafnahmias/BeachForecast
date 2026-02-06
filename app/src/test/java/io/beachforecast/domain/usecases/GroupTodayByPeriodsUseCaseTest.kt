package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GroupTodayByPeriodsUseCaseTest {

    private lateinit var useCase: GroupTodayByPeriodsUseCase

    @Before
    fun setup() {
        useCase = GroupTodayByPeriodsUseCase()
    }

    @Test
    fun `empty list returns empty result`() {
        val result = useCase.execute(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groups hours into 3-hour periods`() {
        val forecast = createFullDayForecast()
        val result = useCase.execute(forecast)

        // Should produce periods (depends on current time vs sunset cutoff=19)
        val now = LocalDateTime.now()
        if (now.hour < 19) {
            assertTrue("Should produce at least one period when before sunset", result.isNotEmpty())

            // Each period should span up to 3 hours
            result.forEach { period ->
                assertTrue(
                    "Period should span at most 3 hours, got ${period.endHour - period.startHour}",
                    period.endHour - period.startHour <= 3
                )
            }

            // Max 5 periods
            assertTrue("Should produce at most 5 periods", result.size <= 5)
        }
    }

    @Test
    fun `first period is labeled Now`() {
        val forecast = createFullDayForecast()
        val result = useCase.execute(forecast)

        val now = LocalDateTime.now()
        if (now.hour < 19 && result.isNotEmpty()) {
            assertEquals("Now", result.first().timeLabel)

            // Non-first periods should have HH:00 format
            result.drop(1).forEach { period ->
                assertTrue(
                    "Non-first period should have HH:00 format, got ${period.timeLabel}",
                    period.timeLabel.matches(Regex("\\d{2}:00"))
                )
            }
        }
    }

    @Test
    fun `returns WaveCategory enum not String`() {
        val forecast = createFullDayForecast()
        val result = useCase.execute(forecast)

        val now = LocalDateTime.now()
        if (now.hour < 19 && result.isNotEmpty()) {
            // Verify waveCategory is a WaveCategory enum value
            result.forEach { period ->
                assertNotNull("waveCategory should not be null", period.waveCategory)
                // Verify it matches what fromHeight would return
                assertEquals(
                    WaveCategory.fromHeight(period.avgWaveHeight),
                    period.waveCategory
                )
            }
        }
    }

    /**
     * Creates forecast data covering all 24 hours of today.
     * Uses today's date so the use case can match hours regardless of when tests run.
     */
    private fun createFullDayForecast(): List<HourlyWaveForecast> {
        val today = LocalDate.now()
        return (0..23).map { hour ->
            HourlyWaveForecast(
                time = "${today}T${String.format("%02d", hour)}:00",
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.8,
                temperatureCelsius = 22.0,
                cloudCover = CloudCoverLevel.CLEAR,
                windSpeedKmh = 15.0,
                windDirectionDegrees = 180,
                swellHeightMeters = 0.5,
                swellDirectionDegrees = 270,
                swellPeriodSeconds = 8.0,
                wavePeriodSeconds = 6.0,
                uvIndex = 5.0
            )
        }
    }
}
