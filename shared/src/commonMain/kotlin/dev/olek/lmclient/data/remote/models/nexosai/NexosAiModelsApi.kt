package dev.olek.lmclient.data.remote.models.nexosai

import arrow.core.Either
import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.mappers.toDomainError
import dev.olek.lmclient.data.remote.models.ModelsApi
import dev.olek.lmclient.data.util.LMClientModelProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class NexosAiModelsApi(
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ModelsApi {
    companion object {
        private const val PATH = "models"
    }

    private val logger = Logger.withTag("NexosAiModelsApi")

    override suspend fun models(): Either<LMClientError, List<Model>> = withContext(dispatcher) {
        Either
            .catch {
                logger.d { "Fetching models from Nexos AI" }
                val responseBody = httpClient.get(PATH).body<NexosAiModelsResponse>()

                responseBody.data.map { nexosModel ->
                    Model(
                        id = nexosModel.nexosId,
                        providerId = LMClientModelProvider.NexosAI.id,
                        name = nexosModel.name,
                        capabilities = listOf(
                            Model.Capability.Completion,
                        ),
                        contextLength = 128000L, // Most models support large context windows
                        maxOutputTokens = 4096L, // Default max output
                    )
                }
            }.mapLeft {
                logger.e(it) { "Failed to fetch models from Nexos AI" }
                it.toDomainError()
            }
    }
}

@Serializable
internal data class NexosAiModelsResponse(val data: List<NexosAiModel>, val `object`: String, val total: Long)

@Serializable
internal data class NexosAiModel(
    val id: String, // Claude 3.7 Sonnet (Anthropic) - Intelligent model
    @SerialName("nexos_model_id")
    val nexosId: String, // 5dd11d25-a38c-4def-b7d1-8ca9c9bbc190
    val name: String, // Claude 3.7 Sonnet (Anthropic) - Intelligent model
    val `object`: String,
    val created: Long,
    val updated: Long,
    @SerialName("owned_by") val ownedBy: String,
    @SerialName("timeout_ms") val timeoutMs: Long,
    @SerialName("stream_timeout_ms") val streamTimeoutMs: Long,
)
