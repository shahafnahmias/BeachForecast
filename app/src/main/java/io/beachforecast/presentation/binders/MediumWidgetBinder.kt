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
 * Binds data to the Medium (4x2) widget layout.
 * Shows: header with condition badge, 3-col vitals, activity row.
 */
class MediumWidgetBinder(
    private val context: Context,
    private val views: RemoteViews
) {

    fun applyTheme(themeColors: WidgetThemeColors) {
        views.setInt(R.id.widget_medium_root, "setBackgroundColor", themeColors.background)

        // Header
        views.setTextColor(R.id.widget_medium_city, themeColors.primaryText)
        views.setTextColor(R.id.widget_medium_temp, themeColors.primaryText)

        // Vitals labels
        views.setTextColor(R.id.widget_medium_wind_label, themeColors.tertiaryText)
        views.setTextColor(R.id.widget_medium_swell_label, themeColors.tertiaryText)
        views.setTextColor(R.id.widget_medium_sea_temp_label, themeColors.tertiaryText)

        // Vitals values
        views.setTextColor(R.id.widget_medium_wind_value, themeColors.primaryText)
        views.setTextColor(R.id.widget_medium_swell_value, themeColors.primaryText)
        views.setTextColor(R.id.widget_medium_sea_temp_value, themeColors.primaryText)
    }

    fun bind(
        cityName: String,
        conditions: CurrentConditions,
        unitSystem: UnitSystem,
        selectedSports: Set<Activity>,
        themeColors: WidgetThemeColors
    ) {
        // Header
        views.setTextViewText(R.id.widget_medium_city, cityName)
        val cloudIcon = WeatherFormatter.formatCloudCover(conditions.cloudCover)
        val tempText = WeatherFormatter.formatTemperature(conditions.temperatureCelsius, unitSystem)
        views.setTextViewText(R.id.widget_medium_cloud_icon, cloudIcon)
        views.setTextViewText(R.id.widget_medium_temp, tempText)

        // Condition badge
        val recommendations = ActivityRecommendationCalculator.calculateForSports(conditions, selectedSports)
        val ratingColor = recommendations.conditionRating.getWidgetColor()
        val badgeText = "\u25CF ${context.getString(recommendations.conditionRating.nameRes).uppercase()}"
        views.setTextViewText(R.id.widget_medium_condition_badge, badgeText)
        views.setTextColor(R.id.widget_medium_condition_badge, ratingColor)

        // Vitals
        views.setTextViewText(
            R.id.widget_medium_wind_value,
            WeatherFormatter.formatWindCompact(conditions.windSpeedKmh, conditions.windDirectionDegrees, unitSystem)
        )
        views.setTextViewText(
            R.id.widget_medium_swell_value,
            WeatherFormatter.formatSwellCompact(conditions.swellHeightMeters, conditions.swellPeriodSeconds, unitSystem)
        )
        views.setTextViewText(
            R.id.widget_medium_sea_temp_value,
            WeatherFormatter.formatSeaTemperature(conditions.seaSurfaceTemperatureCelsius, unitSystem)
        )

        // Activity row
        bindActivities(recommendations, themeColors)
    }

    private fun bindActivities(
        recommendations: io.beachforecast.domain.models.ActivityRecommendations,
        themeColors: WidgetThemeColors
    ) {
        val activityViewMap = mapOf(
            Activity.SURF to R.id.widget_medium_activity_surf,
            Activity.SWIM to R.id.widget_medium_activity_swim,
            Activity.KITE to R.id.widget_medium_activity_kite,
            Activity.SUP to R.id.widget_medium_activity_sup
        )

        for ((activity, viewId) in activityViewMap) {
            val isRecommended = recommendations.isRecommended(activity)
            val prefix = if (isRecommended) "\u2713" else "\u2717"
            val color = if (isRecommended) themeColors.recommendedColor else themeColors.notRecommendedColor
            val name = context.getString(activity.nameRes)
            views.setTextViewText(viewId, "$prefix$name")
            views.setTextColor(viewId, color)
        }
    }

    fun showError(errorMessage: String) {
        views.setTextViewText(R.id.widget_medium_city, context.getString(R.string.widget_error))
        views.setTextViewText(R.id.widget_medium_condition_badge, errorMessage)
        views.setTextColor(R.id.widget_medium_condition_badge, 0xFF90C1CB.toInt())
    }
}
