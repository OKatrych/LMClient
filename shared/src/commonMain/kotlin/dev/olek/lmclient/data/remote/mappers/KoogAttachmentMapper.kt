package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.ContentPart
import arrow.core.getOrElse
import dev.olek.lmclient.data.models.AttachmentContent
import dev.olek.lmclient.data.models.AttachmentContent.LocalFile
import dev.olek.lmclient.data.models.AttachmentContent.RemoteFile
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.repositories.FilesRepository
import org.koin.core.annotation.Factory
import kotlin.io.encoding.Base64
import ai.koog.prompt.message.AttachmentContent as KoogAttachmentContent

@Factory
internal class KoogAttachmentMapper(
    private val filesRepository: FilesRepository,
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
    ): AttachmentContent {
        return when (this) {
            is KoogAttachmentContent.URL -> RemoteFile(url = url)
            is KoogAttachmentContent.Binary.Base64 -> filesRepository.processFile(
                base64 = base64,
                mimeType = mimeType,
            ).getOrElse { throw Exception(it.toString()) }

            is KoogAttachmentContent.Binary.Bytes -> filesRepository.processFile(
                base64 = asBase64(),
                mimeType = mimeType,
            ).getOrElse { throw Exception(it.toString()) }

            is KoogAttachmentContent.PlainText -> error("Should not happen")
        }
    }

    private suspend fun AttachmentContent.toKoogAttachmentContent(): KoogAttachmentContent {
        return when (this) {
            is LocalFile -> {
                KoogAttachmentContent.Binary.Base64(
                    Base64.encode(
                        filesRepository.getFileBytes(this)
                            .getOrElse { throw Exception(it.toString()) }
                    )
                )
            }

            is RemoteFile -> {
                KoogAttachmentContent.URL(url = url)
            }
        }
    }
}
