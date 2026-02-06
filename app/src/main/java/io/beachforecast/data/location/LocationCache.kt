package io.beachforecast.data.location

import android.content.Context
import android.location.Location
import io.beachforecast.data.sources.CacheStorage
import io.beachforecast.data.sources.SharedPreferencesCacheStorage

/**
 * Simple cache for last known good location
 * Used by widgets when fresh location is unavailable
 * Uses CacheStorage interface for testability
 */
class LocationCache(
    private val cacheStorage: CacheStorage
) {

    constructor(context: Context) : this(
        SharedPreferencesCacheStorage(context, "location_cache")
    )

    companion object {
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val MAX_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    /**
     * Save location to cache
     */
    fun saveLocation(location: Location) {
        cacheStorage.saveFloat(KEY_LATITUDE, location.latitude.toFloat())
        cacheStorage.saveFloat(KEY_LONGITUDE, location.longitude.toFloat())
        cacheStorage.saveLong(KEY_TIMESTAMP, System.currentTimeMillis())
    }

    /**
     * Get cached location if it's not too old
     */
    fun getCachedLocation(): Location? {
        // Check if we have a valid cache using key presence (not sentinel values)
        if (!cacheStorage.contains(KEY_LATITUDE) || !cacheStorage.contains(KEY_LONGITUDE)) {
            return null
        }

        val latitude = cacheStorage.getFloat(KEY_LATITUDE, 0f)
        val longitude = cacheStorage.getFloat(KEY_LONGITUDE, 0f)
        val timestamp = cacheStorage.getLong(KEY_TIMESTAMP, 0L)

        // Check if cache is too old
        val age = System.currentTimeMillis() - timestamp
        if (age > MAX_AGE_MS) {
            return null
        }

        // Create location from cached data
        return Location("cache").apply {
            this.latitude = latitude.toDouble()
            this.longitude = longitude.toDouble()
            time = timestamp
        }
    }

    /**
     * Check if we have a valid cached location
     */
    fun hasCachedLocation(): Boolean {
        return getCachedLocation() != null
    }

    /**
     * Clear cached location
     */
    fun clear() {
        cacheStorage.clear()
    }
}
