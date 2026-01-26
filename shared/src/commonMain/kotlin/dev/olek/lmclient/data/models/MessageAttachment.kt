package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface MessageAttachment {
    val content: AttachmentContent

    val format: String
    val mimeType: String
    val fileName: String?

    @Serializable
    data class Image(
        override val content: AttachmentContent,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?,
    ) : MessageAttachment

    @Serializable
    data class Video(
        override val content: AttachmentContent,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment

    @Serializable
    data class Audio(
        override val content: AttachmentContent,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment

    @Serializable
    data class File(
        override val content: AttachmentContent,
        override val format: String,
        override val mimeType: String,
        override val fileName: String?
    ) : MessageAttachment
}

sealed interface AttachmentContent {
    /**
     * File uploaded via Files API to providers storage
     */
    @Serializable
    data class RemoteFile(val url: String) : AttachmentContent

    /**
     * File stored locally in Base64 format
     */
    @Serializable
    data class LocalFile(val uri: String) : AttachmentContent
}
