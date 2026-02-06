package io.beachforecast.domain.usecases

import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.models.Beach
import io.beachforecast.domain.models.WeatherError
import io.beachforecast.domain.models.WeatherResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetWidgetDataUseCaseTest {

    private lateinit var mockGetSelectedBeachUseCase: GetSelectedBeachUseCase
    private lateinit var mockWeatherRepository: WeatherRepository
    private lateinit var useCase: GetWidgetDataUseCase

    @Before
    fun setup() {
        mockGetSelectedBeachUseCase = mock()
        mockWeatherRepository = mock()
        useCase = GetWidgetDataUseCase(
            getSelectedBeachUseCase = mockGetSelectedBeachUseCase,
            weatherRepository = mockWeatherRepository,
            languageCode = "en"
        )
    }

    private fun createTestBeach(): Beach {
        return Beach(
            id = "tel_aviv_hilton",
            nameHe = "חוף הילטון תל אביב",
            nameEn = "Tel Aviv Hilton Beach",
            latitude = 32.0853,
            longitude = 34.7818
        )
    }

    @Test
    fun `execute returns success with all data when everything succeeds`() = runTest {
        // Given
        val beach = createTestBeach()
        val mockWeatherData: WeatherRepository.WeatherData = mock()

        whenever(mockGetSelectedBeachUseCase.execute()).thenReturn(WeatherResult.Success(beach))
        whenever(mockWeatherRepository.getAllWeatherData(beach.latitude, beach.longitude))
            .thenReturn(WeatherResult.Success(mockWeatherData))

        // When
        val result = useCase.execute()

        // Then
        assertTrue(result.isSuccess())
        val widgetData = result.getOrNull()
        assertNotNull(widgetData)
        assertEquals("Tel Aviv Hilton Beach", widgetData!!.cityName)
        assertEquals(mockWeatherData, widgetData.weatherData)
    }

    @Test
    fun `execute returns error when beach selection fails`() = runTest {
        // Given
        val error = WeatherError.LocationUnavailable
        whenever(mockGetSelectedBeachUseCase.execute()).thenReturn(WeatherResult.Error(error))

        // When
        val result = useCase.execute()

        // Then
        assertTrue(result.isError())
        assertEquals(error, result.errorOrNull())
        verify(mockWeatherRepository, never()).getAllWeatherData(any(), any())
    }

    @Test
    fun `execute returns error when weather data fails`() = runTest {
        // Given
        val beach = createTestBeach()
        val error = WeatherError.NoInternet

        whenever(mockGetSelectedBeachUseCase.execute()).thenReturn(WeatherResult.Success(beach))
        whenever(mockWeatherRepository.getAllWeatherData(beach.latitude, beach.longitude))
            .thenReturn(WeatherResult.Error(error))

        // When
        val result = useCase.execute()

        // Then
        assertTrue(result.isError())
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun `execute uses Hebrew beach name when language is Hebrew`() = runTest {
        // Given
        val beach = createTestBeach()
        val mockWeatherData: WeatherRepository.WeatherData = mock()
        val hebrewUseCase = GetWidgetDataUseCase(
            getSelectedBeachUseCase = mockGetSelectedBeachUseCase,
            weatherRepository = mockWeatherRepository,
            languageCode = "he"
        )

        whenever(mockGetSelectedBeachUseCase.execute()).thenReturn(WeatherResult.Success(beach))
        whenever(mockWeatherRepository.getAllWeatherData(beach.latitude, beach.longitude))
            .thenReturn(WeatherResult.Success(mockWeatherData))

        // When
        val result = hebrewUseCase.execute()

        // Then
        assertTrue(result.isSuccess())
        val widgetData = result.getOrNull()
        assertNotNull(widgetData)
        assertEquals("חוף הילטון תל אביב", widgetData!!.cityName)
    }

    @Test
    fun `execute calls all dependencies in correct order`() = runTest {
        // Given
        val beach = createTestBeach()
        val mockWeatherData: WeatherRepository.WeatherData = mock()

        whenever(mockGetSelectedBeachUseCase.execute()).thenReturn(WeatherResult.Success(beach))
        whenever(mockWeatherRepository.getAllWeatherData(beach.latitude, beach.longitude))
            .thenReturn(WeatherResult.Success(mockWeatherData))

        // When
        useCase.execute()

        // Then - verify order of calls
        val inOrder = inOrder(mockGetSelectedBeachUseCase, mockWeatherRepository)
        inOrder.verify(mockGetSelectedBeachUseCase).execute()
        inOrder.verify(mockWeatherRepository).getAllWeatherData(beach.latitude, beach.longitude)
    }
}
