package io.beachforecast.domain.models

import androidx.annotation.StringRes
import io.beachforecast.R

/**
 * Represents customizable weather metrics that users can choose to display
 */
enum class WeatherMetric(
    val displayName: String,
    @StringRes val nameRes: Int
) {
    CLOUD_COVER("Cloud Cover", R.string.metric_cloud_cover),
    TEMPERATURE("Temperature", R.string.metric_temperature),
    UV_INDEX("UV Index", R.string.metric_uv_index),
    SWELL("Swell", R.string.metric_swell),
    WIND("Wind", R.string.metric_wind),
    WAVE_PERIOD("Wave Period", R.string.metric_wave_period);

    companion object {
        /**
         * Default metrics shown to users (current behavior)
         */
        fun getDefaults(): Set<WeatherMetric> = setOf(
            CLOUD_COVER,
            TEMPERATURE,
            WAVE_PERIOD
        )

        /**
         * All available metrics
         */
        fun getAll(): List<WeatherMetric> = values().toList()
    }
}
