package io.beachforecast.data.sources

/**
 * Interface for cache storage operations
 * Allows mocking SharedPreferences for testing
 */
interface CacheStorage {
    fun saveFloat(key: String, value: Float)
    fun getFloat(key: String, defaultValue: Float): Float

    fun saveLong(key: String, value: Long)
    fun getLong(key: String, defaultValue: Long): Long

    fun contains(key: String): Boolean

    fun clear()
}
