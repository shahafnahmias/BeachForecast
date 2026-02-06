package io.beachforecast.domain.calculators

import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.CloudCoverLevel
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.presentation.models.PeriodConditionsUiData
import org.junit.Assert.*
import org.junit.Test

class BestDayCalculatorTest {

    // region calculateDayAverageConditions

    @Test
    fun `calculateDayAverageConditions averages three periods`() {
        val day = createDay(
            morningWave = 1.0, morningTemp = 20.0,
            afternoonWave = 2.0, afternoonTemp = 25.0,
            eveningWave = 1.5, eveningTemp = 22.0
        )

        val result = BestDayCalculator.calculateDayAverageConditions(day)

        assertEquals(1.5, result.waveHeight, 0.001)
        assertEquals(WaveCategory.CHEST, result.waveCategory)
        assertEquals(22.333, result.temperature, 0.01)
    }

    @Test
    fun `calculateDayAverageConditions handles single valid period`() {
        val day = createDay(
            morningWave = 1.0, morningTemp = 20.0,
            afternoonWave = 0.0, afternoonTemp = 0.0,
            eveningWave = 0.0, eveningTemp = 0.0
        )

        val result = BestDayCalculator.calculateDayAverageConditions(day)

        assertEquals(1.0, result.waveHeight, 0.001)
        assertEquals(20.0, result.temperature, 0.001)
    }

    @Test
    fun `calculateDayAverageConditions returns flat for all empty periods`() {
        val day = createDay(
            morningWave = 0.0, morningTemp = 0.0,
            afternoonWave = 0.0, afternoonTemp = 0.0,
            eveningWave = 0.0, eveningTemp = 0.0
        )

        val result = BestDayCalculator.calculateDayAverageConditions(day)

        assertEquals(WaveCategory.FLAT, result.waveCategory)
        assertEquals(0.0, result.waveHeight, 0.001)
        assertEquals(0.0, result.temperature, 0.001)
    }

    // endregion

    // region findBestDayPerSport

    @Test
    fun `findBestDayPerSport returns empty for empty week`() {
        val result = BestDayCalculator.findBestDayPerSport(
            emptyList(),
            setOf(Activity.SURF)
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findBestDayPerSport returns empty for empty sports`() {
        val result = BestDayCalculator.findBestDayPerSport(
            listOf(createDay(morningWave = 1.0, morningTemp = 25.0)),
            emptySet()
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findBestDayPerSport picks highest wave day for surf`() {
        val week = listOf(
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 10.0),
            createDay(morningWave = 1.5, morningTemp = 25.0, morningWind = 10.0),
            createDay(morningWave = 1.0, morningTemp = 25.0, morningWind = 10.0)
        )

        val result = BestDayCalculator.findBestDayPerSport(week, setOf(Activity.SURF))

        assertEquals(1, result[Activity.SURF])
    }

    @Test
    fun `findBestDayPerSport picks lowest wave day for swim`() {
        val week = listOf(
            createDay(morningWave = 0.8, morningTemp = 25.0, morningWind = 10.0),
            createDay(morningWave = 0.3, morningTemp = 25.0, morningWind = 10.0),
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 10.0)
        )

        val result = BestDayCalculator.findBestDayPerSport(week, setOf(Activity.SWIM))

        assertEquals(1, result[Activity.SWIM])
    }

    @Test
    fun `findBestDayPerSport picks windiest day for kite`() {
        val week = listOf(
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 10.0),
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 30.0),
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 20.0)
        )

        val result = BestDayCalculator.findBestDayPerSport(week, setOf(Activity.KITE))

        assertEquals(1, result[Activity.KITE])
    }

    @Test
    fun `findBestDayPerSport handles multiple sports`() {
        val week = listOf(
            createDay(morningWave = 0.3, morningTemp = 25.0, morningWind = 5.0),  // best swim + SUP
            createDay(morningWave = 1.5, morningTemp = 25.0, morningWind = 10.0),  // best surf
            createDay(morningWave = 0.5, morningTemp = 25.0, morningWind = 25.0)   // best kite
        )

        val result = BestDayCalculator.findBestDayPerSport(
            week,
            setOf(Activity.SURF, Activity.SWIM, Activity.KITE)
        )

        assertEquals(1, result[Activity.SURF])
        assertEquals(0, result[Activity.SWIM])
        assertEquals(2, result[Activity.KITE])
    }

    @Test
    fun `findBestDayPerSport excludes sport if no day recommended`() {
        // All days have high waves = not recommended for swim
        val week = listOf(
            createDay(morningWave = 2.0, morningTemp = 25.0, morningWind = 30.0),
            createDay(morningWave = 2.5, morningTemp = 25.0, morningWind = 35.0)
        )

        val result = BestDayCalculator.findBestDayPerSport(week, setOf(Activity.SWIM))

        assertFalse(result.containsKey(Activity.SWIM))
    }

    // endregion

    // region helpers

    private fun createDay(
        morningWave: Double = 0.0,
        morningTemp: Double = 0.0,
        morningWind: Double = 0.0,
        afternoonWave: Double = morningWave,
        afternoonTemp: Double = morningTemp,
        afternoonWind: Double = morningWind,
        eveningWave: Double = morningWave,
        eveningTemp: Double = morningTemp,
        eveningWind: Double = morningWind
    ): DayForecastUiData {
        return DayForecastUiData(
            date = "Feb 6",
            dayName = "Thursday",
            morningConditions = PeriodConditionsUiData(
                waveCategory = WaveCategory.fromHeight(morningWave),
                waveHeight = morningWave,
                temperature = morningTemp,
                cloudCover = CloudCoverLevel.CLEAR,
                windSpeed = morningWind
            ),
            afternoonConditions = PeriodConditionsUiData(
                waveCategory = WaveCategory.fromHeight(afternoonWave),
                waveHeight = afternoonWave,
                temperature = afternoonTemp,
                cloudCover = CloudCoverLevel.CLEAR,
                windSpeed = afternoonWind
            ),
            eveningConditions = PeriodConditionsUiData(
                waveCategory = WaveCategory.fromHeight(eveningWave),
                waveHeight = eveningWave,
                temperature = eveningTemp,
                cloudCover = CloudCoverLevel.CLEAR,
                windSpeed = eveningWind
            )
        )
    }

    // endregion
}
