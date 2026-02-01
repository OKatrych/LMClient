package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.ContentPart
import dev.olek.lmclient.data.models.AttachmentReference
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import org.koin.core.annotation.Factory
import ai.koog.prompt.message.AttachmentContent as KoogAttachmentContent

@Factory
internal class KoogAttachmentMapper(
    private val attachmentsRepository: AttachmentsRepository,
) {

    suspend fun mapToDomain(part: ContentPart): MessageAttachment? {
        return when (part) {
            is ContentPart.Image -> MessageAttachment.Image(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Audio -> MessageAttachment.Audio(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Video -> MessageAttachment.Video(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.File -> MessageAttachment.File(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Text -> null
        }
    }

    suspend fun mapToKoog(attachment: MessageAttachment): ContentPart {
        return when (attachment) {
            is MessageAttachment.Image -> ContentPart.Image(
                content = attachment.content.toKoogAttachmentContent(),
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            is MessageAttachment.Audio -> ContentPart.Audio(
                content = attachment.content.toKoogAttachmentContent(),
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            is MessageAttachment.Video -> ContentPart.Video(
                content = attachment.content.toKoogAttachmentContent(),
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            is MessageAttachment.File -> ContentPart.File(
                content = attachment.content.toKoogAttachmentContent(),
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )
        }
    }

    private suspend fun KoogAttachmentContent.toDomainAttachmentContent(
        mimeType: String,
    ): AttachmentReference {
        return when (this) {
            is KoogAttachmentContent.URL -> AttachmentReference.RemoteFile(url = url)
            is KoogAttachmentContent.Binary.Base64 ->
                attachmentsRepository.processAssistantAttachment(
                    base64 = base64,
                    mimeType = mimeType,
                )

            is KoogAttachmentContent.Binary.Bytes ->
                attachmentsRepository.processAssistantAttachment(
                    base64 = asBase64(),
                    mimeType = mimeType,
                )

            is KoogAttachmentContent.PlainText -> error("Should not happen")
        }
    }

    private suspend fun AttachmentReference.toKoogAttachmentContent(): KoogAttachmentContent {
        return when (this) {
            is AttachmentReference.LocalFile -> {
                KoogAttachmentContent.Binary.Base64(
                    base64 = attachmentsRepository.getAttachmentContent(this).base64
                )
            }

            is AttachmentReference.RemoteFile -> {
                KoogAttachmentContent.URL(url = url)
            }
        }
    }
}
