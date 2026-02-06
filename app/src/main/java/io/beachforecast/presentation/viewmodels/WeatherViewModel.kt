package io.beachforecast.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.domain.usecases.GetCurrentWeatherUseCase
import io.beachforecast.presentation.models.WeatherUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Thin ViewModel - delegates all business logic to use cases
 * Only responsible for:
 * - Managing UI state
 * - Coordinating coroutines/lifecycle
 * - Delegating to use cases
 *
 * Now fully testable with dependency injection
 */
class WeatherViewModel(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeatherData()
        // Reload data when unit system changes
        viewModelScope.launch {
            userPreferences.unitSystemFlow
                .drop(1) // Skip initial value to avoid double load
                .collect {
                    loadWeatherData()
                }
        }
        // Reload data when beach selection changes
        viewModelScope.launch {
            userPreferences.beachSelectionFlow
                .drop(1) // Skip initial value to avoid double load
                .collect {
                    loadWeatherData()
                }
        }
    }

    /**
     * Load weather data - delegates everything to use case
     * Reads current unit system and passes it to use case
     */
    fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val unitSystem = userPreferences.getUnitSystem()
            when (val result = getCurrentWeatherUseCase.execute(unitSystem)) {
                is WeatherResult.Success -> {
                    _uiState.value = WeatherUiState.Success(result.data)
                }
                is WeatherResult.Error -> {
                    _uiState.value = WeatherUiState.Error(result.error)
                }
                else -> {
                    // Loading state already set
                }
            }
        }
    }

    /**
     * Refresh - just reload data
     */
    fun refresh() {
        loadWeatherData()
    }
}
