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
                val hardcodedModels = NexosModels.entries

                responseBody.data.map { nexosModel ->
                    val hardcodedModel = hardcodedModels
                        .find { it.id == nexosModel.nexosId } ?: NexosModels.UNKNOWN_MODEL
                    Model(
                        id = nexosModel.nexosId,
                        providerId = LMClientModelProvider.NexosAI.id,
                        name = nexosModel.name,
                        capabilities = hardcodedModel.capabilities,
                        contextLength = hardcodedModel.contextLength,
                        maxOutputTokens = hardcodedModel.maxOutputTokens,
                    )
                }
            }.mapLeft {
                logger.e(it) { "Failed to fetch models from Nexos AI" }
                it.toDomainError()
            }
    }
}

@Serializable
internal data class NexosAiModelsResponse(
    val data: List<NexosAiModel>,
    val `object`: String,
    val total: Long
)

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

private enum class NexosModels(
    val id: String,
    val capabilities: List<Model.Capability>,
    val contextLength: Long,
    val maxOutputTokens: Long,
) {
    UNKNOWN_MODEL(
        id = "unknown",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 100_000,
        maxOutputTokens = 32_000,
    ),
    CLAUDE_SONNET_3_7(
        id = "5dd11d25-a38c-4def-b7d1-8ca9c9bbc190",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    CLAUDE_HAIKU_3_5(
        id = "c6b30b82-4372-4c89-84c7-3cc590dafae1",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 8_192,
    ),
    GPT_4O_MINI(
        id = "8ef67c2a-5f50-4b19-acfe-5c2e404dbc95",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 16_384,
    ),
    GEMINI_2_0_FLASH(
        id = "af796b88-7f2a-4252-b190-69fbdc7c849f",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Vision.Video,
            Model.Capability.Audio, Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_048_576,
        maxOutputTokens = 8_192,
    ),
    GEMINI_2_0_FLASH_LITE(
        id = "71127b75-8633-42e7-8a30-202c118b7d84",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Vision.Video,
            Model.Capability.Audio, Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_048_576,
        maxOutputTokens = 8_192,
    ),
    O1(
        id = "c4470a36-26a8-433f-a546-8616f0bb05d3",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 100_000,
    ),
    O3_MINI(
        id = "cd2df23d-9e2e-4fd9-9958-569dd1d6d26a",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 100_000,
    ),
    GEMMA_3(
        id = "ece75f80-ee58-42dd-a47e-e11998c919d6",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 8_192,
    ),
    GEMINI_2_5_FLASH(
        id = "ce85ef82-fc66-4f34-815b-6c86b3a3202c",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Vision.Video,
            Model.Capability.Audio, Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_048_576,
        maxOutputTokens = 65_536,
    ),
    MISTRAL_MEDIUM(
        id = "8d5bc408-6aa5-41a6-88b7-331189b83b22",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 16_384,
    ),
    CLAUDE_OPUS_4(
        id = "ef1b894d-fb44-42fb-9bb2-2ea810c84545",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 32_000,
    ),
    CLAUDE_SONNET_4(
        id = "50a38df1-ec05-415e-b799-001e3be88418",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    GEMINI_2_5_PRO(
        id = "8b77459d-7cc0-4bcd-a671-34648dd4aec6",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Vision.Video,
            Model.Capability.Audio, Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_048_576,
        maxOutputTokens = 65_536,
    ),
    GPT_4_1(
        id = "7071fb76-722a-41ad-8477-68123b04b4ab",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_000_000,
        maxOutputTokens = 32_768,
    ),
    GPT_4O(
        id = "a1b8d261-5c63-4329-80b4-beff81792858",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 16_384,
    ),
    CLAUDE_SONNET_4_BEDROCK(
        id = "719a4ff7-20b4-412b-8bca-9999175b5773",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    QWEN_3_235B(
        id = "a41cb929-041e-4dce-8a39-88aeac9c26a7",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 32_000,
    ),
    GPT_OSS_20B(
        id = "6c620053-186f-464e-ab38-6be5efe98881",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 128_000,
    ),
    GPT_5(
        id = "75975321-03c8-44b9-9ac3-e1c130aa1924",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 400_000,
        maxOutputTokens = 128_000,
    ),
    GPT_5_MINI(
        id = "11a9b888-7466-47d1-859b-b37f799e6ad2",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 400_000,
        maxOutputTokens = 128_000,
    ),
    GPT_5_NANO(
        id = "b9230911-eb05-4574-b574-fdf33630070c",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 400_000,
        maxOutputTokens = 128_000,
    ),
    LLAMA_4_SCOUT(
        id = "c8518efb-fc4b-4197-8a1d-02e671ffe63c",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 131_072,
        maxOutputTokens = 8_192,
    ),
    GEMMA_3_4B(
        id = "b5a8c14e-b6ce-4c02-b9db-f47d639c7731",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 8_192,
    ),
    QWEN_2_5_7B(
        id = "c403adc5-d079-4d7e-a293-9c4d32b1b877",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 131_072,
        maxOutputTokens = 8_192,
    ),
    GPT_OSS_120B(
        id = "fae2b3bb-8e8a-4128-9996-e72857eeee90",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 128_000,
    ),
    CLAUDE_SONNET_4_5(
        id = "a4b49105-2062-4c50-96e3-5f85c11a023a",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    GPT_5_CHAT(
        id = "07722829-d94b-41a4-88d8-9d90486a0f26",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
        ),
        contextLength = 128_000,
        maxOutputTokens = 16_384,
    ),
    CLAUDE_HAIKU_4_5(
        id = "209b1235-5670-461f-b157-e2fabe0212ec",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    GEMINI_3_PRO_PREVIEW(
        id = "afb78182-2bac-48f0-a45b-700eab7557e9",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Vision.Video,
            Model.Capability.Audio, Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 1_000_000,
        maxOutputTokens = 64_000,
    ),
    CLAUDE_OPUS_4_5(
        id = "fad52b2d-2719-4b86-808b-30641f5f1a52",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Vision.Image(), Model.Capability.Document(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 200_000,
        maxOutputTokens = 64_000,
    ),
    GPT_5_2(
        id = "533b43f7-382c-451e-a0f1-e3c6b5f71d3c",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 400_000,
        maxOutputTokens = 128_000,
    ),
    GPT_5_2_CHAT(
        id = "9329f7d4-1f8f-496b-b9f1-385326488781",
        capabilities = listOf(
            Model.Capability.Completion,
            Model.Capability.Vision.Image(),
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 128_000,
        maxOutputTokens = 16_384,
    ),
    CODESTRAL(
        id = "a211fe1d-5afd-452e-bef8-b71371852743",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 256_000,
        maxOutputTokens = 4_096,
    ),
    GROK_CODE_FAST(
        id = "b36393ba-1c89-4dab-8906-88378c77144b",
        capabilities = listOf(
            Model.Capability.Temperature, Model.Capability.Completion,
            Model.Capability.Tools, Model.Capability.ToolChoice,
        ),
        contextLength = 256_000,
        maxOutputTokens = 256_000,
    ),
    DALL_E_3(
        id = "30efda1f-3f29-44df-aa0a-116ad5f109d1",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    TTS_1(
        id = "e4fe41a7-db1a-4f26-95c2-0f675a892d9d",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    WHISPER_1(
        id = "56ddb108-b377-4b07-9c14-ad6640029133",
        capabilities = listOf(Model.Capability.Audio),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    TEXT_EMBEDDING_3_SMALL(
        id = "8a692621-fdf4-420a-a082-9ba4b3638575",
        capabilities = emptyList(),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    TEXT_EMBEDDING_3_LARGE(
        id = "25ac0c9d-c0dc-4994-9f0c-e63bdfb74311",
        capabilities = emptyList(),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    GPT_IMAGE_1(
        id = "ab9fcd26-94ab-451d-95dd-fdb51bb232eb",
        capabilities = listOf(Model.Capability.Completion, Model.Capability.Vision.Image()),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    IMAGEN_4(
        id = "0a8de53e-c1ee-4a04-a1d0-c6c058669219",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    IMAGEN_4_FAST(
        id = "3ac1a858-da0c-4dc3-a158-ed0b6730aeaa",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
    IMAGEN_4_ULTRA(
        id = "1a715651-7755-4817-a100-5daf15f97142",
        capabilities = listOf(Model.Capability.Completion),
        contextLength = 0,
        maxOutputTokens = 0,
    ),
}
