package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface LMClientError {
    @Serializable
    data object Authentication : LMClientError

    @Serializable
    data object RateLimit : LMClientError

    @Serializable
    data object PermissionDenied : LMClientError

    @Serializable
    data object Timeout : LMClientError

    @Serializable
    data object ConnectionIssue : LMClientError

    @Serializable
    data class UnknownError(val message: String) : LMClientError
}
