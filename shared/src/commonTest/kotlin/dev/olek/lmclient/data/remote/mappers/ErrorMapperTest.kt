package dev.olek.lmclient.data.remote.mappers

import dev.olek.lmclient.data.models.LMClientError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ErrorMapperTest {

    @Test
    fun `IOException maps to ConnectionIssue`() {
        val error = IOException("Network failure").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `HttpRequestTimeoutException maps to ConnectionIssue because it extends IOException`() {
        val error = HttpRequestTimeoutException("url", null).toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `SocketTimeoutException maps to ConnectionIssue because it extends IOException`() {
        val error = SocketTimeoutException("Socket timed out").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `ConnectTimeoutException maps to ConnectionIssue because it extends IOException`() {
        val error = ConnectTimeoutException("Connection timed out").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `message containing 429 maps to RateLimit`() {
        val error = RuntimeException("HTTP 429 Too Many Requests").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `message with status 429 maps to RateLimit`() {
        val error = RuntimeException("status: 429").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `message with error 429 maps to RateLimit`() {
        val error = RuntimeException("error: 429").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `message with code 429 maps to RateLimit`() {
        val error = RuntimeException("code 429").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `message containing 401 maps to Authentication`() {
        val error = RuntimeException("HTTP 401 Unauthorized").toDomainError()
        assertEquals(LMClientError.Authentication, error)
    }

    @Test
    fun `message containing 403 maps to PermissionDenied`() {
        val error = RuntimeException("HTTP 403 Forbidden").toDomainError()
        assertEquals(LMClientError.PermissionDenied, error)
    }

    @Test
    fun `message containing 504 maps to Timeout`() {
        val error = RuntimeException("Gateway Timeout 504").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `message containing 529 maps to Timeout`() {
        val error = RuntimeException("status: 529 Overloaded").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `message containing 500 maps to ConnectionIssue`() {
        val error = RuntimeException("Internal Server Error 500").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `message containing 502 maps to ConnectionIssue`() {
        val error = RuntimeException("Bad Gateway 502").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `message containing 503 maps to ConnectionIssue`() {
        val error = RuntimeException("Service Unavailable 503").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `status code not matched when embedded in larger number`() {
        val error = RuntimeException("Error 14291 occurred").toDomainError()
        assertIs<LMClientError.UnknownError>(error)
    }

    @Test
    fun `api key not valid maps to Authentication`() {
        val error = RuntimeException("The api key not valid for this endpoint").toDomainError()
        assertEquals(LMClientError.Authentication, error)
    }

    @Test
    fun `rate limit maps to RateLimit`() {
        val error = RuntimeException("You have hit the rate limit").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `too many requests maps to RateLimit`() {
        val error = RuntimeException("too many requests, please slow down").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `overloaded maps to Timeout`() {
        val error = RuntimeException("The server is overloaded right now").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `request timeout maps to Timeout`() {
        val error = RuntimeException("request timeout after 30s").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `connection timeout maps to Timeout`() {
        val error = RuntimeException("connection timeout while connecting to host").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `read timeout maps to Timeout`() {
        val error = RuntimeException("read timeout").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `write timeout maps to Timeout`() {
        val error = RuntimeException("write timeout").toDomainError()
        assertEquals(LMClientError.Timeout, error)
    }

    @Test
    fun `connection reset by peer maps to ConnectionIssue`() {
        val error = RuntimeException("connection reset by peer").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `connection refused maps to ConnectionIssue`() {
        val error = RuntimeException("connection refused on port 443").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `temporarily unavailable maps to ConnectionIssue`() {
        val error = RuntimeException("resource temporarily unavailable").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `service unavailable maps to ConnectionIssue`() {
        val error = RuntimeException("service unavailable, try again later").toDomainError()
        assertEquals(LMClientError.ConnectionIssue, error)
    }

    @Test
    fun `keyword matching is case insensitive`() {
        val error = RuntimeException("RATE LIMIT exceeded").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `null message maps to UnknownError with toString`() {
        val exception = object : Exception() {
            override val message: String? = null
            override fun toString(): String = "CustomException"
        }
        val error = exception.toDomainError()
        assertIs<LMClientError.UnknownError>(error)
        assertEquals("CustomException", error.message)
    }

    @Test
    fun `unrecognized message maps to UnknownError with original message`() {
        val error = RuntimeException("something completely different happened").toDomainError()
        assertIs<LMClientError.UnknownError>(error)
        assertEquals("something completely different happened", error.message)
    }

    @Test
    fun `first matching pattern wins - 429 in message with rate limit keyword`() {
        val error = RuntimeException("Error 429: rate limit exceeded").toDomainError()
        assertEquals(LMClientError.RateLimit, error)
    }

    @Test
    fun `HTTP status code 401 takes precedence over api key keyword`() {
        val error = RuntimeException("401 - api key not valid").toDomainError()
        assertEquals(LMClientError.Authentication, error)
    }

}
