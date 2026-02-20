package dev.olek.lmclient.data.remote.messages.anthropic

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow

/**
 * Custom version of [AnthropicLLMClient] that allows to use custom model versions.
 * Use it as a workaround for https://github.com/JetBrains/koog/issues/768
 */
internal class CustomKoogAnthropicClient(
    apiKey: String,
    settings: AnthropicClientSettings = AnthropicClientSettings(),
    private val customVersionsMap: MutableMap<LLModel, String> = settings.modelVersionsMap
        .toMutableMap(),
) : AnthropicLLMClient(
    apiKey,
    settings.withCustomVersionsMap(customVersionsMap),
) {
    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): List<Message.Response> {
        customVersionsMap[model] = model.id
        return super.execute(prompt, model, tools)
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): Flow<StreamFrame> {
        customVersionsMap[model] = model.id
        return super.executeStreaming(prompt, model, tools)
    }
}

private fun AnthropicClientSettings.withCustomVersionsMap(
    map: Map<LLModel, String>,
) = AnthropicClientSettings(
    modelVersionsMap = map,
    baseUrl = baseUrl,
    apiVersion = apiVersion,
    timeoutConfig = timeoutConfig,
)
