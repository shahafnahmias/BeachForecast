package io.beachforecast.data.repository

import android.content.Context
import io.beachforecast.domain.models.Beach
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Repository for loading beach data from JSON asset
 */
class BeachRepository(
    private val context: Context
) {
    private var cachedBeaches: List<Beach>? = null

    /**
     * Load all beaches from assets/beaches.json
     * Caches result for performance
     */
    fun getAllBeaches(): List<Beach> {
        cachedBeaches?.let { return it }

        val beaches = loadBeachesFromAssets()
        cachedBeaches = beaches
        return beaches
    }

    /**
     * Find a beach by its ID
     */
    fun getBeachById(id: String): Beach? {
        return getAllBeaches().find { it.id == id }
    }

    /**
     * Clear cached beaches (useful for testing)
     */
    fun clearCache() {
        cachedBeaches = null
    }

    private fun loadBeachesFromAssets(): List<Beach> {
        return try {
            val json = context.assets.open("beaches.json")
                .bufferedReader().use { it.readText() }

            val gson = Gson()
            val type = object : TypeToken<List<BeachJsonModel>>() {}.type
            val jsonBeaches: List<BeachJsonModel> = gson.fromJson(json, type)

            jsonBeaches.map { jsonModel ->
                Beach(
                    id = generateBeachId(jsonModel.name_en),
                    nameHe = jsonModel.name_he,
                    nameEn = jsonModel.name_en,
                    latitude = jsonModel.latitude,
                    longitude = jsonModel.longitude
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading beaches")
            emptyList()
        }
    }

    /**
     * Generate a stable ID from the beach name
     * Example: "Tel Aviv - Hilton Beach" -> "tel_aviv_hilton_beach"
     */
    private fun generateBeachId(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    /**
     * JSON model matching the structure of beaches.json
     */
    private data class BeachJsonModel(
        val name_he: String,
        val name_en: String,
        val latitude: Double,
        val longitude: Double
    )
}
