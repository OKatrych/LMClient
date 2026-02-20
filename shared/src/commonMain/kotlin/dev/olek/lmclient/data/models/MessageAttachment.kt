package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageAttachment(
    val content: AttachmentContentReference,
    val format: String,
    val mimeType: String,
    val fileName: String?,
)

sealed interface AttachmentContentReference {
    @Serializable
    data class RemoteFile(val url: String) : AttachmentContentReference
    @Serializable
    data class LocalFile(val pathBytes: ByteArray) : AttachmentContentReference {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as LocalFile
            return pathBytes.contentEquals(other.pathBytes)
        }

        override fun hashCode(): Int {
            return pathBytes.contentHashCode()
        }
    }
}

val AttachmentContentReference.id: String
    get() = when (this) {
        is AttachmentContentReference.LocalFile -> pathBytes.decodeToString()
        is AttachmentContentReference.RemoteFile -> url
    }
