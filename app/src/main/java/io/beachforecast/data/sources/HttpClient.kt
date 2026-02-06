package io.beachforecast.data.sources

/**
 * Interface for HTTP operations
 * Allows mocking network calls for testing
 */
interface HttpClient {
    /**
     * Perform HTTP GET request
     * @param url The URL to fetch
     * @return Response body as string
     * @throws Exception if request fails
     */
    suspend fun get(url: String): String
}
