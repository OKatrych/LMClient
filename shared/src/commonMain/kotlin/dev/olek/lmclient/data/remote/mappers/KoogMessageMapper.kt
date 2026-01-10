@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.remote.messages.koog.FinishReasonMapper
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ai.koog.prompt.message.Message as KoogMessage

@Factory
internal class KoogMessageMapper(private val finishReasonMapper: FinishReasonMapper) {
    fun map(message: KoogMessage): Message = when (message) {
        is KoogMessage.Assistant -> message.toDomainMessage()
        is KoogMessage.User -> message.toDomainMessage()
        is KoogMessage.System,
        is KoogMessage.Tool.Result,
        is KoogMessage.Tool.Call,
        -> error("Unsupported message type: $this")
        is KoogMessage.Reasoning -> TODO()
    }

    fun map(message: Message): KoogMessage = when (message) {
        is Message.AssistantMessage -> message.toKoogMessage()
        is Message.UserMessage -> message.toKoogMessage()
    }

    private fun KoogMessage.User.toDomainMessage(): Message.UserMessage = Message.UserMessage(
        id = Uuid.random().toHexDashString(),
        content = Message.MessageContent.Text(this.content),
        attachments = if (hasAttachments()) {
            TODO("Support attachments")
        } else {
            emptyList()
        },
    )

    private fun KoogMessage.Assistant.toDomainMessage(): Message.AssistantMessage = Message.AssistantMessage(
        id = Uuid.random().toHexDashString(),
        content = Message.MessageContent.Text(this.content),
        attachments = if (hasAttachments()) TODO("Support attachments") else emptyList(),
        finishReason = finishReasonMapper.map(finishReason),
        error = null,
    )

    private fun Message.AssistantMessage.toKoogMessage(): KoogMessage {
        val message = this
        return when (message.content) {
            is Message.MessageContent.Text -> {
                KoogMessage.Assistant(
                    content = message.content.text,
                    metaInfo = ResponseMetaInfo.Empty,
                    finishReason = message.finishReason?.reason,
                )
            }

            is Message.MessageContent.Audio -> TODO("Audio content not supported")
        }
    }

    private fun Message.UserMessage.toKoogMessage(): KoogMessage {
        val message = this
        return when (message.content) {
            is Message.MessageContent.Text -> {
                KoogMessage.User(
                    content = message.content.text,
                    metaInfo = RequestMetaInfo.Empty,
                )
            }

            is Message.MessageContent.Audio -> TODO("Audio content not supported")
        }
    }
}
