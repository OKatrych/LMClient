@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.streaming.StreamFrame
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.messages.koog.FinishReasonMapper
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi

@Factory
internal class KoogStreamFrameMapper(private val finishReasonMapper: FinishReasonMapper) {
    fun map(messageId: String, streamFrame: StreamFrame): PromptApi.MessageStreamResult = when (streamFrame) {
        is StreamFrame.Append -> PromptApi.MessageStreamResult.Chunk(
            id = messageId,
            content = streamFrame.text,
        )

        is StreamFrame.End -> PromptApi.MessageStreamResult.Finished(
            id = messageId,
            reason = finishReasonMapper.map(streamFrame.finishReason),
        )

        is StreamFrame.ToolCall -> TODO("Implement ToolCall mapping")
    }
}
