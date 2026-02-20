package dev.olek.lmclient.data.remote.mappers

import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import io.github.vinceglb.filekit.PlatformFile

internal class FakeAttachmentsRepository : AttachmentsRepository {
    var processUserAttachmentResult: MessageAttachment = MessageAttachment(
        content = AttachmentContentReference.LocalFile(
            pathBytes = "default-user-path".encodeToByteArray()
        ),
        format = "png",
        mimeType = "image/png",
        fileName = "test.png",
    )
    var processAssistantAttachmentResult: AttachmentContentReference =
        AttachmentContentReference.LocalFile(
            pathBytes = "default-path".encodeToByteArray()
        )
    var getAttachmentContentResult: AttachmentsRepository.AttachmentContent? =
        AttachmentsRepository.AttachmentContent(
            base64 = "ZGVmYXVsdA=="
        )
    var lastProcessedBase64: String? = null
    var lastProcessedMimeType: String? = null

    override suspend fun processUserAttachment(
        attachmentFile: PlatformFile
    ): MessageAttachment {
        return processUserAttachmentResult
    }

    override suspend fun processAssistantAttachment(
        base64: String,
        mimeType: String,
    ): AttachmentContentReference {
        lastProcessedBase64 = base64
        lastProcessedMimeType = mimeType
        return processAssistantAttachmentResult
    }

    override suspend fun getAttachmentContent(
        reference: AttachmentContentReference,
    ): AttachmentsRepository.AttachmentContent? {
        return getAttachmentContentResult
    }

    override suspend fun deleteAttachment(reference: AttachmentContentReference) {
        // No-op for tests
    }
}
