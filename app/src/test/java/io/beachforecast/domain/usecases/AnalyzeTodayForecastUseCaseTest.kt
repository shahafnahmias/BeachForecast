package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for analyzing today's forecast
 */
class AnalyzeTodayForecastUseCaseTest {

    private lateinit var useCase: AnalyzeTodayForecastUseCase

    @Before
    fun setup() {
        useCase = AnalyzeTodayForecastUseCase()
    }

    @Test
    fun `isAllDayConstant with empty list returns true`() {
        val result = useCase.isAllDayConstant(WaveCategory.ANKLE, emptyList())
        assertTrue(result)
    }

    @Test
    fun `isAllDayConstant with all same category returns true`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T14:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.45,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T15:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.50,
                temperatureCelsius = 21.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T16:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.48,
                temperatureCelsius = 22.0,
                cloudCover = CloudCoverLevel.PARTLY_CLOUDY
            )
        )

        val result = useCase.isAllDayConstant(WaveCategory.ANKLE, forecast)
        assertTrue(result)
    }

    @Test
    fun `isAllDayConstant with different categories returns false`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T14:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.45,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T15:00",
                waveCategory = WaveCategory.KNEE,  // Different
                waveHeightMeters = 0.75,
                temperatureCelsius = 21.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        val result = useCase.isAllDayConstant(WaveCategory.ANKLE, forecast)
        assertFalse(result)
    }

    @Test
    fun `isAllDayConstant with current not matching rest returns false`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T14:00",
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.75,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            ),
            HourlyWaveForecast(
                time = "2024-01-15T15:00",
                waveCategory = WaveCategory.KNEE,
                waveHeightMeters = 0.80,
                temperatureCelsius = 21.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        val result = useCase.isAllDayConstant(WaveCategory.ANKLE, forecast)
        assertFalse(result)
    }

    @Test
    fun `areSameCategory with same enum returns true`() {
        val result = useCase.areSameCategory(WaveCategory.ANKLE, WaveCategory.ANKLE)
        assertTrue(result)
    }

    @Test
    fun `areSameCategory with different enum returns false`() {
        val result = useCase.areSameCategory(WaveCategory.ANKLE, WaveCategory.KNEE)
        assertFalse(result)
    }

    @Test
    fun `isAllDayConstant handles single item correctly`() {
        val forecast = listOf(
            HourlyWaveForecast(
                time = "2024-01-15T14:00",
                waveCategory = WaveCategory.ANKLE,
                waveHeightMeters = 0.45,
                temperatureCelsius = 20.0,
                cloudCover = CloudCoverLevel.CLEAR
            )
        )

        val result = useCase.isAllDayConstant(WaveCategory.ANKLE, forecast)
        assertTrue(result)
    }
}
