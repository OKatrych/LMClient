package dev.olek.lmclient.data.repositories

import dev.olek.lmclient.data.local.AttachmentStore
import dev.olek.lmclient.data.models.AttachmentContentReference
import io.github.vinceglb.filekit.BookmarkData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AttachmentsRepositoryTest {

    private val fakeStore = FakeAttachmentStore()
    private val repository = AttachmentsRepositoryImpl(fakeStore)

    // region processAssistantAttachment tests

    @Test
    fun `processAssistantAttachment stores content and returns LocalFile reference`() = runTest {
        val base64Content = "SGVsbG8gV29ybGQ="
        val mimeType = "text/plain"

        val result = repository.processAssistantAttachment(base64Content, mimeType)

        assertTrue(result is AttachmentContentReference.LocalFile)
        assertEquals(1, fakeStore.savedAttachments.size)
        val saved = fakeStore.savedAttachments.values.first()
        assertEquals(base64Content, saved.base64)
        assertEquals(mimeType, saved.mimeType)
    }

    @Test
    fun `processAssistantAttachment returns reference with correct bookmark bytes`() = runTest {
        val base64Content = "dGVzdCBjb250ZW50"
        val mimeType = "application/octet-stream"

        val result = repository.processAssistantAttachment(base64Content, mimeType)

        val localFile = result as AttachmentContentReference.LocalFile
        assertTrue(fakeStore.savedAttachments.containsKey(localFile.pathBytes.decodeToString()))
    }

    @Test
    fun `processAssistantAttachment handles empty base64`() = runTest {
        val result = repository.processAssistantAttachment("", "text/plain")

        assertTrue(result is AttachmentContentReference.LocalFile)
        val saved = fakeStore.savedAttachments.values.first()
        assertEquals("", saved.base64)
    }

    @Test
    fun `processAssistantAttachment handles various mime types`() = runTest {
        val mimeTypes = listOf(
            "image/png",
            "image/jpeg",
            "application/pdf",
            "video/mp4",
            "audio/mpeg",
        )

        for (mimeType in mimeTypes) {
            fakeStore.reset()
            repository.processAssistantAttachment("content", mimeType)

            val saved = fakeStore.savedAttachments.values.first()
            assertEquals(mimeType, saved.mimeType, "Failed for mimeType: $mimeType")
        }
    }

    // endregion

    // region getAttachmentContent tests

    @Test
    fun `getAttachmentContent retrieves stored content`() = runTest {
        val base64Content = "SGVsbG8gV29ybGQ="
        val reference = repository.processAssistantAttachment(base64Content, "text/plain")

        val result = repository.getAttachmentContent(reference)

        assertEquals(base64Content, result.base64)
    }

    @Test
    fun `getAttachmentContent throws for RemoteFile reference`() = runTest {
        val remoteRef = AttachmentContentReference.RemoteFile("https://example.com/file.png")

        assertFailsWith<IllegalArgumentException> {
            repository.getAttachmentContent(remoteRef)
        }
    }

    @Test
    fun `getAttachmentContent throws when attachment not found`() = runTest {
        val unknownRef = AttachmentContentReference.LocalFile("unknown-key".encodeToByteArray())

        assertFailsWith<NoSuchElementException> {
            repository.getAttachmentContent(unknownRef)
        }
    }

    @Test
    fun `getAttachmentContent works after multiple saves`() = runTest {
        val contents = listOf("content1", "content2", "content3")
        val references = contents.map { content ->
            repository.processAssistantAttachment(content, "text/plain")
        }

        for ((index, ref) in references.withIndex()) {
            val result = repository.getAttachmentContent(ref)
            assertEquals(contents[index], result.base64)
        }
    }

    // endregion

    // region deleteAttachment tests

    @Test
    fun `deleteAttachment removes stored content`() = runTest {
        val reference = repository.processAssistantAttachment("content", "text/plain")
        assertEquals(1, fakeStore.savedAttachments.size)

        repository.deleteAttachment(reference)

        assertEquals(0, fakeStore.savedAttachments.size)
    }

    @Test
    fun `deleteAttachment throws for RemoteFile reference`() = runTest {
        val remoteRef = AttachmentContentReference.RemoteFile("https://example.com/file.png")

        assertFailsWith<IllegalArgumentException> {
            repository.deleteAttachment(remoteRef)
        }
    }

    @Test
    fun `deleteAttachment handles non-existent reference gracefully`() = runTest {
        val unknownRef = AttachmentContentReference.LocalFile("unknown-key".encodeToByteArray())

        // Should not throw
        repository.deleteAttachment(unknownRef)
    }

    @Test
    fun `deleteAttachment only removes specified attachment`() = runTest {
        val ref1 = repository.processAssistantAttachment("content1", "text/plain")
        val ref2 = repository.processAssistantAttachment("content2", "text/plain")
        val ref3 = repository.processAssistantAttachment("content3", "text/plain")
        assertEquals(3, fakeStore.savedAttachments.size)

        repository.deleteAttachment(ref2)

        assertEquals(2, fakeStore.savedAttachments.size)
        // Verify other attachments are still accessible
        assertEquals("content1", repository.getAttachmentContent(ref1).base64)
        assertEquals("content3", repository.getAttachmentContent(ref3).base64)
    }

    // endregion

    // region roundtrip tests

    @Test
    fun `roundtrip assistant attachment preserves content`() = runTest {
        val originalContent = "VGhpcyBpcyBhIHRlc3QgZmlsZSBjb250ZW50IGluIGJhc2U2NA=="
        val mimeType = "application/pdf"

        val reference = repository.processAssistantAttachment(originalContent, mimeType)
        val retrieved = repository.getAttachmentContent(reference)

        assertEquals(originalContent, retrieved.base64)
    }

    @Test
    fun `roundtrip with large content`() = runTest {
        val largeContent = "A".repeat(10_000)

        val reference = repository.processAssistantAttachment(largeContent, "text/plain")
        val retrieved = repository.getAttachmentContent(reference)

        assertEquals(largeContent, retrieved.base64)
    }

    @Test
    fun `roundtrip with special characters in base64`() = runTest {
        // Base64 with +, /, and = characters
        val base64WithSpecialChars = "SGVsbG8rV29ybGQvVGVzdD0="

        val reference = repository.processAssistantAttachment(base64WithSpecialChars, "text/plain")
        val retrieved = repository.getAttachmentContent(reference)

        assertEquals(base64WithSpecialChars, retrieved.base64)
    }

    // endregion
}

/**
 * Fake implementation of [AttachmentStore] for testing.
 */
private class FakeAttachmentStore : AttachmentStore {
    data class SavedAttachment(val base64: String, val mimeType: String)

    val savedAttachments = mutableMapOf<String, SavedAttachment>()
    private var keyCounter = 0

    fun reset() {
        savedAttachments.clear()
        keyCounter = 0
    }

    override suspend fun saveAttachment(base64: String, mimeType: String): BookmarkData {
        val key = "attachment-${keyCounter++}"
        savedAttachments[key] = SavedAttachment(base64, mimeType)
        return BookmarkData(key.encodeToByteArray())
    }

    override suspend fun getAttachmentContent(bookmarkData: BookmarkData): String {
        val key = bookmarkData.bytes.decodeToString()
        val attachment = savedAttachments[key]
            ?: throw NoSuchElementException("Attachment not found: $key")
        return attachment.base64
    }

    override suspend fun removeAttachment(bookmarkData: BookmarkData) {
        val key = bookmarkData.bytes.decodeToString()
        savedAttachments.remove(key)
    }
}
