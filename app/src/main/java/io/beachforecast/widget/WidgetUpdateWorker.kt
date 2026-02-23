package io.beachforecast.widget

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.beachforecast.R
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.ConditionRating
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.domain.usecases.GetSelectedBeachUseCase
import io.beachforecast.domain.usecases.GetWidgetDataUseCase
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "widget_update"
    }

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(context)
        val languageCode = userPreferences.getAppLanguage()
        val localizedContext = updateContextLocale(context, languageCode)
        val unitSystem = userPreferences.getUnitSystem()
        val selectedSports = userPreferences.getSelectedSports()

        // Initialize dependencies
        val locationProvider = LocationProvider(context)
        val beachRepository = BeachRepository(context)
        val weatherRepository = WeatherRepository()
        val getSelectedBeachUseCase = GetSelectedBeachUseCase(
            beachRepository = beachRepository,
            locationProvider = locationProvider,
            userPreferences = userPreferences,
            findClosestBeachUseCase = FindClosestBeachUseCase()
        )
        val getWidgetDataUseCase = GetWidgetDataUseCase(
            getSelectedBeachUseCase = getSelectedBeachUseCase,
            weatherRepository = weatherRepository,
            languageCode = languageCode
        )

        try {
            val result = getWidgetDataUseCase.execute()

            if (result is WeatherResult.Error) {
                val errorMessage = localizedContext.getString(result.error.getUserMessageRes())
                writeErrorState(errorMessage)
                return Result.retry()
            }

            val widgetData = result.getOrNull() ?: return Result.retry()
            val cityName = widgetData.cityName
            val weatherData = widgetData.weatherData
            val conditions = weatherData.currentConditions

            // Pre-compute activity recommendations
            val recommendations = ActivityRecommendationCalculator.calculateForSports(
                conditions, selectedSports
            )
            val activityStates = recommendations.recommendations.map { rec ->
                ActivityState(
                    name = localizedContext.getString(rec.activity.nameRes),
                    isRecommended = rec.isRecommended,
                    reason = rec.reason ?: ""
                )
            }

            // Pre-compute daily forecasts (today + 2 days)
            val dayForecasts = computeDayForecasts(
                weatherData.weekData, unitSystem, localizedContext, languageCode
            )

            // Write state to DataStore for all widgets
            writeSuccessState(
                cityName = cityName,
                conditionRating = recommendations.conditionRating.name,
                conditionRatingDisplay = localizedContext.getString(recommendations.conditionRating.nameRes),
                temperatureFormatted = WeatherFormatter.formatTemperature(conditions.temperatureCelsius, unitSystem),
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(conditions.waveHeightMeters, unitSystem),
                windFormatted = WeatherFormatter.formatWindCompact(
                    conditions.windSpeedKmh, conditions.windDirectionDegrees, unitSystem
                ),
                activitiesJson = WidgetStateSerializer.serializeActivities(activityStates),
                dayForecastsJson = WidgetStateSerializer.serializeDayForecasts(dayForecasts)
            )

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Widget update failed")
            writeErrorState(localizedContext.getString(R.string.error_unknown))
            return Result.retry()
        }
    }

    private fun computeDayForecasts(
        weekData: List<HourlyWaveForecast>,
        unitSystem: UnitSystem,
        localizedContext: Context,
        languageCode: String
    ): List<DayForecastState> {
        if (weekData.isEmpty()) return emptyList()

        val locale = if (languageCode == "he") Locale("he") else Locale("en")

        // Group hourly data by date
        val byDate = weekData
            .filter { it.time.length >= 10 }
            .groupBy { it.time.substring(0, 10) }
            .entries
            .sortedBy { it.key }
            .take(3) // Today + 2 days

        return byDate.mapIndexed { index, (dateStr, hours) ->
            val avgWaveHeight = hours.map { it.waveHeightMeters }.average()
            val avgWindSpeed = hours.map { it.windSpeedKmh }.average()
            val avgWindDir = hours.map { it.windDirectionDegrees }.average().toInt()
            val avgTemp = hours.map { it.temperatureCelsius }.average()
            val rating = ConditionRating.forSurf(avgWaveHeight)

            val dayName = when (index) {
                0 -> localizedContext.getString(R.string.widget_today)
                else -> {
                    try {
                        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale)
                    } catch (_: Exception) { dateStr }
                }
            }

            DayForecastState(
                dayName = dayName,
                conditionRating = rating.name,
                conditionRatingDisplay = localizedContext.getString(rating.nameRes),
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(avgWaveHeight, unitSystem),
                windFormatted = WeatherFormatter.formatWindCompact(avgWindSpeed, avgWindDir, unitSystem),
                temperatureFormatted = WeatherFormatter.formatTemperature(avgTemp, unitSystem)
            )
        }
    }

    private suspend fun writeSuccessState(
        cityName: String,
        conditionRating: String,
        conditionRatingDisplay: String,
        temperatureFormatted: String,
        waveHeightFormatted: String,
        windFormatted: String,
        activitiesJson: String,
        dayForecastsJson: String
    ) {
        val manager = GlanceAppWidgetManager(context)
        val widgetInstances = listOf(SmallWidget(), MediumWidget(), LargeWidget())

        for (widget in widgetInstances) {
            val glanceIds = manager.getGlanceIds(widget::class.java)
            for (glanceId in glanceIds) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] = cityName
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] = conditionRating
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] = conditionRatingDisplay
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] = temperatureFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WAVE_HEIGHT)] = waveHeightFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WIND)] = windFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ACTIVITIES_JSON)] = activitiesJson
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_DAY_FORECASTS_JSON)] = dayForecastsJson
                    prefs.remove(stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE))
                    prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] = System.currentTimeMillis()
                }
                widget.update(context, glanceId)
            }
        }
    }

    private suspend fun writeErrorState(errorMessage: String) {
        val manager = GlanceAppWidgetManager(context)
        val widgetInstances = listOf(SmallWidget(), MediumWidget(), LargeWidget())

        for (widget in widgetInstances) {
            val glanceIds = manager.getGlanceIds(widget::class.java)
            for (glanceId in glanceIds) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)] = errorMessage
                }
                widget.update(context, glanceId)
            }
        }
    }

    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "he") Locale("he") else Locale("en")
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        return context.createConfigurationContext(configuration)
    }
}
