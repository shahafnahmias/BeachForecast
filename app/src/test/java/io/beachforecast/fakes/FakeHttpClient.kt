package io.beachforecast.fakes

import io.beachforecast.data.sources.HttpClient

/**
 * Fake implementation of HttpClient for testing
 * Returns predefined responses or throws exceptions
 */
class FakeHttpClient : HttpClient {

    private val responses = mutableMapOf<String, String>()
    var shouldThrowException = false
    var exceptionToThrow: Exception = RuntimeException("Network error")

    override suspend fun get(url: String): String {
        if (shouldThrowException) throw exceptionToThrow

        return responses[url] ?: throw IllegalStateException("No response configured for URL: $url")
    }

    /**
     * Configure a response for a specific URL
     */
    fun setResponse(url: String, response: String) {
        responses[url] = response
    }

    /**
     * Configure a response for any URL containing the given substring
     */
    fun setResponseForUrlContaining(urlSubstring: String, response: String) {
        // Store with the substring as key for matching
        responses[urlSubstring] = response
    }

    /**
     * Override get to support substring matching
     */
    fun getResponseForUrl(url: String): String? {
        // First try exact match
        responses[url]?.let { return it }

        // Then try substring match
        responses.entries.firstOrNull { url.contains(it.key) }?.let {
            return it.value
        }

        return null
    }

    fun reset() {
        responses.clear()
        shouldThrowException = false
        exceptionToThrow = RuntimeException("Network error")
    }
}
