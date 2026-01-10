package dev.olek.lmclient.data.remote.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun claudeHttpClient(
    apiKey: String,
    baseUrl: String,
    isDebug: Boolean,
) = HttpClient {
    install(Logging) {
        level = if (isDebug) LogLevel.BODY else LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger.i(message, tag = "Ktor")
            }
        }
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }

    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
        // Anthropic specific headers
        header("X-Api-Key", apiKey)
        header("anthropic-version", "2023-06-01")
    }
}
