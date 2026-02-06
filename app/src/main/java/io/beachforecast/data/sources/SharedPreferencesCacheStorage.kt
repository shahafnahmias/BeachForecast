package io.beachforecast.data.sources

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences implementation of CacheStorage
 */
class SharedPreferencesCacheStorage(context: Context, prefsName: String) : CacheStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return prefs.getFloat(key, defaultValue)
    }

    override fun saveLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
