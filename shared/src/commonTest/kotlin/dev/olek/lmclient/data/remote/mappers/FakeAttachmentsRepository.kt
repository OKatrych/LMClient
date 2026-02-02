package dev.olek.lmclient.data.remote.mappers

import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import io.github.vinceglb.filekit.PlatformFile

internal class FakeAttachmentsRepository : AttachmentsRepository {
    var processAssistantAttachmentResult: AttachmentContentReference =
        AttachmentContentReference.LocalFile(
            pathBytes = "default-path".encodeToByteArray()
        )
    var getAttachmentContentResult: AttachmentsRepository.AttachmentContent =
        AttachmentsRepository.AttachmentContent(
            base64 = "ZGVmYXVsdA=="
        )
    var lastProcessedBase64: String? = null
    var lastProcessedMimeType: String? = null

    override suspend fun processUserAttachment(
        attachmentFile: PlatformFile
    ): AttachmentContentReference {
        error("Not implemented for tests")
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
    ): AttachmentsRepository.AttachmentContent {
        return getAttachmentContentResult
    }

    override suspend fun deleteAttachment(reference: AttachmentContentReference) {
        // No-op for tests
    }
}
