package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * THIS IS WHERE THE BUG WAS!
 * These tests would have caught the crash from substring on short strings
 */
class GroupForecastByDaysUseCaseTest {

    private lateinit var useCase: GroupForecastByDaysUseCase

    @Before
    fun setup() {
        useCase = GroupForecastByDaysUseCase()
    }

    // ========== Edge Cases - THE BUG ==========

    @Test
    fun `execute with empty list returns empty`() {
        val result = useCase.execute(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute with short time format does not crash`() {
        // THIS WOULD HAVE CAUGHT THE BUG!
        val forecast = listOf(
            HourlyWaveForecast(
                time = "14:00",  // Short format - would crash with substring(0, 10)
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        // Should not crash, should filter out invalid timestamps
        val result = useCase.execute(forecast)
        assertTrue(result.isEmpty())  // Invalid timestamps filtered out
    }

    @Test
    fun `execute with invalid timestamp format returns empty`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "invalid",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        val result = useCase.execute(forecast)
        assertTrue(result.isEmpty())
    }

    // ========== Valid Data Tests ==========

    @Test
    fun `execute with valid ISO timestamps groups by date`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T08:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 18.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T14:00",
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.8,
                temperatureCelsius = 22.0,
                cloudCover = CloudCoverLevel.PARTLY_CLOUDY
            ),
            HourlyWaveForecast(
                time = "2024-01-16T08:00",
                waveCategory = WaveCategory.WAIST,
                waveHeightMeters = 1.2,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLOUDY
            )
        )

        val result = useCase.execute(forecast)

        assertEquals(2, result.size)  // 2 days
        assertEquals("2024-01-15", result[0].date)
        assertEquals("2024-01-16", result[1].date)
    }

    @Test
    fun `execute groups hours into morning afternoon evening periods`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T08:00",  // Morning
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 18.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T14:00",  // Afternoon
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.8,
                temperatureCelsius = 22.0,
                cloudCover = CloudCoverLevel.PARTLY_CLOUDY
            ),
            HourlyWaveForecast(
                time = "2024-01-15T18:00",  // Evening
                waveCategory = WaveCategory.WAIST,
                waveHeightMeters = 1.2,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLOUDY
            )
        )

        val result = useCase.execute(forecast)

        assertEquals(1, result.size)
        val day = result[0]

        // Morning has data
        assertEquals(WaveCategory.ANKLE, day.morningConditions.waveCategory)

        // Afternoon has data
        assertEquals(WaveCategory.KNEE, day.afternoonConditions.waveCategory)

        // Evening has data
        assertEquals(WaveCategory.WAIST, day.eveningConditions.waveCategory)
    }

    @Test
    fun `execute with empty period uses default values`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T08:00",  // Only morning
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 18.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        val result = useCase.execute(forecast)

        assertEquals(1, result.size)
        val day = result[0]

        // Morning has data
        assertEquals(WaveCategory.ANKLE, day.morningConditions.waveCategory)

        // Afternoon empty - uses defaults
        assertEquals(WaveCategory.FLAT, day.afternoonConditions.waveCategory)
        assertEquals(0.0, day.afternoonConditions.waveHeight, 0.01)

        // Evening empty - uses defaults
        assertEquals(WaveCategory.FLAT, day.eveningConditions.waveCategory)
        assertEquals(0.0, day.eveningConditions.waveHeight, 0.01)
    }

    @Test
    fun `execute limits results to 7 days`() {
        val forecast = (0..9).flatMap { day ->
            listOf(
                HourlyWaveForecast(
                    time = "2024-01-${15 + day}T08:00",
                    waveCategory = WaveCategory.ANKLE,
                    waveHeightMeters = 0.5,
                    temperatureCelsius = 18.0,
                    cloudCover = CloudCoverLevel.CLEAR
                )
            )
        }

        val result = useCase.execute(forecast)

        assertEquals(7, result.size)  // Limited to 7 days
    }

    @Test
    fun `execute calculates average conditions for period`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T08:00",  // Morning
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.4,
                temperatureCelsius = 18.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T09:00",  // Morning
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.6,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.PARTLY_CLOUDY
            )
        )

        val result = useCase.execute(forecast)

        assertEquals(1, result.size)
        val morningConditions = result[0].morningConditions

        // Average wave height
        assertEquals(0.5, morningConditions.waveHeight, 0.01)

        // Average temperature
        assertEquals(19.0, morningConditions.temperature, 0.01)
    }

    @Test
    fun `execute with mixed valid and invalid timestamps filters correctly`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T08:00",  // Valid
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.5,
                temperatureCelsius = 18.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "invalid",  // Invalid
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.8,
                temperatureCelsius = 22.0,
                cloudCover = CloudCoverLevel.PARTLY_CLOUDY
            ),
            HourlyWaveForecast(
                time = "2024-01-15T14:00",  // Valid
                waveCategory = WaveCategory.WAIST,
                waveHeightMeters = 1.2,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLOUDY
            )
        )

        val result = useCase.execute(forecast)

        assertEquals(1, result.size)  // Only valid timestamps grouped
        // Should have morning and afternoon, but not the invalid entry
    }
}
