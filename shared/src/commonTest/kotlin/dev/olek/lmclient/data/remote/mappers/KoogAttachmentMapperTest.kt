package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.AttachmentContent as KoogAttachmentContent
import ai.koog.prompt.message.ContentPart
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class KoogAttachmentMapperTest {

    private val fakeRepository = FakeAttachmentsRepository()
    private val mapper = KoogAttachmentMapper(fakeRepository)

    // region mapToDomain tests

    @Test
    fun `mapToDomain maps Image ContentPart with URL correctly`() = runTest {
        val imagePart = ContentPart.Image(
            content = KoogAttachmentContent.URL("https://example.com/image.png"),
            format = "png",
            mimeType = "image/png",
            fileName = "screenshot.png",
        )

        val result = mapper.mapToDomain(imagePart)!!

        val remoteContent = result.content as AttachmentContentReference.RemoteFile
        assertEquals("https://example.com/image.png", remoteContent.url)
        assertEquals("png", result.format)
        assertEquals("image/png", result.mimeType)
        assertEquals("screenshot.png", result.fileName)
    }

    @Test
    fun `mapToDomain maps Image ContentPart with Base64 correctly`() = runTest {
        fakeRepository.processAssistantAttachmentResult = AttachmentContentReference.LocalFile(
            pathBytes = "test-path".encodeToByteArray()
        )
        val imagePart = ContentPart.Image(
            content = KoogAttachmentContent.Binary.Base64("SGVsbG8gV29ybGQ="),
            format = "jpg",
            mimeType = "image/jpeg",
            fileName = "photo.jpg",
        )

        val result = mapper.mapToDomain(imagePart)!!

        val localContent = result.content as AttachmentContentReference.LocalFile
        assertEquals("test-path", localContent.pathBytes.decodeToString())
        assertEquals("jpg", result.format)
        assertEquals("image/jpeg", result.mimeType)
        assertEquals("photo.jpg", result.fileName)
        assertEquals("SGVsbG8gV29ybGQ=", fakeRepository.lastProcessedBase64)
        assertEquals("image/jpeg", fakeRepository.lastProcessedMimeType)
    }

    @Test
    fun `mapToDomain maps Image ContentPart with Bytes correctly`() = runTest {
        fakeRepository.processAssistantAttachmentResult = AttachmentContentReference.LocalFile(
            pathBytes = "bytes-path".encodeToByteArray()
        )
        val imagePart = ContentPart.Image(
            content = KoogAttachmentContent.Binary.Bytes(byteArrayOf(1, 2, 3, 4)),
            format = "gif",
            mimeType = "image/gif",
            fileName = "animation.gif",
        )

        val result = mapper.mapToDomain(imagePart)!!

        assertIs<AttachmentContentReference.LocalFile>(result.content)
        assertEquals("gif", result.format)
        assertEquals("image/gif", result.mimeType)
    }

    @Test
    fun `mapToDomain maps Audio ContentPart correctly`() = runTest {
        val audioPart = ContentPart.Audio(
            content = KoogAttachmentContent.URL("https://example.com/audio.mp3"),
            format = "mp3",
            mimeType = "audio/mpeg",
            fileName = "music.mp3",
        )

        val result = mapper.mapToDomain(audioPart)!!

        val remoteContent = result.content as AttachmentContentReference.RemoteFile
        assertEquals("https://example.com/audio.mp3", remoteContent.url)
        assertEquals("mp3", result.format)
        assertEquals("audio/mpeg", result.mimeType)
        assertEquals("music.mp3", result.fileName)
    }

    @Test
    fun `mapToDomain maps Video ContentPart correctly`() = runTest {
        val videoPart = ContentPart.Video(
            content = KoogAttachmentContent.URL("https://example.com/video.mp4"),
            format = "mp4",
            mimeType = "video/mp4",
            fileName = "clip.mp4",
        )

        val result = mapper.mapToDomain(videoPart)!!

        val remoteContent = result.content as AttachmentContentReference.RemoteFile
        assertEquals("https://example.com/video.mp4", remoteContent.url)
        assertEquals("mp4", result.format)
        assertEquals("video/mp4", result.mimeType)
        assertEquals("clip.mp4", result.fileName)
    }

    @Test
    fun `mapToDomain maps File ContentPart correctly`() = runTest {
        val filePart = ContentPart.File(
            content = KoogAttachmentContent.URL("https://example.com/document.pdf"),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "report.pdf",
        )

        val result = mapper.mapToDomain(filePart)!!

        val remoteContent = result.content as AttachmentContentReference.RemoteFile
        assertEquals("https://example.com/document.pdf", remoteContent.url)
        assertEquals("pdf", result.format)
        assertEquals("application/pdf", result.mimeType)
        assertEquals("report.pdf", result.fileName)
    }

    @Test
    fun `mapToDomain returns null for Text ContentPart`() = runTest {
        val textPart = ContentPart.Text("Hello World")

        val result = mapper.mapToDomain(textPart)

        assertNull(result)
    }

    @Test
    fun `mapToDomain handles null fileName`() = runTest {
        val imagePart = ContentPart.Image(
            content = KoogAttachmentContent.URL("https://example.com/image"),
            format = "png",
            mimeType = "image/png",
            fileName = null,
        )

        val result = mapper.mapToDomain(imagePart)!!

        assertNull(result.fileName)
    }

    // endregion

    // region mapToKoog tests

    @Test
    fun `mapToKoog maps image attachment to Image ContentPart`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("aW1hZ2UtZGF0YQ==")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("local-path".encodeToByteArray()),
            format = "png",
            mimeType = "image/png",
            fileName = "photo.png",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Image>(result)
        val base64Content = result.content as KoogAttachmentContent.Binary.Base64
        assertEquals("aW1hZ2UtZGF0YQ==", base64Content.base64)
        assertEquals("png", result.format)
        assertEquals("image/png", result.mimeType)
        assertEquals("photo.png", result.fileName)
    }

    @Test
    fun `mapToKoog maps image jpeg attachment correctly`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("anBlZy1kYXRh")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "jpg",
            mimeType = "image/jpeg",
            fileName = "photo.jpg",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Image>(result)
    }

    @Test
    fun `mapToKoog maps audio attachment to Audio ContentPart`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("YXVkaW8tZGF0YQ==")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "mp3",
            mimeType = "audio/mpeg",
            fileName = "song.mp3",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Audio>(result)
        assertEquals("mp3", result.format)
        assertEquals("audio/mpeg", result.mimeType)
        assertEquals("song.mp3", result.fileName)
    }

    @Test
    fun `mapToKoog maps audio wav attachment correctly`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("d2F2LWRhdGE=")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "wav",
            mimeType = "audio/wav",
            fileName = "recording.wav",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Audio>(result)
    }

    @Test
    fun `mapToKoog maps video attachment to Video ContentPart`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("dmlkZW8tZGF0YQ==")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "mp4",
            mimeType = "video/mp4",
            fileName = "clip.mp4",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Video>(result)
        assertEquals("mp4", result.format)
        assertEquals("video/mp4", result.mimeType)
        assertEquals("clip.mp4", result.fileName)
    }

    @Test
    fun `mapToKoog maps video webm attachment correctly`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("d2VibS1kYXRh")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "webm",
            mimeType = "video/webm",
            fileName = "video.webm",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Video>(result)
    }

    @Test
    fun `mapToKoog maps other mimeType attachment to File ContentPart`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("cGRmLWRhdGE=")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "document.pdf",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.File>(result)
        assertEquals("pdf", result.format)
        assertEquals("application/pdf", result.mimeType)
        assertEquals("document.pdf", result.fileName)
    }

    @Test
    fun `mapToKoog maps text plain attachment to File ContentPart`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("dGV4dC1kYXRh")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "txt",
            mimeType = "text/plain",
            fileName = "notes.txt",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.File>(result)
    }

    @Test
    fun `mapToKoog maps remote file attachment using URL content`() = runTest {
        val attachment = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://cdn.example.com/file.png"),
            format = "png",
            mimeType = "image/png",
            fileName = "remote-image.png",
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.Image>(result)
        val urlContent = result.content as KoogAttachmentContent.URL
        assertEquals("https://cdn.example.com/file.png", urlContent.url)
    }

    @Test
    fun `mapToKoog handles null fileName`() = runTest {
        fakeRepository.getAttachmentContentResult =
            AttachmentsRepository.AttachmentContent("ZGF0YQ==")
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
            format = "bin",
            mimeType = "application/octet-stream",
            fileName = null,
        )

        val result = mapper.mapToKoog(attachment)

        assertIs<ContentPart.File>(result)
        assertNull(result.fileName)
    }

    @Test
    fun `mapToKoog returns null when local file content not found`() = runTest {
        fakeRepository.getAttachmentContentResult = null
        val attachment = MessageAttachment(
            content = AttachmentContentReference.LocalFile("missing-path".encodeToByteArray()),
            format = "png",
            mimeType = "image/png",
            fileName = "missing.png",
        )

        val result = mapper.mapToKoog(attachment)

        assertNull(result)
    }

    // endregion

    // region roundtrip tests

    @Test
    fun `roundtrip image attachment preserves data`() = runTest {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/image.jpg"),
            format = "jpg",
            mimeType = "image/jpeg",
            fileName = "photo.jpg",
        )

        val koogPart = mapper.mapToKoog(original)!!
        val restored = mapper.mapToDomain(koogPart)!!

        val originalRemote = original.content as AttachmentContentReference.RemoteFile
        val restoredRemote = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalRemote.url, restoredRemote.url)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip audio attachment preserves data`() = runTest {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/audio.mp3"),
            format = "mp3",
            mimeType = "audio/mpeg",
            fileName = "song.mp3",
        )

        val koogPart = mapper.mapToKoog(original)!!
        val restored = mapper.mapToDomain(koogPart)!!

        val originalRemote = original.content as AttachmentContentReference.RemoteFile
        val restoredRemote = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalRemote.url, restoredRemote.url)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip video attachment preserves data`() = runTest {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/video.mp4"),
            format = "mp4",
            mimeType = "video/mp4",
            fileName = "clip.mp4",
        )

        val koogPart = mapper.mapToKoog(original)!!
        val restored = mapper.mapToDomain(koogPart)!!

        val originalRemote = original.content as AttachmentContentReference.RemoteFile
        val restoredRemote = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalRemote.url, restoredRemote.url)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    @Test
    fun `roundtrip file attachment preserves data`() = runTest {
        val original = MessageAttachment(
            content = AttachmentContentReference.RemoteFile("https://example.com/doc.pdf"),
            format = "pdf",
            mimeType = "application/pdf",
            fileName = "document.pdf",
        )

        val koogPart = mapper.mapToKoog(original)!!
        val restored = mapper.mapToDomain(koogPart)!!

        val originalRemote = original.content as AttachmentContentReference.RemoteFile
        val restoredRemote = restored.content as AttachmentContentReference.RemoteFile
        assertEquals(originalRemote.url, restoredRemote.url)
        assertEquals(original.format, restored.format)
        assertEquals(original.mimeType, restored.mimeType)
        assertEquals(original.fileName, restored.fileName)
    }

    // endregion
}
