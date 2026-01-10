package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.utils.NexosAI
import dev.olek.lmclient.data.util.LMClientModelProvider

fun Model.toKoogModel(): LLModel {
    val provider = LMClientModelProvider.fromId(providerId).toKoogProvider()
    return LLModel(
        provider = provider,
        id = id,
        capabilities = capabilities
            .map(Model.Capability::toKoogModel)
            .plusOpenAiCapability(provider),
        contextLength = contextLength,
        maxOutputTokens = maxOutputTokens,
    )
}

fun LLModel.toDomainModel(): Model = Model(
    id = id,
    name = id,
    providerId = provider.id,
    capabilities = capabilities.mapNotNull(LLMCapability::toDomainModel),
    contextLength = contextLength,
    maxOutputTokens = maxOutputTokens,
)

/**
 * OpenAi clients have special capabilities
 */
private fun List<LLMCapability>.plusOpenAiCapability(provider: LLMProvider): List<LLMCapability> {
    val openAiApiProviders = listOf(LLMProvider.OpenAI, LLMProvider.NexosAI, LLMProvider.OpenRouter)
    if (provider in openAiApiProviders) {
        if (contains(LLMCapability.Completion)) {
            return if (contains(LLMCapability.Audio)) {
                plus(LLMCapability.OpenAIEndpoint.Completions)
            } else {
                plus(LLMCapability.OpenAIEndpoint.Completions)
                    .plus(LLMCapability.OpenAIEndpoint.Responses)
            }
        }
    }
    return this
}
