package io.beachforecast.data.sources

import android.location.Location

/**
 * Interface for location data sources
 * Allows mocking Android location APIs for testing
 */
interface LocationDataSource {
    /**
     * Get current location from GPS/network
     * Returns null if location unavailable
     */
    suspend fun getCurrentLocation(): Location?

    /**
     * Get last known cached location
     * Returns null if no cached location available
     */
    suspend fun getLastKnownLocation(): Location?
}
