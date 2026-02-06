package io.beachforecast.config

import io.beachforecast.BuildConfig

/**
 * API and network configuration constants
 */
object ApiConfig {
    // Server endpoints
    val SERVER_BASE_URL: String = BuildConfig.SERVER_BASE_URL
    const val WEATHER_ENDPOINT = "/weather"

    // HTTP timeouts
    const val CONNECTION_TIMEOUT_MS = 10000
    const val READ_TIMEOUT_MS = 10000

    // Location timeouts
    const val LOCATION_TIMEOUT_MS = 15_000L  // Increased for background/widget requests
    const val GEOCODING_TIMEOUT_MS = 5_000L

    // Forecast settings
    const val FORECAST_DAYS = 7  // 7-day forecast (server returns all days)
}
