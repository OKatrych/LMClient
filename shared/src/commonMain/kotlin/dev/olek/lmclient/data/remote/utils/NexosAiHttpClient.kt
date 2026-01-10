package dev.olek.lmclient.data.remote.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun nexosAiHttpClient(apiKey: String, baseUrl: String, isDebug: Boolean) = HttpClient {
    install(Logging) {
        level = if (isDebug) LogLevel.BODY else LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger
                    .i(message, tag = "Ktor")
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

    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(accessToken = apiKey, refreshToken = "")
            }
        }
    }

    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }

    expectSuccess = true
}
