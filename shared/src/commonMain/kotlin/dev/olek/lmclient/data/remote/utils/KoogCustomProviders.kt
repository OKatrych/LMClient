package dev.olek.lmclient.data.remote.utils

import ai.koog.prompt.llm.LLMProvider
import dev.olek.lmclient.data.util.LMClientModelProvider

internal data object KoogNexosAI :
    LLMProvider(LMClientModelProvider.NexosAI.id, LMClientModelProvider.NexosAI.display)

internal data object KoogGithubCopilot :
    LLMProvider(LMClientModelProvider.GithubCopilot.id, LMClientModelProvider.GithubCopilot.display)

internal val LLMProvider.Companion.NexosAI get() = KoogNexosAI
internal val LLMProvider.Companion.GithubCopilot get() = KoogGithubCopilot
