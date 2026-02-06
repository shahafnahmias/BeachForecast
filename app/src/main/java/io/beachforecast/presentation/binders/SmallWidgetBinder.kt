package io.beachforecast.presentation.binders

import android.content.Context
import android.widget.RemoteViews
import io.beachforecast.R
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WidgetThemeColors
import io.beachforecast.domain.models.getWidgetColor

/**
 * Binds data to the Small (2x2) widget layout.
 * Shows: beach name, cloud+temp, wave height (colored), rating + primary activity.
 */
class SmallWidgetBinder(
    private val context: Context,
    private val views: RemoteViews
) {

    fun applyTheme(themeColors: WidgetThemeColors) {
        views.setInt(R.id.widget_small_root, "setBackgroundColor", themeColors.background)
        views.setTextColor(R.id.widget_small_city, themeColors.primaryText)
        views.setTextColor(R.id.widget_small_temp, themeColors.primaryText)
    }

    fun bind(
        cityName: String,
        conditions: CurrentConditions,
        unitSystem: UnitSystem,
        selectedSports: Set<Activity>
    ) {
        views.setTextViewText(R.id.widget_small_city, cityName)

        // Cloud + temp
        val cloudIcon = WeatherFormatter.formatCloudCover(conditions.cloudCover)
        val tempText = WeatherFormatter.formatTemperature(conditions.temperatureCelsius, unitSystem)
        views.setTextViewText(R.id.widget_small_cloud_icon, cloudIcon)
        views.setTextViewText(R.id.widget_small_temp, tempText)

        // Wave height (colored by condition)
        val recommendations = ActivityRecommendationCalculator.calculateForSports(conditions, selectedSports)
        val ratingColor = recommendations.conditionRating.getWidgetColor()

        val waveText = WeatherFormatter.formatSwellCompact(conditions.waveHeightMeters, 0.0, unitSystem)
            .split(" ").first() // Take just the height part (e.g., "1.2m")
        views.setTextViewText(R.id.widget_small_wave_height, waveText)
        views.setTextColor(R.id.widget_small_wave_height, ratingColor)

        // Rating + primary activity
        val ratingName = context.getString(recommendations.conditionRating.nameRes)
        val primaryName = recommendations.primaryActivity?.let { context.getString(it.nameRes) }
        val ratingText = if (primaryName != null) {
            "$ratingName \u00B7 $primaryName"
        } else {
            ratingName
        }
        views.setTextViewText(R.id.widget_small_rating_activity, ratingText)
        views.setTextColor(R.id.widget_small_rating_activity, ratingColor)
    }

    fun showError(errorMessage: String) {
        views.setTextViewText(R.id.widget_small_city, context.getString(R.string.widget_error))
        views.setTextViewText(R.id.widget_small_wave_height, "--")
        views.setTextViewText(R.id.widget_small_rating_activity, errorMessage)
        views.setTextColor(R.id.widget_small_rating_activity, 0xFF90C1CB.toInt())
    }
}
