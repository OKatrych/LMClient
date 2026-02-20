@file:OptIn(ExperimentalEncodingApi::class)

package dev.olek.lmclient.data.local.mapper

import dev.olek.lmclient.data.databases.MessageAttachments
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MessageAttachmentMapperTest {

    private val mapper = MessageAttachmentMapper()

    // region mapToDbModel tests

    @Test
    fun `mapToDbModel maps local file attachment correctly`() {
        val pathBytes = "/path/to/file.pdf".encodeToByteArray()
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile(pathBytes),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "document.pdf",
        )

        val result = mapper.mapToDbModel(attachment, "message-123")

        assertEquals("message-123", result.message_id)
        assertEquals("local", result.content_reference_type)
        assertEquals(Base64.encode(pathBytes), result.content_reference)
        assertEquals("pdf", result.file_type)
        assertEquals("application/pdf", result.mime_type)
        assertEquals("document.pdf", result.file_name)
        assertTrue(result.id.isNotEmpty())
    }

    @Test
    fun `mapToDbModel maps remote file attachment correctly`() {
        val attachment = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/image.png"),
            format = "png",
            mimeType = "image/png",
            fileName = "screenshot.png",
        )

        val result = mapper.mapToDbModel(attachment, "message-456")

        assertEquals("message-456", result.message_id)
        assertEquals("remote", result.content_reference_type)
        assertEquals("https://example.com/image.png", result.content_reference)
        assertEquals("png", result.file_type)
        assertEquals("image/png", result.mime_type)
        assertEquals("screenshot.png", result.file_name)
    }

    @Test
    fun `mapToDbModel handles null fileName`() {
        val attachment = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/file"),
            format = "bin",
            mimeType = "application/octet-stream",
            fileName = null,
        )

        val result = mapper.mapToDbModel(attachment, "message-789")

        assertNull(result.file_name)
    }

    @Test
    fun `mapToDbModel generates unique IDs for different calls`() {
        val attachment = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/file"),
            format = "txt",
            mimeType = "text/plain",
            fileName = "file.txt",
        )

        val result1 = mapper.mapToDbModel(attachment, "message-1")
        val result2 = mapper.mapToDbModel(attachment, "message-1")

        assertNotEquals(result1.id, result2.id)
    }

    @Test
    fun `mapToDbModel handles empty format and mimeType`() {
        val attachment = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/unknown"),
            format = "",
            mimeType = "",
            fileName = "unknown",
        )

        val result = mapper.mapToDbModel(attachment, "message-123")

        assertEquals("", result.file_type)
        assertEquals("", result.mime_type)
    }

    @Test
    fun `mapToDbModel handles binary path bytes`() {
        val pathBytes = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile(pathBytes),
            format = "bin",
            mimeType = "application/octet-stream",
            fileName = "binary.bin",
        )

        val result = mapper.mapToDbModel(attachment, "message-123")

        assertEquals("local", result.content_reference_type)
        // Verify the base64 encoding is valid and reversible
        assertContentEquals(pathBytes, Base64.decode(result.content_reference))
    }

    // endregion

    // region mapFromDbModel tests

    @Test
    fun `mapFromDbModel maps local file attachment correctly`() {
        val pathBytes = "/path/to/document.pdf".encodeToByteArray()
        val dbModel = MessageAttachments(
            id = "attachment-123",
            message_id = "message-456",
            content_reference_type = "local",
            content_reference = Base64.encode(pathBytes),
            file_type = "pdf",
            mime_type = "application/pdf",
            file_name = "document.pdf",
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel)

        val localFile = result.content as AttachmentContentReference.LocalFile
        assertContentEquals(pathBytes, localFile.pathBytes)
        assertEquals("pdf", result.format)
        assertEquals("application/pdf", result.mimeType)
        assertEquals("document.pdf", result.fileName)
    }

    @Test
    fun `mapFromDbModel maps remote file attachment correctly`() {
        val dbModel = MessageAttachments(
            id = "attachment-123",
            message_id = "message-456",
            content_reference_type = "remote",
            content_reference = "https://cdn.example.com/files/image.jpg",
            file_type = "jpg",
            mime_type = "image/jpeg",
            file_name = "vacation.jpg",
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel)

        val remoteFile = result.content as AttachmentContentReference.RemoteFile
        assertEquals("https://cdn.example.com/files/image.jpg", remoteFile.url)
        assertEquals("jpg", result.format)
        assertEquals("image/jpeg", result.mimeType)
        assertEquals("vacation.jpg", result.fileName)
    }

    @Test
    fun `mapFromDbModel handles null file_type and mime_type`() {
        val dbModel = MessageAttachments(
            id = "attachment-123",
            message_id = "message-456",
            content_reference_type = "remote",
            content_reference = "https://example.com/unknown",
            file_type = null,
            mime_type = null,
            file_name = null,
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel)

        assertEquals("", result.format)
        assertEquals("", result.mimeType)
        assertNull(result.fileName)
    }

    @Test
    fun `mapFromDbModel throws for unknown content reference type`() {
        val dbModel = MessageAttachments(
            id = "attachment-123",
            message_id = "message-456",
            content_reference_type = "cloud",
            content_reference = "cloud://bucket/file",
            file_type = "txt",
            mime_type = "text/plain",
            file_name = "file.txt",
            created_at = "2024-01-01T00:00:00Z",
        )

        assertFailsWith<IllegalStateException> {
            mapper.mapFromDbModel(dbModel)
        }
    }

    @Test
    fun `mapFromDbModel handles binary data in local file`() {
        val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
        val dbModel = MessageAttachments(
            id = "attachment-123",
            message_id = "message-456",
            content_reference_type = "local",
            content_reference = Base64.encode(binaryData),
            file_type = "bin",
            mime_type = "application/octet-stream",
            file_name = "data.bin",
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel)

        val localFile = result.content as AttachmentContentReference.LocalFile
        assertContentEquals(binaryData, localFile.pathBytes)
    }

    // endregion

    // region roundtrip tests

    @Test
    fun `roundtrip local file attachment preserves all data`() {
        val pathBytes = "/Users/test/Documents/report.pdf".encodeToByteArray()
        val original = MessageAttachment(
            content = AttachmentContentReference.LocalFile(pathBytes),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "report.pdf",
        )

        val dbModel = mapper.mapToDbModel(original, "message-123")
        val restored = mapper.mapFromDbModel(dbModel)

        val originalContent = original.content as AttachmentContentReference.LocalFile
        val restoredContent = restored.content as AttachmentContentReference.LocalFile
        assertContentEquals(originalContent.pathBytes, restoredContent.pathBytes)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip remote file attachment preserves all data`() {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://storage.example.com/files/document.docx"),
            format = "docx",
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            fileName = "quarterly_report.docx",
        )

        val dbModel = mapper.mapToDbModel(original, "message-456")
        val restored = mapper.mapFromDbModel(dbModel)

        val originalContent = original.content as AttachmentContentReference.RemoteFile
        val restoredContent = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalContent.url, restoredContent.url)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip attachment with null fileName`() {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/api/download/123"),
            format = "json",
            mimeType = "application/json",
            fileName = null,
        )

        val dbModel = mapper.mapToDbModel(original, "message-789")
        val restored = mapper.mapFromDbModel(dbModel)

        assertNull(restored.fileName)
    }

    @Test
    fun `roundtrip attachment with special characters in URL`() {
        val original = MessageAttachment(
            content = AttachmentContentReference
                .RemoteFile("https://example.com/files/path%20with%20spaces/file%23name.txt"),
            format = "txt",
            mimeType = "text/plain",
            fileName = "file with spaces.txt",
        )

        val dbModel = mapper.mapToDbModel(original, "message-abc")
        val restored = mapper.mapFromDbModel(dbModel)

        val originalContent = original.content as AttachmentContentReference.RemoteFile
        val restoredContent = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalContent.url, restoredContent.url)
    }

    @Test
    fun `roundtrip attachment with unicode in fileName`() {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/file"),
            format = "txt",
            mimeType = "text/plain",
            fileName = "文档_résumé_😀.txt",
        )

        val dbModel = mapper.mapToDbModel(original, "message-unicode")
        val restored = mapper.mapFromDbModel(dbModel)

        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip local file with unicode path`() {
        val pathBytes = "/Users/用户/Documents/文件.pdf".encodeToByteArray()
        val original = MessageAttachment(
            content = AttachmentContentReference.LocalFile(pathBytes),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "文件.pdf",
        )

        val dbModel = mapper.mapToDbModel(original, "message-unicode-path")
        val restored = mapper.mapFromDbModel(dbModel)

        val originalContent = original.content as AttachmentContentReference.LocalFile
        val restoredContent = restored.content as AttachmentContentReference.LocalFile
        assertContentEquals(originalContent.pathBytes, restoredContent.pathBytes)
    }

    // endregion
}
