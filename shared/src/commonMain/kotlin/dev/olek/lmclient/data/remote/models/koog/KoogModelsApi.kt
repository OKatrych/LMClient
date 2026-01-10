package dev.olek.lmclient.data.remote.models.koog

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import arrow.core.Either
import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.mappers.toDomainError
import dev.olek.lmclient.data.remote.mappers.toDomainModel
import dev.olek.lmclient.data.remote.models.ModelsApi

internal class KoogModelsApi(private val provider: LLMProvider) : ModelsApi {
    private val logger = Logger.withTag("KoogModelsApi")

    private val koogHardcodedModels: Map<LLMProvider, List<LLModel>> = mapOf(
        LLMProvider.Anthropic to listOf(
            AnthropicModels.Opus_3,
            AnthropicModels.Haiku_3,
            AnthropicModels.Sonnet_3_5,
            AnthropicModels.Haiku_3_5,
            AnthropicModels.Sonnet_3_7,
            AnthropicModels.Sonnet_4,
            AnthropicModels.Opus_4,
            AnthropicModels.Opus_4_1,
            AnthropicModels.Opus_4_5,
        ),
        LLMProvider.OpenAI to listOf(
            // Chat models
            OpenAIModels.Chat.GPT5_2Pro,
            OpenAIModels.Chat.GPT5_2,
            OpenAIModels.Chat.GPT5_1Codex,
            OpenAIModels.Chat.GPT5_1,
            OpenAIModels.Chat.GPT5Pro,
            OpenAIModels.Chat.GPT5,
            OpenAIModels.Chat.GPT5Codex,
            OpenAIModels.Chat.GPT5Mini,
            OpenAIModels.Chat.GPT5Nano,
            OpenAIModels.Chat.GPT4_1,
            OpenAIModels.Chat.GPT4o,
            OpenAIModels.Chat.O1,
            OpenAIModels.Chat.O3,
            OpenAIModels.Chat.O3Mini,
            OpenAIModels.Chat.O4Mini,
            // Moderation
            OpenAIModels.Moderation.Omni,
            // Audio
            OpenAIModels.Audio.GptAudio,
            OpenAIModels.Audio.GPT4oAudio,
            OpenAIModels.Audio.GPT4oMiniAudio,
        ),
        LLMProvider.Google to listOf(
            GoogleModels.Gemini2_0Flash001,
            GoogleModels.Gemini2_0FlashLite001,
            GoogleModels.Gemini2_0FlashLite,
            GoogleModels.Gemini2_0Flash,
            GoogleModels.Gemini2_5Pro,
            GoogleModels.Gemini2_5Flash,
            GoogleModels.Gemini2_5Flash,
            GoogleModels.Gemini3_Pro_Preview,
        ),
        LLMProvider.Meta to listOf(
            // TODO: Add Meta models
        ),
        LLMProvider.DeepSeek to listOf(
            // TODO: Add DeepSeek models
        ),
        LLMProvider.Alibaba to listOf(
            // TODO: Add Alibaba models
        ),
        LLMProvider.OpenRouter to listOf(
            // TODO: Add OpenRouter models
        ),
        LLMProvider.Ollama to listOf(
            // TODO: Add Ollama models
        ),
        LLMProvider.Bedrock to listOf(
            // TODO: Add Bedrock models
        ),
    )

    override suspend fun models(): Either<LMClientError, List<Model>> {
        logger.d { "Fetching models for provider $provider" }
        return Either
            .catch {
                koogHardcodedModels[provider]?.map(LLModel::toDomainModel)
                    ?: error("No models found for provider $provider")
            }.mapLeft {
                logger.e(it) { "Failed to fetch models for provider $provider" }
                it.toDomainError()
            }
    }
}
