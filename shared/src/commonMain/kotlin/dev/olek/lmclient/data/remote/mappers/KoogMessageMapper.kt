@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.ContentPart
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.remote.messages.koog.FinishReasonMapper
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ai.koog.prompt.message.Message as KoogMessage

@Factory
internal class KoogMessageMapper(
    private val finishReasonMapper: FinishReasonMapper,
    private val attachmentMapper: KoogAttachmentMapper,
) {
    suspend fun map(message: KoogMessage): Message = when (message) {
        is KoogMessage.Assistant -> message.toDomainMessage()
        is KoogMessage.User -> message.toDomainMessage()
        is KoogMessage.System,
        is KoogMessage.Tool.Result,
        is KoogMessage.Tool.Call,
            -> error("Unsupported message type: $this")

        is KoogMessage.Reasoning -> TODO()
    }

    suspend fun map(message: Message): KoogMessage = when (message) {
        is Message.AssistantMessage -> message.toKoogMessage()
        is Message.UserMessage -> message.toKoogMessage()
    }

    private suspend fun KoogMessage.User.toDomainMessage(): Message.UserMessage = Message.UserMessage(
        id = Uuid.random().toHexDashString(),
        content = MessageContent.Text(this.content),
        attachments = parts.mapNotNull { attachmentMapper.mapToDomain(it) },
    )

    private suspend fun KoogMessage.Assistant.toDomainMessage(): Message.AssistantMessage =
        Message.AssistantMessage(
            id = Uuid.random().toHexDashString(),
            content = MessageContent.Text(this.content),
            attachments = parts.mapNotNull { attachmentMapper.mapToDomain(it) },
            finishReason = finishReasonMapper.map(finishReason),
            error = null,
        )

    private fun Message.AssistantMessage.toKoogMessage(): KoogMessage {
        val message = this
        return when (message.content) {
            is MessageContent.Text -> {
                KoogMessage.Assistant(
                    content = message.content.text,
                    metaInfo = ResponseMetaInfo.Empty,
                    finishReason = message.finishReason?.reason,
                )
            }

            is MessageContent.Audio -> TODO("Audio content not supported")
        }
    }

    private suspend fun Message.UserMessage.toKoogMessage(): KoogMessage {
        val message = this
        return when (val content = message.content) {
            is MessageContent.Text -> {
                KoogMessage.User(
                    parts = buildList {
                        if (content.text.isNotBlank()) {
                            add(ContentPart.Text(content.text))
                        }
                        message.attachments.forEach { attachment ->
                            val mappedAttachment = attachmentMapper.mapToKoog(attachment)
                            if (mappedAttachment != null) add(mappedAttachment)
                        }
                    },
                    metaInfo = RequestMetaInfo.Empty,
                )
            }

            is MessageContent.Audio -> TODO("Audio content not supported")
        }
    }
}
