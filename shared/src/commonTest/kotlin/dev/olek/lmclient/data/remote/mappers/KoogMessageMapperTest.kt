@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.message.AttachmentContent as KoogAttachmentContent
import ai.koog.prompt.message.ContentPart
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.MessageFinishReason
import dev.olek.lmclient.data.remote.messages.koog.FinishReasonMapper
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import ai.koog.prompt.message.Message as KoogMessage

class KoogMessageMapperTest {

    private val fakeRepository = FakeAttachmentsRepository()
    private val attachmentMapper = KoogAttachmentMapper(fakeRepository)
    private val finishReasonMapper = FinishReasonMapper()
    private val mapper = KoogMessageMapper(finishReasonMapper, attachmentMapper)

    // region map KoogMessage to domain Message tests

    @Test
    fun `map converts KoogMessage User to UserMessage`() = runTest {
        val koogMessage = KoogMessage.User(
            parts = listOf(ContentPart.Text("Hello, how are you?")),
            metaInfo = RequestMetaInfo.Empty,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.UserMessage>(result)
        val textContent = result.content as MessageContent.Text
        assertEquals("Hello, how are you?", textContent.text)
        assertNotNull(result.id)
    }

    @Test
    fun `map converts KoogMessage User with empty parts`() = runTest {
        val koogMessage = KoogMessage.User(
            parts = emptyList(),
            metaInfo = RequestMetaInfo.Empty,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.UserMessage>(result)
        val textContent = result.content as MessageContent.Text
        assertEquals("", textContent.text)
    }

    @Test
    fun `map converts KoogMessage User with image attachment`() = runTest {
        val koogMessage = KoogMessage.User(
            parts = listOf(
                ContentPart.Text("Check this image"),
                ContentPart.Image(
                    content = KoogAttachmentContent.URL("https://example.com/image.png"),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "photo.png",
                ),
            ),
            metaInfo = RequestMetaInfo.Empty,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.UserMessage>(result)
        val textContent = result.content as MessageContent.Text
        assertEquals("Check this image", textContent.text)
        assertEquals(1, result.attachments.size)
        assertEquals("image/png", result.attachments[0].mimeType)
    }

    @Test
    fun `map converts KoogMessage User with multiple attachments`() = runTest {
        val koogMessage = KoogMessage.User(
            parts = listOf(
                ContentPart.Text("Multiple files"),
                ContentPart.Image(
                    content = KoogAttachmentContent.URL("https://example.com/img1.png"),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "image1.png",
                ),
                ContentPart.File(
                    content = KoogAttachmentContent.URL("https://example.com/doc.pdf"),
                    format = "pdf",
                    mimeType = "application/pdf",
                    fileName = "document.pdf",
                ),
            ),
            metaInfo = RequestMetaInfo.Empty,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.UserMessage>(result)
        assertEquals(2, result.attachments.size)
    }

    @Test
    fun `map converts KoogMessage User filters out text content from attachments`() = runTest {
        val koogMessage = KoogMessage.User(
            parts = listOf(
                ContentPart.Text("First part"),
                ContentPart.Text("Second part"),
            ),
            metaInfo = RequestMetaInfo.Empty,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.UserMessage>(result)
        assertEquals(0, result.attachments.size)
    }

    @Test
    fun `map converts KoogMessage Assistant to AssistantMessage`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Hello! I can help you with that.",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "stop",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        val textContent = result.content as MessageContent.Text
        assertEquals("Hello! I can help you with that.", textContent.text)
        assertEquals(MessageFinishReason.Stop, result.finishReason)
        assertNull(result.error)
        assertNotNull(result.id)
    }

    @Test
    fun `map converts KoogMessage Assistant with end_turn finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Response",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "end_turn",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(MessageFinishReason.Stop, result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with length finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Truncated response",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "length",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(MessageFinishReason.Length, result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with max_tokens finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Truncated",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "max_tokens",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(MessageFinishReason.Length, result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with tool_use finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Using tool",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "tool_use",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(MessageFinishReason.ToolCalls, result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with content_filter finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Filtered content",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "content_filter",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(MessageFinishReason.ContentFilter, result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with null finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Streaming response",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = null,
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertNull(result.finishReason)
    }

    @Test
    fun `map converts KoogMessage Assistant with unknown finish reason`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            content = "Response",
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "some_unknown_reason",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        val finishReason = result.finishReason
        assertIs<MessageFinishReason.Unknown>(finishReason)
        assertEquals("some_unknown_reason", finishReason.reason)
    }

    @Test
    fun `map converts KoogMessage Assistant with attachments`() = runTest {
        val koogMessage = KoogMessage.Assistant(
            parts = listOf(
                ContentPart.Text("Here is an image"),
                ContentPart.Image(
                    content = KoogAttachmentContent.URL("https://example.com/generated.png"),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "generated.png",
                ),
            ),
            metaInfo = ResponseMetaInfo.Empty,
            finishReason = "stop",
        )

        val result = mapper.map(koogMessage)

        assertIs<Message.AssistantMessage>(result)
        assertEquals(1, result.attachments.size)
        assertEquals("image/png", result.attachments[0].mimeType)
    }

    @Test
    fun `map throws for KoogMessage System`() = runTest {
        val koogMessage = KoogMessage.System(
            content = "System prompt",
            metaInfo = RequestMetaInfo.Empty,
        )

        assertFailsWith<IllegalStateException> {
            mapper.map(koogMessage)
        }
    }

    @Test
    fun `map throws for KoogMessage Tool Result`() = runTest {
        val koogMessage = KoogMessage.Tool.Result(
            id = "tool-id",
            tool = "calculator",
            content = "42",
            metaInfo = RequestMetaInfo.Empty,
        )

        assertFailsWith<IllegalStateException> {
            mapper.map(koogMessage)
        }
    }

    @Test
    fun `map throws for KoogMessage Tool Call`() = runTest {
        val koogMessage = KoogMessage.Tool.Call(
            id = "call-id",
            tool = "calculator",
            content = "{\"a\": 1, \"b\": 2}",
            metaInfo = ResponseMetaInfo.Empty,
        )

        assertFailsWith<IllegalStateException> {
            mapper.map(koogMessage)
        }
    }

    // endregion

    // region map domain Message to KoogMessage tests

    @Test
    fun `map converts UserMessage with text to KoogMessage User`() = runTest {
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Hello there"),
            attachments = emptyList(),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(1, result.parts.size)
        val textPart = result.parts[0] as ContentPart.Text
        assertEquals("Hello there", textPart.text)
    }

    @Test
    fun `map converts UserMessage with blank text correctly`() = runTest {
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("   "),
            attachments = emptyList(),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(0, result.parts.size)
    }

    @Test
    fun `map converts UserMessage with empty text correctly`() = runTest {
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text(""),
            attachments = emptyList(),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(0, result.parts.size)
    }

    @Test
    fun `map converts UserMessage with attachments to KoogMessage User`() = runTest {
        fakeRepository.getAttachmentContentResult = AttachmentsRepository.AttachmentContent("aW1hZ2UtZGF0YQ==")
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Check this file"),
            attachments = listOf(
                MessageAttachment(
                    content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "photo.png",
                )
            ),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(2, result.parts.size)
        assertIs<ContentPart.Text>(result.parts[0])
        assertIs<ContentPart.Image>(result.parts[1])
    }

    @Test
    fun `map converts UserMessage with multiple attachments`() = runTest {
        fakeRepository.getAttachmentContentResult = AttachmentsRepository.AttachmentContent("ZGF0YQ==")
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Files"),
            attachments = listOf(
                MessageAttachment(
                    content = AttachmentContentReference.LocalFile("path1".encodeToByteArray()),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "image.png",
                ),
                MessageAttachment(
                    content = AttachmentContentReference.RemoteFile("https://example.com/doc.pdf"),
                    format = "pdf",
                    mimeType = "application/pdf",
                    fileName = "doc.pdf",
                ),
            ),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(3, result.parts.size)
        assertIs<ContentPart.Text>(result.parts[0])
        assertIs<ContentPart.Image>(result.parts[1])
        assertIs<ContentPart.File>(result.parts[2])
    }

    @Test
    fun `map converts UserMessage with only attachments and blank text`() = runTest {
        fakeRepository.getAttachmentContentResult = AttachmentsRepository.AttachmentContent("ZGF0YQ==")
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text(""),
            attachments = listOf(
                MessageAttachment(
                    content = AttachmentContentReference.LocalFile("path".encodeToByteArray()),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "image.png",
                ),
            ),
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.User>(result)
        assertEquals(1, result.parts.size)
        assertIs<ContentPart.Image>(result.parts[0])
    }

    @Test
    fun `map converts AssistantMessage with text to KoogMessage Assistant`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Here is my response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertEquals("Here is my response", result.content)
        assertEquals("stop", result.finishReason)
    }

    @Test
    fun `map converts AssistantMessage with Length finish reason`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Truncated"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Length,
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertEquals("length", result.finishReason)
    }

    @Test
    fun `map converts AssistantMessage with ContentFilter finish reason`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Filtered"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.ContentFilter,
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertEquals("content_filter", result.finishReason)
    }

    @Test
    fun `map converts AssistantMessage with ToolCalls finish reason`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Calling tool"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.ToolCalls,
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertEquals("tool_calls", result.finishReason)
    }

    @Test
    fun `map converts AssistantMessage with null finish reason`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Streaming"),
            attachments = emptyList(),
            finishReason = null,
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertNull(result.finishReason)
    }

    @Test
    fun `map converts AssistantMessage with Unknown finish reason`() = runTest {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Unknown("custom_reason"),
            error = null,
        )

        val result = mapper.map(message)

        assertIs<KoogMessage.Assistant>(result)
        assertEquals("custom_reason", result.finishReason)
    }

    // endregion

    // region roundtrip tests

    @Test
    fun `roundtrip UserMessage preserves text content`() = runTest {
        val original = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Test message"),
            attachments = emptyList(),
        )

        val koog = mapper.map(original)
        val restored = mapper.map(koog)

        assertIs<Message.UserMessage>(restored)
        val originalText = original.content as MessageContent.Text
        val restoredText = restored.content as MessageContent.Text
        assertEquals(originalText.text, restoredText.text)
    }

    @Test
    fun `roundtrip AssistantMessage preserves text and finish reason`() = runTest {
        val original = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response text"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )

        val koog = mapper.map(original)
        val restored = mapper.map(koog)

        assertIs<Message.AssistantMessage>(restored)
        val originalText = original.content as MessageContent.Text
        val restoredText = restored.content as MessageContent.Text
        assertEquals(originalText.text, restoredText.text)
        assertEquals(original.finishReason, restored.finishReason)
    }

    @Test
    fun `roundtrip AssistantMessage with Length finish reason`() = runTest {
        val original = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Truncated response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Length,
            error = null,
        )

        val koog = mapper.map(original)
        val restored = mapper.map(koog)

        assertIs<Message.AssistantMessage>(restored)
        assertEquals(MessageFinishReason.Length, restored.finishReason)
    }

    @Test
    fun `roundtrip UserMessage with remote attachment preserves data`() = runTest {
        val original = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Check this"),
            attachments = listOf(
                MessageAttachment(
                    content = AttachmentContentReference.RemoteFile("https://example.com/image.png"),
                    format = "png",
                    mimeType = "image/png",
                    fileName = "image.png",
                )
            ),
        )

        val koog = mapper.map(original)
        val restored = mapper.map(koog)

        assertIs<Message.UserMessage>(restored)
        assertEquals(1, restored.attachments.size)
        val originalRemote = original.attachments[0].content as AttachmentContentReference.RemoteFile
        val restoredRemote = restored.attachments[0].content as AttachmentContentReference.RemoteFile
        assertEquals(originalRemote.url, restoredRemote.url)
        assertEquals(original.attachments[0].mimeType, restored.attachments[0].mimeType)
    }

    // endregion
}
