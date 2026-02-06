package io.beachforecast.domain.models

import androidx.compose.runtime.Stable

/**
 * Type of tide event
 */
enum class TideType {
    HIGH, LOW
}

/**
 * Domain model for a single tide event (high or low tide)
 */
@Stable
data class TideEvent(
    val time: String,  // ISO timestamp format
    val height: Float,  // meters
    val type: TideType
)
