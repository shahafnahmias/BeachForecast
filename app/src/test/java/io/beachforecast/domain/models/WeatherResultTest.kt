package io.beachforecast.domain.models

import org.junit.Assert.*
import org.junit.Test

class WeatherResultTest {

    @Test
    fun `fold on Success calls onSuccess with data`() {
        val result: WeatherResult<String> = WeatherResult.Success("hello")

        val output = result.fold(
            onSuccess = { "got: $it" },
            onError = { "error" },
            onLoading = { "loading" }
        )

        assertEquals("got: hello", output)
    }

    @Test
    fun `fold on Error calls onError with error`() {
        val result: WeatherResult<String> = WeatherResult.Error(WeatherError.NoInternet)

        val output = result.fold(
            onSuccess = { "success" },
            onError = { it.message },
            onLoading = { "loading" }
        )

        assertEquals("No internet connection", output)
    }

    @Test
    fun `fold on Loading calls onLoading`() {
        val result: WeatherResult<String> = WeatherResult.Loading

        val output = result.fold(
            onSuccess = { "success" },
            onError = { "error" },
            onLoading = { "loading" }
        )

        assertEquals("loading", output)
    }

    @Test
    fun `map on Success transforms data`() {
        val result: WeatherResult<Int> = WeatherResult.Success(42)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess())
        assertEquals(84, mapped.getOrNull())
    }

    @Test
    fun `map on Error passes through error`() {
        val result: WeatherResult<Int> = WeatherResult.Error(WeatherError.NoInternet)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isError())
        assertEquals(WeatherError.NoInternet, mapped.errorOrNull())
    }

    @Test
    fun `map on Loading passes through loading`() {
        val result: WeatherResult<Int> = WeatherResult.Loading

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isLoading())
    }

    @Test
    fun `getOrNull on Success returns data`() {
        val result: WeatherResult<String> = WeatherResult.Success("data")

        assertEquals("data", result.getOrNull())
    }

    @Test
    fun `getOrNull on Error returns null`() {
        val result: WeatherResult<String> = WeatherResult.Error(WeatherError.NoInternet)

        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrNull on Loading returns null`() {
        val result: WeatherResult<String> = WeatherResult.Loading

        assertNull(result.getOrNull())
    }

    @Test
    fun `errorOrNull on Error returns error`() {
        val error = WeatherError.NetworkTimeout
        val result: WeatherResult<String> = WeatherResult.Error(error)

        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun `errorOrNull on Success returns null`() {
        val result: WeatherResult<String> = WeatherResult.Success("data")

        assertNull(result.errorOrNull())
    }

    @Test
    fun `errorOrNull on Loading returns null`() {
        val result: WeatherResult<String> = WeatherResult.Loading

        assertNull(result.errorOrNull())
    }
}
