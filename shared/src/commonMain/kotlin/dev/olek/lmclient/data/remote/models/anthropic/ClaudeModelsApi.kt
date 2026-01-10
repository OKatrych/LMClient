package dev.olek.lmclient.data.remote.models.anthropic

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

internal class ClaudeModelsApi(
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ModelsApi {
    companion object Companion {
        private const val PATH = "v1/models"

        private val DEFAULT_CAPABILITIES = listOf(Model.Capability.Completion)
        private const val DEFAULT_MAX_OUTPUT = 4096L
        private const val DEFAULT_CONTEXT_WINDOW = 200_000L
    }

    private val logger = Logger.withTag("ClaudeModelsApi")

    override suspend fun models(): Either<LMClientError, List<Model>> = withContext(dispatcher) {
        Either
            .catch {
                logger.d { "Fetching models from Claude" }
                val responseBody = httpClient.get(PATH).body<ClaudeModelsResponse>()

                responseBody.data.map { claudeModel ->
                    val (capabilities, contextWindow, maxOutput) = ClaudeModels.entries
                        .find { it.id == claudeModel.id }
                        ?.let { Triple(it.capabilities, it.contextWindow, it.maxOutput) }
                        // Fallback to default values if model not found in AnthropicModels
                        ?: Triple(DEFAULT_CAPABILITIES, DEFAULT_CONTEXT_WINDOW, DEFAULT_MAX_OUTPUT)

                    Model(
                        id = claudeModel.id,
                        providerId = LMClientModelProvider.Claude.id,
                        name = claudeModel.displayName,
                        capabilities = capabilities,
                        contextLength = contextWindow,
                        maxOutputTokens = maxOutput,
                    )
                }
            }.mapLeft {
                logger.e(it) { "Failed to fetch model" }
                it.toDomainError()
            }
    }
}

@Serializable
internal data class ClaudeModelsResponse(
    val data: List<ClaudeModel>,
    @SerialName("first_id") val firstId: String,
    @SerialName("last_id") val lastId: String,
    @SerialName("has_more") val hasMore: Boolean,
)

@Serializable
internal data class ClaudeModel(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String,
    val type: String,
)

private enum class ClaudeModels(
    val id: String,
    val capabilities: List<Model.Capability>,
    val contextWindow: Long,
    val maxOutput: Long,
) {
    OPUS_4_1(
        id = "claude-opus-4-1",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 32000,
    ),
    OPUS_4(
        id = "claude-opus-4-0",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 32000,
    ),
    SONNET_4(
        id = "claude-sonnet-4",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 64000,
    ),
    SONNET_3_7(
        id = "claude-3-7-sonnet-latest",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 64000,
    ),
    HAIKU_3_5(
        id = "claude-3-5-haiku-latest",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 8192,
    ),
    HAIKU_3(
        id = "claude-3-haiku-20240307",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Completion,
        ),
        contextWindow = 200_000,
        maxOutput = 4096,
    ),
}
