package io.beachforecast.domain.models

/**
 * Enum representing time periods throughout the day
 * @param displayName Human-readable name
 * @param hourRange Range of hours (24-hour format)
 */
enum class TimeOfDay(
    val displayName: String,
    val hourRange: IntRange
) {
    MORNING(
        displayName = "Morning",
        hourRange = 5..11
    ),
    AFTERNOON(
        displayName = "Afternoon",
        hourRange = 12..16
    ),
    EVENING(
        displayName = "Evening",
        hourRange = 17..20
    ),
    NIGHT(
        displayName = "Night",
        hourRange = 21..4  // Note: wraps around midnight
    );

    companion object {
        /**
         * Get time of day from hour (0-23)
         */
        fun fromHour(hour: Int): TimeOfDay {
            return when (hour) {
                in MORNING.hourRange -> MORNING
                in AFTERNOON.hourRange -> AFTERNOON
                in EVENING.hourRange -> EVENING
                else -> NIGHT
            }
        }

        /**
         * Get forecast periods for tomorrow
         */
        fun getForecastPeriods(): List<ForecastPeriod> = listOf(
            ForecastPeriod(MORNING, "06:00-11:00"),
            ForecastPeriod(AFTERNOON, "12:00-17:00"),  // Using "Noon" label
            ForecastPeriod(EVENING, "18:00-23:00")
        )
    }

    /**
     * Data class for forecast period configuration
     */
    data class ForecastPeriod(
        val timeOfDay: TimeOfDay,
        val hours: String,
        val label: String = if (timeOfDay == AFTERNOON) "Noon" else timeOfDay.displayName
    )
}
