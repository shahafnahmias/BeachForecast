package io.beachforecast.domain.models

/**
 * Enum representing cloud cover levels
 * @param displayName Human-readable name
 * @param minCover Minimum cloud cover percentage (inclusive)
 * @param maxCover Maximum cloud cover percentage (exclusive)
 * @param icon Emoji icon representation
 */
enum class CloudCoverLevel(
    val displayName: String,
    val minCover: Int,
    val maxCover: Int,
    val icon: String
) {
    CLEAR(
        displayName = "Clear",
        minCover = 0,
        maxCover = 10,
        icon = "☀️"
    ),
    MOSTLY_CLEAR(
        displayName = "Mostly Clear",
        minCover = 10,
        maxCover = 30,
        icon = "🌤️"
    ),
    PARTLY_CLOUDY(
        displayName = "Partly Cloudy",
        minCover = 30,
        maxCover = 60,
        icon = "⛅"
    ),
    CLOUDY(
        displayName = "Cloudy",
        minCover = 60,
        maxCover = 90,
        icon = "☁️"
    ),
    OVERCAST(
        displayName = "Overcast",
        minCover = 90,
        maxCover = 101,
        icon = "☁️"
    );

    companion object {
        /**
         * Get cloud cover level from percentage
         */
        fun fromPercentage(percentage: Int): CloudCoverLevel {
            return values().first { percentage >= it.minCover && percentage < it.maxCover }
        }
    }
}
