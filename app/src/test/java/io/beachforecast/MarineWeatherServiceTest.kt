package io.beachforecast

import io.beachforecast.config.ApiConfig
import io.beachforecast.fakes.FakeHttpClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class MarineWeatherServiceTest {

    private lateinit var fakeHttpClient: FakeHttpClient
    private lateinit var service: MarineWeatherService

    private val testLat = 32.0
    private val testLon = 34.0

    private val expectedUrl = "${ApiConfig.SERVER_BASE_URL}${ApiConfig.WEATHER_ENDPOINT}" +
        "?lat=$testLat&lon=$testLon&days=${ApiConfig.FORECAST_DAYS}"

    @Before
    fun setup() {
        fakeHttpClient = FakeHttpClient()
        service = MarineWeatherService(fakeHttpClient)
    }

    @Test
    fun `getCombinedWeatherData returns null on network error`() = runTest {
        fakeHttpClient.shouldThrowException = true
        fakeHttpClient.exceptionToThrow = RuntimeException("Network error")

        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when network error occurs", result)
    }

    @Test
    fun `getCombinedWeatherData returns null on IOException`() = runTest {
        fakeHttpClient.shouldThrowException = true
        fakeHttpClient.exceptionToThrow = IOException("Connection refused")

        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when IOException occurs", result)
    }

    @Test
    fun `getCombinedWeatherData returns null on malformed JSON`() = runTest {
        fakeHttpClient.setResponse(expectedUrl, "invalid json")

        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when response is malformed JSON", result)
    }

    @Test
    fun `getCombinedWeatherData returns null on empty JSON object`() = runTest {
        fakeHttpClient.setResponse(expectedUrl, "{}")

        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when response is empty JSON object", result)
    }

    @Test
    fun `getCombinedWeatherData returns null on empty response`() = runTest {
        fakeHttpClient.setResponse(expectedUrl, "")

        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when response is empty string", result)
    }

    @Test
    fun `getCombinedWeatherData returns null when URL has no configured response`() = runTest {
        // FakeHttpClient throws IllegalStateException for unconfigured URLs
        val result = service.getCombinedWeatherData(testLat, testLon)

        assertNull("Should return null when no response is configured", result)
    }
}
