package io.beachforecast.domain.models

/**
 * Domain model for a beach location
 * Pure Kotlin - no Android dependencies
 */
data class Beach(
    val id: String,
    val nameHe: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Get localized name based on language code
     */
    fun getLocalizedName(languageCode: String): String {
        return if (languageCode == "he") nameHe else nameEn
    }
}

/**
 * Represents the user's beach selection preference
 */
sealed class BeachSelection {
    /**
     * Auto mode - find closest beach to user's GPS location
     */
    object Auto : BeachSelection()

    /**
     * Manual selection - user selected a specific beach
     */
    data class Manual(val beachId: String) : BeachSelection()

    companion object {
        const val AUTO_ID = "AUTO"

        /**
         * Parse from stored string value
         */
        fun fromString(value: String?): BeachSelection {
            return when {
                value == null || value == AUTO_ID -> Auto
                else -> Manual(value)
            }
        }

        /**
         * Convert to string for storage
         */
        fun toStorageString(selection: BeachSelection): String {
            return when (selection) {
                is Auto -> AUTO_ID
                is Manual -> selection.beachId
            }
        }
    }
}
