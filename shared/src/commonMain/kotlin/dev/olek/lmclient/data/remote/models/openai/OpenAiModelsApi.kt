package dev.olek.lmclient.data.remote.models.openai

import arrow.core.Either
import co.touchlab.kermit.Logger
import com.aallam.openai.client.OpenAI
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.mappers.toDomainError
import dev.olek.lmclient.data.remote.models.ModelsApi
import dev.olek.lmclient.data.util.LMClientModelProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class OpenAiModelsApi(private val openAI: OpenAI, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    ModelsApi {
    companion object {
        private val DEFAULT_CAPABILITIES = listOf(Model.Capability.Completion)
        private const val DEFAULT_MAX_OUTPUT = 16_384L
        private const val DEFAULT_CONTEXT_WINDOW = 128_000L
    }

    private val logger = Logger.Companion.withTag("OpenAiModelsApi")

    override suspend fun models(): Either<LMClientError, List<Model>> = withContext(dispatcher) {
        Either
            .catch {
                openAI.models().map { openAiModel ->
                    val (capabilities, contextWindow, maxOutput) = OpenAIModels.entries
                        .find { it.id == openAiModel.id.id }
                        ?.let { Triple(it.capabilities, it.contextWindow, it.maxOutput) }
                        // Fallback to default values if model not found in OpenAIModels
                        ?: Triple(DEFAULT_CAPABILITIES, DEFAULT_CONTEXT_WINDOW, DEFAULT_MAX_OUTPUT)
                    Model(
                        id = openAiModel.id.id,
                        providerId = LMClientModelProvider.OpenAI.id,
                        name = openAiModel.id.id,
                        capabilities = capabilities,
                        contextLength = contextWindow,
                        maxOutputTokens = maxOutput,
                    )
                }
            }.mapLeft {
                logger.e(it) { "Failed to fetch models" }
                it.toDomainError()
            }
    }
}

private enum class OpenAIModels(
    val id: String,
    val capabilities: List<Model.Capability>,
    val contextWindow: Long,
    val maxOutput: Long,
) {
    GPT_5(
        id = "gpt-5",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 400_000,
        maxOutput = 128_000,
    ),
    O4_MINI(
        id = "o4-mini",
        capabilities = listOf(
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 200_000,
        maxOutput = 100_000,
    ),
    O3_MINI(
        id = "o3-mini",
        capabilities = listOf(
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 200_000,
        maxOutput = 100_000,
    ),
    O3(
        id = "o3",
        capabilities = listOf(
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 200_000,
        maxOutput = 100_000,
    ),
    O1(
        id = "o1",
        capabilities = listOf(
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 200_000,
        maxOutput = 100_000,
    ),
    GPT_4O(
        id = "gpt-4o",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.ToolChoice,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 128_000,
        maxOutput = 16_384,
    ),
    GPT_4_1(
        id = "gpt-4.1",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 1_047_576,
        maxOutput = 32_768,
    ),
    GPT_5_MINI(
        id = "gpt-5-mini",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 400_000,
        maxOutput = 128_000,
    ),
    GPT_5_NANO(
        id = "gpt-5-nano",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 400_000,
        maxOutput = 128_000,
    ),
    GPT_4O_MINI_AUDIO(
        id = "gpt-4o-mini-audio-preview",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Completion,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Audio,
        ),
        contextWindow = 128_000,
        maxOutput = 16_384,
    ),
    GPT_4O_AUDIO(
        id = "gpt-4o-audio-preview",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Completion,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Audio,
        ),
        contextWindow = 128_000,
        maxOutput = 16_384,
    ),
    GPT_4_1_NANO(
        id = "gpt-4.1-nano",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 1_047_576,
        maxOutput = 32_768,
    ),
    GPT_4_1_MINI(
        id = "gpt-4.1-mini",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 1_047_576,
        maxOutput = 32_768,
    ),
    GPT_4O_MINI(
        id = "gpt-4o-mini",
        capabilities = listOf(
            Model.Capability.Temperature,
            Model.Capability.Schema.JSON.Basic,
            Model.Capability.Schema.JSON.Standard,
            Model.Capability.Tools,
            Model.Capability.ToolChoice,
            Model.Capability.Vision.Image,
            Model.Capability.Document,
            Model.Capability.Completion,
            Model.Capability.MultipleChoices,
        ),
        contextWindow = 128_000,
        maxOutput = 16_384,
    ),
}
