package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.llm.LLMCapability
import dev.olek.lmclient.data.models.Model

fun LLMCapability.toDomainModel(): Model.Capability? = when (this) {
    LLMCapability.Audio -> Model.Capability.Audio
    LLMCapability.Completion -> Model.Capability.Completion
    LLMCapability.Document -> Model.Capability.Document()
    LLMCapability.MultipleChoices -> Model.Capability.MultipleChoices
    LLMCapability.Schema.JSON.Basic -> Model.Capability.Schema.JSON.Basic
    LLMCapability.Schema.JSON.Standard -> Model.Capability.Schema.JSON.Standard
    LLMCapability.Temperature -> Model.Capability.Temperature
    LLMCapability.ToolChoice -> Model.Capability.ToolChoice
    LLMCapability.Tools -> Model.Capability.Tools
    LLMCapability.Vision.Image -> Model.Capability.Vision.Image()
    LLMCapability.Vision.Video -> Model.Capability.Vision.Video
    // Not needed in this app
    LLMCapability.Speculation,
    LLMCapability.Moderation,
    LLMCapability.Embed,
    LLMCapability.PromptCaching,
    LLMCapability.OpenAIEndpoint.Completions,
    LLMCapability.OpenAIEndpoint.Responses,
    -> null
}

fun Model.Capability.toKoogModel(): LLMCapability = when (this) {
    Model.Capability.Audio -> LLMCapability.Audio
    Model.Capability.Completion -> LLMCapability.Completion
    is Model.Capability.Document -> LLMCapability.Document
    Model.Capability.MultipleChoices -> LLMCapability.MultipleChoices
    Model.Capability.Schema.JSON.Basic -> LLMCapability.Schema.JSON.Basic
    Model.Capability.Schema.JSON.Standard -> LLMCapability.Schema.JSON.Standard
    Model.Capability.Temperature -> LLMCapability.Temperature
    Model.Capability.ToolChoice -> LLMCapability.ToolChoice
    Model.Capability.Tools -> LLMCapability.Tools
    is Model.Capability.Vision.Image -> LLMCapability.Vision.Image
    Model.Capability.Vision.Video -> LLMCapability.Vision.Video
}
