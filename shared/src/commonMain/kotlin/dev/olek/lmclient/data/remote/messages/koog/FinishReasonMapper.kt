package dev.olek.lmclient.data.remote.messages.koog

import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.models.Message
import org.koin.core.annotation.Factory

@Factory
class FinishReasonMapper {
    private val logger = Logger.withTag("FinishReasonMapper")

    private val reasonsMap = mapOf(
        Message.MessageFinishReason.Stop to listOf("end_turn", "stop"),
        Message.MessageFinishReason.Length to listOf("max_tokens", "length"),
        Message.MessageFinishReason.ToolCalls to listOf("tool_use", "tool_calls"),
        Message.MessageFinishReason.PauseTurn to listOf("pause_turn"),
        Message.MessageFinishReason.ContentFilter to listOf(
            "refusal",
            "content_filter",
            "safety",
            "blocklist",
            "prohibited_content",
            "spii",
            "image_safety",
        ),
        Message.MessageFinishReason.StopSequence to listOf("stop_sequence"),
        Message.MessageFinishReason.FunctionCall to listOf("function_call"),
        Message.MessageFinishReason.Language to listOf("language"),
    )

    fun map(finishReason: String?): Message.MessageFinishReason? =
        finishReason?.lowercase()?.let { finishReason ->
            reasonsMap.entries.find { it.value.contains(finishReason) }?.key
                ?: Message.MessageFinishReason.Unknown(finishReason).also {
                    logger.w("Unknown finish reason: $finishReason")
                }
        }
}
