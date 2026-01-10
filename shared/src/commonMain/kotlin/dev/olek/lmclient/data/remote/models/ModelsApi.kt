package dev.olek.lmclient.data.remote.models

import arrow.core.Either
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model

interface ModelsApi {
    suspend fun models(): Either<LMClientError, List<Model>>
}
