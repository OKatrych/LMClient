package dev.olek.lmclient.data.remote.messages.koog

import dev.olek.lmclient.data.models.MessageFinishReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class FinishReasonMapperTest {

    private val mapper = FinishReasonMapper()

    @Test
    fun `end_turn maps to Stop`() {
        assertEquals(MessageFinishReason.Stop, mapper.map("end_turn"))
    }

    @Test
    fun `stop maps to Stop`() {
        assertEquals(MessageFinishReason.Stop, mapper.map("stop"))
    }

    @Test
    fun `max_tokens maps to Length`() {
        assertEquals(MessageFinishReason.Length, mapper.map("max_tokens"))
    }

    @Test
    fun `length maps to Length`() {
        assertEquals(MessageFinishReason.Length, mapper.map("length"))
    }

    @Test
    fun `tool_use maps to ToolCalls`() {
        assertEquals(MessageFinishReason.ToolCalls, mapper.map("tool_use"))
    }

    @Test
    fun `tool_calls maps to ToolCalls`() {
        assertEquals(MessageFinishReason.ToolCalls, mapper.map("tool_calls"))
    }

    @Test
    fun `pause_turn maps to PauseTurn`() {
        assertEquals(MessageFinishReason.PauseTurn, mapper.map("pause_turn"))
    }

    @Test
    fun `refusal maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("refusal"))
    }

    @Test
    fun `content_filter maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("content_filter"))
    }

    @Test
    fun `safety maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("safety"))
    }

    @Test
    fun `blocklist maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("blocklist"))
    }

    @Test
    fun `prohibited_content maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("prohibited_content"))
    }

    @Test
    fun `spii maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("spii"))
    }

    @Test
    fun `image_safety maps to ContentFilter`() {
        assertEquals(MessageFinishReason.ContentFilter, mapper.map("image_safety"))
    }

    @Test
    fun `stop_sequence maps to StopSequence`() {
        assertEquals(MessageFinishReason.StopSequence, mapper.map("stop_sequence"))
    }

    @Test
    fun `function_call maps to FunctionCall`() {
        assertEquals(MessageFinishReason.FunctionCall, mapper.map("function_call"))
    }

    @Test
    fun `language maps to Language`() {
        assertEquals(MessageFinishReason.Language, mapper.map("language"))
    }

    @Test
    fun `mapping is case insensitive`() {
        assertEquals(MessageFinishReason.Stop, mapper.map("STOP"))
    }

    @Test
    fun `null input returns null`() {
        assertNull(mapper.map(null))
    }

    @Test
    fun `unknown reason returns Unknown with lowercased reason`() {
        val result = mapper.map("some_custom_reason")
        assertIs<MessageFinishReason.Unknown>(result)
        assertEquals("some_custom_reason", result.reason)
    }

    @Test
    fun `unknown reason in mixed case is lowercased`() {
        val result = mapper.map("SomeCustomReason")
        assertIs<MessageFinishReason.Unknown>(result)
        assertEquals("somecustomreason", result.reason)
    }

    @Test
    fun `empty string returns Unknown`() {
        val result = mapper.map("")
        assertIs<MessageFinishReason.Unknown>(result)
        assertEquals("", result.reason)
    }
}
