package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.llm.LLMProvider
import dev.olek.lmclient.data.remote.utils.NexosAI
import dev.olek.lmclient.data.util.LMClientModelProvider

internal fun LMClientModelProvider.toKoogProvider(): LLMProvider = when (this) {
    LMClientModelProvider.Claude -> LLMProvider.Anthropic
    LMClientModelProvider.DeepSeek -> LLMProvider.DeepSeek
    LMClientModelProvider.Google -> LLMProvider.Google
    LMClientModelProvider.Ollama -> LLMProvider.Ollama
    LMClientModelProvider.OpenAI -> LLMProvider.OpenAI
    LMClientModelProvider.OpenRouter -> LLMProvider.OpenRouter
    LMClientModelProvider.NexosAI -> LLMProvider.NexosAI
}
