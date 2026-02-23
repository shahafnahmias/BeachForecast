package io.beachforecast.widget

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Pre-computed widget display data, serialized to DataStore by WidgetUpdateWorker.
 * All formatting/computation happens in the worker; widgets just render.
 */
data class WidgetState(
    val cityName: String = "",
    val conditionRating: String = "",         // ConditionRating enum name
    val conditionRatingDisplay: String = "",  // Localized display text
    val temperatureFormatted: String = "",    // e.g. "25°C"
    val waveHeightFormatted: String = "",     // e.g. "40-60cm"
    val windFormatted: String = "",           // e.g. "12 km/h NW"
    val activities: List<ActivityState> = emptyList(),
    val dayForecasts: List<DayForecastState> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdated: Long = 0L
)

data class ActivityState(
    val name: String,           // Localized activity name
    val isRecommended: Boolean,
    val reason: String          // Localized reason text
)

data class DayForecastState(
    val dayName: String,                // Localized: "Today", "Tomorrow", "Wednesday"
    val conditionRating: String,        // ConditionRating enum name
    val conditionRatingDisplay: String, // Localized display text
    val waveHeightFormatted: String,    // e.g. "40-60cm"
    val windFormatted: String,          // e.g. "12 km/h NW"
    val temperatureFormatted: String    // e.g. "25°C"
)

/**
 * DataStore keys and serialization for widget state.
 */
object WidgetStateSerializer {
    private val gson = Gson()

    // DataStore preference keys
    const val KEY_CITY_NAME = "widget_city_name"
    const val KEY_CONDITION_RATING = "widget_condition_rating"
    const val KEY_CONDITION_RATING_DISPLAY = "widget_condition_rating_display"
    const val KEY_TEMPERATURE = "widget_temperature"
    const val KEY_WAVE_HEIGHT = "widget_wave_height"
    const val KEY_WIND = "widget_wind"
    const val KEY_ACTIVITIES_JSON = "widget_activities_json"
    const val KEY_DAY_FORECASTS_JSON = "widget_day_forecasts_json"
    const val KEY_ERROR_MESSAGE = "widget_error_message"
    const val KEY_LAST_UPDATED = "widget_last_updated"

    fun serializeActivities(activities: List<ActivityState>): String = gson.toJson(activities)

    fun deserializeActivities(json: String): List<ActivityState> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<ActivityState>>() {}.type
        return gson.fromJson(json, type)
    }

    fun serializeDayForecasts(forecasts: List<DayForecastState>): String = gson.toJson(forecasts)

    fun deserializeDayForecasts(json: String): List<DayForecastState> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<DayForecastState>>() {}.type
        return gson.fromJson(json, type)
    }
}
