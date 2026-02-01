package dev.olek.lmclient.data.remote.messages.koog

import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.models.MessageFinishReason
import org.koin.core.annotation.Factory

@Factory
class FinishReasonMapper {
    private val logger = Logger.withTag("FinishReasonMapper")

    private val reasonsMap = mapOf(
        MessageFinishReason.Stop to listOf("end_turn", "stop"),
        MessageFinishReason.Length to listOf("max_tokens", "length"),
        MessageFinishReason.ToolCalls to listOf("tool_use", "tool_calls"),
        MessageFinishReason.PauseTurn to listOf("pause_turn"),
        MessageFinishReason.ContentFilter to listOf(
            "refusal",
            "content_filter",
            "safety",
            "blocklist",
            "prohibited_content",
            "spii",
            "image_safety",
        ),
        MessageFinishReason.StopSequence to listOf("stop_sequence"),
        MessageFinishReason.FunctionCall to listOf("function_call"),
        MessageFinishReason.Language to listOf("language"),
    )

    fun map(finishReason: String?): MessageFinishReason? =
        finishReason?.lowercase()?.let { finishReason ->
            reasonsMap.entries.find { it.value.contains(finishReason) }?.key
                ?: MessageFinishReason.Unknown(finishReason).also {
                    logger.w("Unknown finish reason: $finishReason")
                }
        }
}
