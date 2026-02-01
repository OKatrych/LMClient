package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface MessageAttachment {
    val content: AttachmentReference

    val format: String
    val mimeType: String
    val fileName: String?

    @Serializable
    data class Image(
        override val content: AttachmentReference,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?,
    ) : MessageAttachment

    @Serializable
    data class Video(
        override val content: AttachmentReference,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment

    @Serializable
    data class Audio(
        override val content: AttachmentReference,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment

    @Serializable
    data class File(
        override val content: AttachmentReference,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment
}

sealed interface AttachmentReference {
    @Serializable
    data class RemoteFile(val url: String) : AttachmentReference
    @Serializable
    data class LocalFile(val uri: String) : AttachmentReference
}
