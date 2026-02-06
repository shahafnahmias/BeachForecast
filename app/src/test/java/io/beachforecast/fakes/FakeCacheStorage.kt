package io.beachforecast.fakes

import io.beachforecast.data.sources.CacheStorage

/**
 * Fake implementation of CacheStorage for testing
 * Uses in-memory storage instead of SharedPreferences
 */
class FakeCacheStorage : CacheStorage {

    private val floatStorage = mutableMapOf<String, Float>()
    private val longStorage = mutableMapOf<String, Long>()

    override fun saveFloat(key: String, value: Float) {
        floatStorage[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return floatStorage[key] ?: defaultValue
    }

    override fun saveLong(key: String, value: Long) {
        longStorage[key] = value
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return longStorage[key] ?: defaultValue
    }

    override fun contains(key: String): Boolean {
        return floatStorage.containsKey(key) || longStorage.containsKey(key)
    }

    override fun clear() {
        floatStorage.clear()
        longStorage.clear()
    }

    fun reset() {
        clear()
    }
}
