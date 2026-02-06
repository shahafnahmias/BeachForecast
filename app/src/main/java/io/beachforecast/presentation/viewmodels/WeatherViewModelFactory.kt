package io.beachforecast.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.domain.usecases.GetCurrentWeatherUseCase
import io.beachforecast.domain.usecases.GetSelectedBeachUseCase

/**
 * Factory for creating WeatherViewModel with dependencies
 * Handles dependency injection for the ViewModel
 */
class WeatherViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            // Create dependencies
            val userPreferences = UserPreferences(context)
            val locationProvider = LocationProvider(context)
            val beachRepository = BeachRepository(context)
            val weatherRepository = WeatherRepository()

            // Get language code synchronously from SharedPreferences
            val languageCode = userPreferences.getAppLanguageSync()

            // Create GetSelectedBeachUseCase (centralized beach selection logic)
            val getSelectedBeachUseCase = GetSelectedBeachUseCase(
                beachRepository = beachRepository,
                locationProvider = locationProvider,
                userPreferences = userPreferences,
                findClosestBeachUseCase = FindClosestBeachUseCase()
            )

            // Create GetCurrentWeatherUseCase with beach selection
            val getCurrentWeatherUseCase = GetCurrentWeatherUseCase(
                getSelectedBeachUseCase = getSelectedBeachUseCase,
                weatherRepository = weatherRepository,
                languageCode = languageCode
            )

            // Create ViewModel with dependencies
            return WeatherViewModel(getCurrentWeatherUseCase, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
