@file:OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class)

package dev.olek.lmclient.data.local.mapper

import dev.olek.lmclient.data.databases.MessageAttachments
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import org.koin.core.annotation.Factory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal typealias MessageAttachmentDbModel = MessageAttachments

@Factory
internal class MessageAttachmentMapper {

    fun mapToDbModel(attachment: MessageAttachment, messageId: String): MessageAttachmentDbModel {
        val (contentRefType, contentRef) = attachment.content.serialize()
        return MessageAttachmentDbModel(
            id = Uuid.random().toHexDashString(),
            message_id = messageId,
            content_reference_type = contentRefType,
            content_reference = contentRef,
            file_type = attachment.format,
            mime_type = attachment.mimeType,
            file_name = attachment.fileName,
            created_at = "",
        )
    }

    fun mapFromDbModel(dbModel: MessageAttachmentDbModel): MessageAttachment {
        val content = deserializeContentReference(
            type = dbModel.content_reference_type,
            value = dbModel.content_reference,
        )
        return MessageAttachment(
            content = content,
            format = dbModel.file_type ?: "",
            mimeType = dbModel.mime_type ?: "",
            fileName = dbModel.file_name,
        )
    }

    private fun AttachmentContentReference.serialize(): Pair<String, String> = when (this) {
        is AttachmentContentReference.LocalFile -> "local" to Base64.encode(pathBytes)
        is AttachmentContentReference.RemoteFile -> "remote" to url
    }

    private fun deserializeContentReference(type: String, value: String): AttachmentContentReference {
        return when (type) {
            "local" -> AttachmentContentReference.LocalFile(Base64.decode(value))
            "remote" -> AttachmentContentReference.RemoteFile(value)
            else -> error("Unknown content reference type: $type")
        }
    }
}
