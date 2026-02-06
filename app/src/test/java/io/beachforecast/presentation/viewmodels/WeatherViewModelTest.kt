package io.beachforecast.presentation.viewmodels

import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.domain.models.*
import io.beachforecast.domain.usecases.GetCurrentWeatherUseCase
import io.beachforecast.presentation.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private lateinit var mockGetCurrentWeatherUseCase: GetCurrentWeatherUseCase
    private lateinit var mockUserPreferences: UserPreferences
    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetCurrentWeatherUseCase = mock()
        mockUserPreferences = mock()

        // Mock UserPreferences to return METRIC by default
        whenever(mockUserPreferences.unitSystemFlow).thenReturn(flowOf(UnitSystem.METRIC))
        whenever(mockUserPreferences.beachSelectionFlow).thenReturn(flowOf(BeachSelection.Auto))
        runTest {
            whenever(mockUserPreferences.getUnitSystem()).thenReturn(UnitSystem.METRIC)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial`() = runTest {
        // Given/When
        viewModel = WeatherViewModel(mockGetCurrentWeatherUseCase, mockUserPreferences)

        // Then
        assertTrue(viewModel.uiState.value is WeatherUiState.Initial)
    }

    @Test
    fun `loadWeatherData sets Success state when use case succeeds`() = runTest {
        // Given
        val mockWeatherData: WeatherUiData = mock()
        whenever(mockGetCurrentWeatherUseCase.execute(any())).thenReturn(
            WeatherResult.Success(mockWeatherData)
        )

        // When
        viewModel = WeatherViewModel(mockGetCurrentWeatherUseCase, mockUserPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
        val successState = viewModel.uiState.value as WeatherUiState.Success
        assertEquals(mockWeatherData, successState.data)
    }

    @Test
    fun `loadWeatherData sets Error state when use case fails`() = runTest {
        // Given
        val error = WeatherError.NoInternet
        whenever(mockGetCurrentWeatherUseCase.execute(any())).thenReturn(
            WeatherResult.Error(error)
        )

        // When
        viewModel = WeatherViewModel(mockGetCurrentWeatherUseCase, mockUserPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is WeatherUiState.Error)
        val errorState = viewModel.uiState.value as WeatherUiState.Error
        assertEquals(error, errorState.error)
    }

    @Test
    fun `refresh executes use case again`() = runTest {
        // Given
        val mockWeatherData: WeatherUiData = mock()
        whenever(mockGetCurrentWeatherUseCase.execute(any())).thenReturn(
            WeatherResult.Success(mockWeatherData)
        )
        viewModel = WeatherViewModel(mockGetCurrentWeatherUseCase, mockUserPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(mockGetCurrentWeatherUseCase, times(2)).execute(any())
    }
}
