@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed interface Message {
    val content: MessageContent

    val attachments: List<MessageAttachment>

    val id: String

    @Serializable
    data class UserMessage(
        override val id: String = Uuid.random().toHexDashString(),
        override val content: MessageContent,
        override val attachments: List<MessageAttachment>,
    ) : Message

    @Serializable
    data class AssistantMessage(
        override val id: String,
        override val content: MessageContent,
        override val attachments: List<MessageAttachment>,
        val finishReason: MessageFinishReason?,
        val error: LMClientError?,
    ) : Message
}

@Serializable
sealed interface MessageContent {
    @Serializable
    data class Text(val text: String) : MessageContent

    @Serializable
    data class Audio(val audioUri: String) : MessageContent
}

@Serializable
sealed interface MessageFinishReason {
    val reason: String

    /**
     * The model reached a natural stopping point
     */
    data object Stop : MessageFinishReason {
        override val reason: String = "stop"
    }

    /**
     * The maximum number of tokens specified in the request was reached
     */
    data object Length : MessageFinishReason {
        override val reason: String = "length"
    }

    /**
     * Content was omitted due to a flag from provider content filters
     */
    data object ContentFilter : MessageFinishReason {
        override val reason: String = "content_filter"
    }

    /**
     * The model decided to call a tool
     */
    data object ToolCalls : MessageFinishReason {
        override val reason: String = "tool_calls"
    }

    /**
     * The model decided to call a function
     */
    data object FunctionCall : MessageFinishReason {
        override val reason: String = "function_call"
    }

    /**
     * Provider paused a long-running turn. You may provide the response back as-is in a
     * subsequent request to let the model continue.
     */
    data object PauseTurn : MessageFinishReason {
        override val reason: String = "pause_turn"
    }

    /**
     * One of provided custom `stop_sequences` was generated
     */
    data object StopSequence : MessageFinishReason {
        override val reason: String = "stop_sequence"
    }

    /**
     * The response content was flagged for using an unsupported language.
     */
    data object Language : MessageFinishReason {
        override val reason: String = "language"
    }

    data class Unknown(override val reason: String) : MessageFinishReason
}

fun Message.AssistantMessage.finishedGeneration() = finishReason != null || error != null
