package io.beachforecast.domain.usecases

import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.models.WeatherResult

/**
 * Use case for getting all data needed to display the widget
 * Encapsulates all widget business logic for testability
 */
class GetWidgetDataUseCase(
    private val getSelectedBeachUseCase: GetSelectedBeachUseCase,
    private val weatherRepository: WeatherRepository,
    private val languageCode: String = "en"
) {

    /**
     * Execute the use case to get widget display data
     * @return WeatherResult with WidgetData or Error
     */
    suspend fun execute(): WeatherResult<WidgetData> {
        // Step 1: Get selected beach (replaces direct GPS location)
        val beach = when (val beachResult = getSelectedBeachUseCase.execute()) {
            is WeatherResult.Success -> beachResult.data
            is WeatherResult.Error -> return WeatherResult.Error(beachResult.error)
            is WeatherResult.Loading -> return WeatherResult.Loading
        }

        // Step 2: Use beach name directly (no geocoding needed)
        val beachName = beach.getLocalizedName(languageCode)

        // Step 3: Get weather data using beach coordinates
        val weatherData = when (val weatherResult = weatherRepository.getAllWeatherData(beach.latitude, beach.longitude)) {
            is WeatherResult.Success -> weatherResult.data
            is WeatherResult.Error -> return WeatherResult.Error(weatherResult.error)
            is WeatherResult.Loading -> return WeatherResult.Loading
        }

        // Step 4: Package all data for widget
        return WeatherResult.Success(
            WidgetData(
                cityName = beachName,
                weatherData = weatherData
            )
        )
    }

    /**
     * Data class containing all information needed to render the widget
     */
    data class WidgetData(
        val cityName: String,
        val weatherData: WeatherRepository.WeatherData
    )
}
