package dev.olek.lmclient.data.remote.mappers

import dev.olek.lmclient.data.models.LMClientError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException

/**
 * Maps various exception types to domain-specific [LMClientError] instances.
 * This provides a centralized error mapping strategy for all remote API calls.
 */
internal fun Throwable.toDomainError(): LMClientError = when (this) {
    is IOException -> LMClientError.ConnectionIssue
    is HttpRequestTimeoutException,
    is SocketTimeoutException,
    is ConnectTimeoutException,
    -> LMClientError.Timeout

    else -> extractErrorFromMessage()
}

/**
 * Attempts to extract a meaningful error from the exception message using pattern matching.
 */
private fun Throwable.extractErrorFromMessage(): LMClientError {
    val errorMessage = message ?: return LMClientError.UnknownError(toString())

    return ErrorPatternRegistry.findMatchingError(errorMessage)
        ?: LMClientError.UnknownError(errorMessage)
}

/**
 * Registry for error pattern matchers that can identify specific error types from messages.
 */
private object ErrorPatternRegistry {
    private val matchers = listOf(
        // HTTP status codes
        HttpStatusMatcher(429, LMClientError.RateLimit),
        HttpStatusMatcher(401, LMClientError.Authentication),
        HttpStatusMatcher(403, LMClientError.PermissionDenied),
        HttpStatusMatcher(504, LMClientError.Timeout), // Gateway timeout
        HttpStatusMatcher(529, LMClientError.Timeout), // Anthropic overloaded
        HttpStatusMatcher(500, LMClientError.ConnectionIssue),
        HttpStatusMatcher(502, LMClientError.ConnectionIssue),
        HttpStatusMatcher(503, LMClientError.ConnectionIssue),
        // Wrong API key patterns
        KeywordMatcher("api key not valid", LMClientError.Authentication),
        // Rate limiting patterns
        KeywordMatcher("rate limit", LMClientError.RateLimit),
        KeywordMatcher("too many requests", LMClientError.RateLimit),
        // Timeout patterns
        KeywordMatcher("overloaded", LMClientError.Timeout),
        KeywordMatcher("request timeout", LMClientError.Timeout),
        KeywordMatcher("connection timeout", LMClientError.Timeout),
        KeywordMatcher("read timeout", LMClientError.Timeout),
        KeywordMatcher("write timeout", LMClientError.Timeout),
        // Connection issue patterns
        KeywordMatcher("connection reset by peer", LMClientError.ConnectionIssue),
        KeywordMatcher("connection refused", LMClientError.ConnectionIssue),
        KeywordMatcher("temporarily unavailable", LMClientError.ConnectionIssue),
        KeywordMatcher("service unavailable", LMClientError.ConnectionIssue),
    )

    /**
     * Finds the first matching error for the given message.
     */
    fun findMatchingError(message: String): LMClientError? = matchers.firstNotNullOfOrNull { matcher ->
        matcher.match(message)
    }
}

/**
 * Base interface for error pattern matchers.
 */
private sealed interface ErrorPatternMatcher {
    val targetError: LMClientError

    fun match(message: String): LMClientError?
}

/**
 * Matches HTTP status codes in error messages using multiple pattern variations.
 */
private data class HttpStatusMatcher(private val statusCode: Int, override val targetError: LMClientError) :
    ErrorPatternMatcher {
    private val patterns = listOf(
        Regex("\\b$statusCode\\b"),
        Regex("status:?\\s*$statusCode", RegexOption.IGNORE_CASE),
        Regex("error:?\\s*$statusCode", RegexOption.IGNORE_CASE),
        Regex("code:?\\s*$statusCode", RegexOption.IGNORE_CASE),
    )

    override fun match(message: String): LMClientError? = if (patterns.any { it.containsMatchIn(message) }) {
        targetError
    } else {
        null
    }
}

/**
 * Matches specific keywords in error messages (case-insensitive).
 */
private data class KeywordMatcher(private val keyword: String, override val targetError: LMClientError) :
    ErrorPatternMatcher {
    private val lowercaseKeyword = keyword.lowercase()

    override fun match(message: String): LMClientError? = if (lowercaseKeyword in message.lowercase()) {
        targetError
    } else {
        null
    }
}
