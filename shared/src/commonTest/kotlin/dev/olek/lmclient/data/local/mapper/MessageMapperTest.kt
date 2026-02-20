package dev.olek.lmclient.data.local.mapper

import dev.olek.lmclient.data.databases.Messages
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.MessageFinishReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MessageMapperTest {

    private val mapper = MessageMapper()

    // region mapToDbModel tests

    @Test
    fun `mapToDbModel maps UserMessage with text content correctly`() {
        val message = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Hello world"),
            attachments = emptyList(),
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("user-123", result.id)
        assertEquals("chat-room-456", result.chat_room_id)
        assertEquals("user", result.role)
        assertEquals("Hello world", result.content_text)
        assertNull(result.content_audio)
        assertEquals("text", result.content_type)
        assertNull(result.finish_reason_type)
        assertNull(result.finish_reason_message)
        assertNull(result.error_type)
        assertNull(result.error_message)
        assertNull(result.token_count)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Stop finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response text"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("assistant-123", result.id)
        assertEquals("system", result.role)
        assertEquals("Response text", result.content_text)
        assertEquals("stop", result.finish_reason_type)
        assertNull(result.finish_reason_message)
        assertNull(result.error_type)
        assertNull(result.error_message)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Length finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Length,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("length", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with ContentFilter finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.ContentFilter,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("content_filter", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with ToolCalls finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.ToolCalls,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("tool_calls", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with FunctionCall finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.FunctionCall,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("functional_call", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with PauseTurn finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.PauseTurn,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("pause_turn", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with StopSequence finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.StopSequence,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("stop_sequence", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Language finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Language,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("language", result.finish_reason_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Unknown finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Unknown("custom_reason"),
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("unknown", result.finish_reason_type)
        assertEquals("custom_reason", result.finish_reason_message)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with null finish reason`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = null,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertNull(result.finish_reason_type)
        assertNull(result.finish_reason_message)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Authentication error`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.Authentication,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("authentication", result.error_type)
        assertNull(result.error_message)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with RateLimit error`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.RateLimit,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("rate_limit", result.error_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with ConnectionIssue error`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.ConnectionIssue,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("connection_issue", result.error_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with PermissionDenied error`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.PermissionDenied,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("permission_denied", result.error_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with Timeout error`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.Timeout,
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("timeout", result.error_type)
    }

    @Test
    fun `mapToDbModel maps AssistantMessage with UnknownError`() {
        val message = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.UnknownError("Something went wrong"),
        )

        val result = mapper.mapToDbModel(message, "chat-room-456")

        assertEquals("unknown", result.error_type)
        assertEquals("Something went wrong", result.error_message)
    }

    // endregion

    // region mapFromDbModel tests

    @Test
    fun `mapFromDbModel maps user message with text content`() {
        val dbModel = Messages(
            id = "user-123",
            chat_room_id = "chat-room-456",
            role = "user",
            content_text = "Hello world",
            content_audio = null,
            content_type = "text",
            finish_reason_type = null,
            finish_reason_message = null,
            error_type = null,
            error_message = null,
            token_count = null,
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel, emptyList())

        val userMessage = result as Message.UserMessage
        assertEquals("user-123", userMessage.id)
        assertEquals(MessageContent.Text("Hello world"), userMessage.content)
        assertEquals(emptyList(), userMessage.attachments)
    }

    @Test
    fun `mapFromDbModel maps user message with attachments`() {
        val dbModel = Messages(
            id = "user-123",
            chat_room_id = "chat-room-456",
            role = "user",
            content_text = "Check this file",
            content_audio = null,
            content_type = "text",
            finish_reason_type = null,
            finish_reason_message = null,
            error_type = null,
            error_message = null,
            token_count = null,
            created_at = "2024-01-01T00:00:00Z",
        )
        val attachments = listOf(
            MessageAttachment(
                content = AttachmentContentReference.RemoteFile("https://example.com/file.pdf"),
                format = "pdf",
                mimeType = "application/pdf",
                fileName = "document.pdf",
            )
        )

        val result = mapper.mapFromDbModel(dbModel, attachments)

        val userMessage = result as Message.UserMessage
        assertEquals(1, userMessage.attachments.size)
        assertEquals("document.pdf", userMessage.attachments[0].fileName)
    }

    @Test
    fun `mapFromDbModel maps assistant message with Stop finish reason`() {
        val dbModel = Messages(
            id = "assistant-123",
            chat_room_id = "chat-room-456",
            role = "system",
            content_text = "Response text",
            content_audio = null,
            content_type = "text",
            finish_reason_type = "stop",
            finish_reason_message = null,
            error_type = null,
            error_message = null,
            token_count = null,
            created_at = "2024-01-01T00:00:00Z",
        )

        val result = mapper.mapFromDbModel(dbModel, emptyList())

        val assistantMessage = result as Message.AssistantMessage
        assertEquals("assistant-123", assistantMessage.id)
        assertEquals(MessageContent.Text("Response text"), assistantMessage.content)
        assertEquals(MessageFinishReason.Stop, assistantMessage.finishReason)
        assertNull(assistantMessage.error)
    }

    @Test
    fun `mapFromDbModel maps assistant message with all finish reasons`() {
        val finishReasonCases = listOf(
            "stop" to MessageFinishReason.Stop,
            "length" to MessageFinishReason.Length,
            "content_filter" to MessageFinishReason.ContentFilter,
            "tool_calls" to MessageFinishReason.ToolCalls,
            "functional_call" to MessageFinishReason.FunctionCall,
            "pause_turn" to MessageFinishReason.PauseTurn,
            "stop_sequence" to MessageFinishReason.StopSequence,
            "language" to MessageFinishReason.Language,
        )

        for ((dbValue, expectedReason) in finishReasonCases) {
            val dbModel = createAssistantDbModel(finishReasonType = dbValue)
            val result = mapper.mapFromDbModel(dbModel, emptyList()) as Message.AssistantMessage
            assertEquals(expectedReason, result.finishReason, "Failed for finish reason: $dbValue")
        }
    }

    @Test
    fun `mapFromDbModel maps assistant message with Unknown finish reason`() {
        val dbModel = createAssistantDbModel(
            finishReasonType = "unknown",
            finishReasonMessage = "custom_reason",
        )

        val result = mapper.mapFromDbModel(dbModel, emptyList()) as Message.AssistantMessage

        assertEquals(MessageFinishReason.Unknown("custom_reason"), result.finishReason)
    }

    @Test
    fun `mapFromDbModel maps assistant message with Unknown finish reason without message`() {
        val dbModel = createAssistantDbModel(
            finishReasonType = "unknown",
            finishReasonMessage = null,
        )

        val result = mapper.mapFromDbModel(dbModel, emptyList()) as Message.AssistantMessage

        assertEquals(MessageFinishReason.Unknown("Unknown"), result.finishReason)
    }

    @Test
    fun `mapFromDbModel maps assistant message with all error types`() {
        val errorCases = listOf(
            "authentication" to LMClientError.Authentication,
            "rate_limit" to LMClientError.RateLimit,
            "connection_issue" to LMClientError.ConnectionIssue,
            "permission_denied" to LMClientError.PermissionDenied,
            "timeout" to LMClientError.Timeout,
        )

        for ((dbValue, expectedError) in errorCases) {
            val dbModel = createAssistantDbModel(errorType = dbValue)
            val result = mapper.mapFromDbModel(dbModel, emptyList()) as Message.AssistantMessage
            assertEquals(expectedError, result.error, "Failed for error type: $dbValue")
        }
    }

    @Test
    fun `mapFromDbModel maps assistant message with UnknownError`() {
        val dbModel = createAssistantDbModel(
            errorType = "unknown",
            errorMessage = "Something went wrong",
        )

        val result = mapper.mapFromDbModel(dbModel, emptyList()) as Message.AssistantMessage

        assertEquals(LMClientError.UnknownError("Something went wrong"), result.error)
    }

    @Test
    fun `mapFromDbModel throws for unknown content type`() {
        val dbModel = Messages(
            id = "user-123",
            chat_room_id = "chat-room-456",
            role = "user",
            content_text = "text",
            content_audio = null,
            content_type = "video",
            finish_reason_type = null,
            finish_reason_message = null,
            error_type = null,
            error_message = null,
            token_count = null,
            created_at = "2024-01-01T00:00:00Z",
        )

        assertFailsWith<IllegalStateException> {
            mapper.mapFromDbModel(dbModel, emptyList())
        }
    }

    @Test
    fun `mapFromDbModel throws for unknown role`() {
        val dbModel = Messages(
            id = "msg-123",
            chat_room_id = "chat-room-456",
            role = "moderator",
            content_text = "text",
            content_audio = null,
            content_type = "text",
            finish_reason_type = null,
            finish_reason_message = null,
            error_type = null,
            error_message = null,
            token_count = null,
            created_at = "2024-01-01T00:00:00Z",
        )

        assertFailsWith<IllegalStateException> {
            mapper.mapFromDbModel(dbModel, emptyList())
        }
    }

    @Test
    fun `mapFromDbModel throws for unknown finish reason type`() {
        val dbModel = createAssistantDbModel(finishReasonType = "invalid_reason")

        assertFailsWith<IllegalStateException> {
            mapper.mapFromDbModel(dbModel, emptyList())
        }
    }

    @Test
    fun `mapFromDbModel throws for unknown error type`() {
        val dbModel = createAssistantDbModel(errorType = "invalid_error")

        assertFailsWith<IllegalStateException> {
            mapper.mapFromDbModel(dbModel, emptyList())
        }
    }

    // endregion

    // region getFinishReasonDbFormat tests

    @Test
    fun `getFinishReasonDbFormat returns correct values for all reasons`() {
        val cases = listOf(
            MessageFinishReason.Stop to ("stop" to null),
            MessageFinishReason.Length to ("length" to null),
            MessageFinishReason.ContentFilter to ("content_filter" to null),
            MessageFinishReason.ToolCalls to ("tool_calls" to null),
            MessageFinishReason.FunctionCall to ("functional_call" to null),
            MessageFinishReason.PauseTurn to ("pause_turn" to null),
            MessageFinishReason.StopSequence to ("stop_sequence" to null),
            MessageFinishReason.Language to ("language" to null),
            MessageFinishReason.Unknown("custom") to ("unknown" to "custom"),
            null to (null to null),
        )

        for ((input, expected) in cases) {
            val result = mapper.getFinishReasonDbFormat(input)
            assertEquals(expected, result, "Failed for: $input")
        }
    }

    // endregion

    // region getErrorDbFormat tests

    @Test
    fun `getErrorDbFormat returns correct values for all errors`() {
        val cases = listOf(
            LMClientError.Authentication to ("authentication" to null),
            LMClientError.RateLimit to ("rate_limit" to null),
            LMClientError.ConnectionIssue to ("connection_issue" to null),
            LMClientError.PermissionDenied to ("permission_denied" to null),
            LMClientError.Timeout to ("timeout" to null),
            LMClientError.UnknownError("error msg") to ("unknown" to "error msg"),
            null to (null to null),
        )

        for ((input, expected) in cases) {
            val result = mapper.getErrorDbFormat(input)
            assertEquals(expected, result, "Failed for: $input")
        }
    }

    // endregion

    // region roundtrip tests

    @Test
    fun `roundtrip UserMessage preserves all data`() {
        val original = Message.UserMessage(
            id = "user-123",
            content = MessageContent.Text("Test message"),
            attachments = emptyList(),
        )

        val dbModel = mapper.mapToDbModel(original, "chat-room-456")
        val restored = mapper.mapFromDbModel(dbModel, emptyList())

        val restoredUser = restored as Message.UserMessage
        assertEquals(original.id, restoredUser.id)
        assertEquals(original.content, restoredUser.content)
        assertEquals(original.attachments, restoredUser.attachments)
    }

    @Test
    fun `roundtrip AssistantMessage preserves all data`() {
        val original = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Response"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )

        val dbModel = mapper.mapToDbModel(original, "chat-room-456")
        val restored = mapper.mapFromDbModel(dbModel, emptyList())

        val restoredAssistant = restored as Message.AssistantMessage
        assertEquals(original.id, restoredAssistant.id)
        assertEquals(original.content, restoredAssistant.content)
        assertEquals(original.finishReason, restoredAssistant.finishReason)
        assertEquals(original.error, restoredAssistant.error)
    }

    @Test
    fun `roundtrip AssistantMessage with error preserves all data`() {
        val original = Message.AssistantMessage(
            id = "assistant-123",
            content = MessageContent.Text("Error response"),
            attachments = emptyList(),
            finishReason = null,
            error = LMClientError.RateLimit,
        )

        val dbModel = mapper.mapToDbModel(original, "chat-room-456")
        val restored = mapper.mapFromDbModel(dbModel, emptyList())

        val restoredAssistant = restored as Message.AssistantMessage
        assertEquals(original.error, restoredAssistant.error)
    }

    // endregion

    private fun createAssistantDbModel(
        id: String = "assistant-123",
        finishReasonType: String? = null,
        finishReasonMessage: String? = null,
        errorType: String? = null,
        errorMessage: String? = null,
    ) = Messages(
        id = id,
        chat_room_id = "chat-room-456",
        role = "system",
        content_text = "Response",
        content_audio = null,
        content_type = "text",
        finish_reason_type = finishReasonType,
        finish_reason_message = finishReasonMessage,
        error_type = errorType,
        error_message = errorMessage,
        token_count = null,
        created_at = "2024-01-01T00:00:00Z",
    )
}
