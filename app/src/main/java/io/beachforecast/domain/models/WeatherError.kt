package io.beachforecast.domain.models

import androidx.annotation.StringRes
import io.beachforecast.R

/**
 * Sealed class representing specific error types
 * Allows for precise error handling and user feedback
 */
sealed class WeatherError(val message: String) {

    // Network Errors
    object NoInternet : WeatherError("No internet connection")
    object NetworkTimeout : WeatherError("Request timed out")
    data class ApiError(val statusCode: Int, val details: String = "") :
        WeatherError("API error: $statusCode ${if (details.isNotEmpty()) "- $details" else ""}")
    object ApiRateLimited : WeatherError("Too many requests. Try again later")

    // Location Errors
    object LocationPermissionDenied : WeatherError("Location permission denied")
    object LocationUnavailable : WeatherError("Location unavailable")
    object LocationDisabled : WeatherError("Location services disabled")
    object LocationTimeout : WeatherError("Location request timed out")

    // Data Errors
    object NoDataAvailable : WeatherError("No weather data available")
    object InvalidCoordinates : WeatherError("Invalid location coordinates")
    object DataParsingError : WeatherError("Failed to parse weather data")

    // General Errors
    data class Unknown(val throwable: Throwable? = null) :
        WeatherError("Unknown error: ${throwable?.message ?: "Something went wrong"}")

    /**
     * Get user-friendly error message resource ID
     */
    @StringRes
    fun getUserMessageRes(): Int = when (this) {
        is NoInternet -> R.string.error_no_internet
        is NetworkTimeout -> R.string.error_timeout
        is ApiError -> R.string.error_api
        is ApiRateLimited -> R.string.error_rate_limited
        is LocationPermissionDenied -> R.string.error_location_permission
        is LocationUnavailable -> R.string.error_location_unavailable
        is LocationDisabled -> R.string.error_location_disabled
        is LocationTimeout -> R.string.error_location_timeout
        is NoDataAvailable -> R.string.error_no_data
        is InvalidCoordinates -> R.string.error_invalid_coordinates
        is DataParsingError -> R.string.error_data_parsing
        is Unknown -> R.string.error_unknown
    }

    /**
     * Get short error message resource ID
     */
    @StringRes
    fun getShortMessageRes(): Int = when (this) {
        is NoInternet -> R.string.error_short_no_internet
        is NetworkTimeout -> R.string.error_short_timeout
        is ApiError -> R.string.error_short_api
        is ApiRateLimited -> R.string.error_short_rate_limited
        is LocationPermissionDenied -> R.string.error_short_location_permission
        is LocationUnavailable -> R.string.error_short_location_unavailable
        is LocationDisabled -> R.string.error_short_location_disabled
        is LocationTimeout -> R.string.error_short_location_timeout
        is NoDataAvailable -> R.string.error_short_no_data
        is InvalidCoordinates -> R.string.error_short_invalid_coordinates
        is DataParsingError -> R.string.error_short_data_parsing
        is Unknown -> R.string.error_short_unknown
    }

    /**
     * Check if error is recoverable by retrying
     */
    fun isRetryable(): Boolean = when (this) {
        is NoInternet, is NetworkTimeout, is LocationTimeout,
        is LocationUnavailable, is NoDataAvailable -> true
        is ApiRateLimited -> false // Wait required
        is LocationPermissionDenied, is LocationDisabled -> false // User action required
        is ApiError -> statusCode >= 500 // Server errors are retryable
        else -> false
    }

    /**
     * Get suggested recovery action
     */
    fun getRecoveryAction(): RecoveryAction = when (this) {
        is NoInternet -> RecoveryAction.CHECK_CONNECTION
        is NetworkTimeout -> RecoveryAction.RETRY
        is ApiError -> if (statusCode >= 500) RecoveryAction.RETRY else RecoveryAction.WAIT
        is ApiRateLimited -> RecoveryAction.WAIT
        is LocationPermissionDenied -> RecoveryAction.GRANT_PERMISSION
        is LocationUnavailable -> RecoveryAction.ENABLE_GPS
        is LocationDisabled -> RecoveryAction.ENABLE_GPS
        is LocationTimeout -> RecoveryAction.RETRY
        is NoDataAvailable -> RecoveryAction.CHANGE_LOCATION
        is InvalidCoordinates -> RecoveryAction.CHANGE_LOCATION
        is DataParsingError -> RecoveryAction.UPDATE_APP
        is Unknown -> RecoveryAction.RETRY
    }

    enum class RecoveryAction {
        RETRY,
        CHECK_CONNECTION,
        GRANT_PERMISSION,
        ENABLE_GPS,
        CHANGE_LOCATION,
        WAIT,
        UPDATE_APP
    }
}

/**
 * Result type for operations that can fail
 */
sealed class WeatherResult<out T> {
    data class Success<T>(val data: T) : WeatherResult<T>()
    data class Error(val error: WeatherError) : WeatherResult<Nothing>()
    object Loading : WeatherResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorOrNull(): WeatherError? = when (this) {
        is Error -> error
        else -> null
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (WeatherError) -> R,
        onLoading: () -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(error)
        is Loading -> onLoading()
    }

    inline fun <R> map(transform: (T) -> R): WeatherResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(error)
        is Loading -> Loading
    }
}
