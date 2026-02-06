package io.beachforecast.domain.usecases

import java.text.SimpleDateFormat
import java.util.*

/**
 * Pure formatting logic extracted from ViewModel
 * 100% testable - no Android dependencies
 */
object FormatWeatherForDisplayUseCase {

    // ISO timestamp "yyyy-MM-ddTHH:mm" positional constants
    private const val ISO_TIMESTAMP_MIN_LENGTH = 16
    private const val ISO_DATE_LENGTH = 10
    private const val ISO_HOUR_START = 11
    private const val ISO_HOUR_END = 13

    /**
     * Format ISO timestamp to display time
     * Input: "2024-01-15T14:00" or "14:00"
     * Output: "2 PM"
     */
    fun formatTime(time: String): String {
        return try {
            // Handle ISO format: "yyyy-MM-ddTHH:mm"
            if (time.contains("T") && time.length >= ISO_TIMESTAMP_MIN_LENGTH) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
                val date = inputFormat.parse(time)
                date?.let { outputFormat.format(it) } ?: extractHourMinute(time)
            } else {
                time
            }
        } catch (e: Exception) {
            extractHourMinute(time)
        }
    }

    /**
     * Format date string to display format
     * Input: "2024-01-15"
     * Output: "Mon, Jan 15"
     */
    fun formatDayName(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val dateObj = inputFormat.parse(date)
            dateObj?.let { outputFormat.format(it) } ?: date
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Format timestamp to relative time
     * Input: timestamp in milliseconds
     * Output: "Just now", "5m ago", "2h ago"
     */
    fun formatLastUpdated(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 60000

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            else -> "${minutes / 60}h ago"
        }
    }

    /**
     * Extract hour from ISO timestamp
     * Input: "2024-01-15T14:00" or any string with time at position 11-13
     * Output: 14
     */
    fun extractHourFromTime(time: String): Int {
        return try {
            if (time.length >= ISO_HOUR_END) {
                time.substring(ISO_HOUR_START, ISO_HOUR_END).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Extract HH:mm from ISO timestamp as fallback
     */
    private fun extractHourMinute(time: String): String {
        return if (time.length >= ISO_TIMESTAMP_MIN_LENGTH) time.substring(ISO_HOUR_START, ISO_TIMESTAMP_MIN_LENGTH) else time
    }

    /**
     * Validate ISO timestamp format
     * Returns true if format is "yyyy-MM-ddTHH:mm" and has valid length
     */
    fun isValidIsoTimestamp(time: String): Boolean {
        return time.contains("T") && time.length >= ISO_TIMESTAMP_MIN_LENGTH
    }

    /**
     * Extract date portion from ISO timestamp
     * Input: "2024-01-15T14:00"
     * Output: "2024-01-15"
     */
    fun extractDate(timestamp: String): String {
        return if (timestamp.length >= ISO_DATE_LENGTH) {
            timestamp.substring(0, ISO_DATE_LENGTH)
        } else {
            ""
        }
    }
}
