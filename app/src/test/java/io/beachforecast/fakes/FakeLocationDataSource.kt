package io.beachforecast.fakes

import android.location.Location
import io.beachforecast.data.sources.LocationDataSource

/**
 * Fake implementation of LocationDataSource for testing
 * Allows tests to control what location data is returned
 */
class FakeLocationDataSource : LocationDataSource {

    var currentLocationToReturn: Location? = null
    var lastKnownLocationToReturn: Location? = null
    var shouldThrowException = false
    var exceptionToThrow: Exception = RuntimeException("Test exception")

    override suspend fun getCurrentLocation(): Location? {
        if (shouldThrowException) throw exceptionToThrow
        return currentLocationToReturn
    }

    override suspend fun getLastKnownLocation(): Location? {
        if (shouldThrowException) throw exceptionToThrow
        return lastKnownLocationToReturn
    }

    fun reset() {
        currentLocationToReturn = null
        lastKnownLocationToReturn = null
        shouldThrowException = false
        exceptionToThrow = RuntimeException("Test exception")
    }
}
