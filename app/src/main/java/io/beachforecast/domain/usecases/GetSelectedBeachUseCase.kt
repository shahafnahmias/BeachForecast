package io.beachforecast.domain.usecases

import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.domain.models.Beach
import io.beachforecast.domain.models.BeachSelection
import io.beachforecast.domain.models.WeatherResult

/**
 * Centralized use case for getting the currently selected beach
 * Used by BOTH app and widget to ensure consistent behavior
 *
 * Logic:
 * 1. If BeachSelection.Auto: Get GPS location, find closest beach
 * 2. If BeachSelection.Manual: Return the stored beach
 * 3. If no beaches available or location unavailable: Return Tel Aviv as fallback
 */
class GetSelectedBeachUseCase(
    private val beachRepository: BeachRepository,
    private val locationProvider: LocationProvider,
    private val userPreferences: UserPreferences,
    private val findClosestBeachUseCase: FindClosestBeachUseCase = FindClosestBeachUseCase()
) {

    /**
     * Get the currently selected beach based on user preference
     * @param languageCode Language code for fallback beach name (optional)
     * @return WeatherResult with Beach on success, or Error if no beaches available
     */
    suspend fun execute(): WeatherResult<Beach> {
        val beaches = beachRepository.getAllBeaches()
        if (beaches.isEmpty()) {
            return WeatherResult.Error(
                io.beachforecast.domain.models.WeatherError.Unknown(
                    IllegalStateException("No beaches available")
                )
            )
        }

        val selection = userPreferences.getBeachSelection()

        return when (selection) {
            is BeachSelection.Auto -> {
                // Find closest beach to user's GPS
                val locationResult = locationProvider.getLocation()
                when (locationResult) {
                    is WeatherResult.Success -> {
                        val location = locationResult.data
                        val closestBeach = findClosestBeachUseCase.execute(
                            location.latitude,
                            location.longitude,
                            beaches
                        )
                        WeatherResult.Success(closestBeach)
                    }
                    is WeatherResult.Error -> {
                        // Fallback to Tel Aviv (reasonable default for Israel)
                        val defaultBeach = findDefaultBeach(beaches)
                        WeatherResult.Success(defaultBeach)
                    }
                    else -> {
                        val defaultBeach = findDefaultBeach(beaches)
                        WeatherResult.Success(defaultBeach)
                    }
                }
            }
            is BeachSelection.Manual -> {
                val beach = beaches.find { it.id == selection.beachId }
                if (beach != null) {
                    WeatherResult.Success(beach)
                } else {
                    // Beach was deleted or ID changed? Fallback to first
                    WeatherResult.Success(beaches.first())
                }
            }
        }
    }

    /**
     * Get all available beaches for display in picker
     */
    fun getAllBeaches(): List<Beach> {
        return beachRepository.getAllBeaches()
    }

    /**
     * Find a reasonable default beach (Tel Aviv or first in list)
     */
    private fun findDefaultBeach(beaches: List<Beach>): Beach {
        // Try to find Tel Aviv (central, popular beach)
        return beaches.find { it.nameEn.contains("Tel Aviv", ignoreCase = true) }
            ?: beaches.first()
    }
}
