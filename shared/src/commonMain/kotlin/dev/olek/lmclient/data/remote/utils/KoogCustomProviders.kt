@file:Suppress("MatchingDeclarationName")

package dev.olek.lmclient.data.remote.utils

import ai.koog.prompt.llm.LLMProvider
import dev.olek.lmclient.data.util.LMClientModelProvider

internal data object KoogNexosAI :
    LLMProvider(LMClientModelProvider.NexosAI.id, LMClientModelProvider.NexosAI.display)

internal val LLMProvider.Companion.NexosAI get() = KoogNexosAI
