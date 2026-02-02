package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.ContentPart
import dev.olek.lmclient.data.models.AttachmentContentReference
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
            is ContentPart.Image -> MessageAttachment(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Audio -> MessageAttachment(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Video -> MessageAttachment(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.File -> MessageAttachment(
                content = part.content.toDomainAttachmentContent(part.mimeType),
                format = part.format,
                mimeType = part.mimeType,
                fileName = part.fileName,
            )

            is ContentPart.Text -> null
        }
    }

    suspend fun mapToKoog(attachment: MessageAttachment): ContentPart {
        val content = attachment.content.toKoogAttachmentContent()
        return when {
            attachment.mimeType.startsWith("image/") -> ContentPart.Image(
                content = content,
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            attachment.mimeType.startsWith("audio/") -> ContentPart.Audio(
                content = content,
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            attachment.mimeType.startsWith("video/") -> ContentPart.Video(
                content = content,
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )

            else -> ContentPart.File(
                content = content,
                format = attachment.format,
                mimeType = attachment.mimeType,
                fileName = attachment.fileName,
            )
        }
    }

    private suspend fun KoogAttachmentContent.toDomainAttachmentContent(
        mimeType: String,
    ): AttachmentContentReference {
        return when (this) {
            is KoogAttachmentContent.URL -> AttachmentContentReference.RemoteFile(url = url)
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

    private suspend fun AttachmentContentReference.toKoogAttachmentContent(): KoogAttachmentContent {
        return when (this) {
            is AttachmentContentReference.LocalFile -> {
                KoogAttachmentContent.Binary.Base64(
                    base64 = attachmentsRepository.getAttachmentContent(this).base64
                )
            }

            is AttachmentContentReference.RemoteFile -> {
                KoogAttachmentContent.URL(url = url)
            }
        }
    }
}
