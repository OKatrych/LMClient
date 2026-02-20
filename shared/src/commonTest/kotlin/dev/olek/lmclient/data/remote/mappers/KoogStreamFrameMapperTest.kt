package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.streaming.StreamFrame
import dev.olek.lmclient.data.models.MessageFinishReason
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.messages.koog.FinishReasonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class KoogStreamFrameMapperTest {

    private val finishReasonMapper = FinishReasonMapper()
    private val mapper = KoogStreamFrameMapper(finishReasonMapper)

    @Test
    fun `Append maps to Chunk with correct id and content`() {
        val result = mapper.map("msg-1", StreamFrame.Append("Hello world"))

        assertIs<PromptApi.MessageStreamResult.Chunk>(result)
        assertEquals("msg-1", result.id)
        assertEquals("Hello world", result.content)
    }

    @Test
    fun `Append with empty content maps to Chunk with empty content`() {
        val result = mapper.map("msg-1", StreamFrame.Append(""))

        assertIs<PromptApi.MessageStreamResult.Chunk>(result)
        assertEquals("", result.content)
    }

    @Test
    fun `End maps to Finished with mapped finish reason`() {
        val result = mapper.map("msg-1", StreamFrame.End("stop"))

        assertIs<PromptApi.MessageStreamResult.Finished>(result)
        assertEquals("msg-1", result.id)
        assertEquals(MessageFinishReason.Stop, result.reason)
    }

    @Test
    fun `End with null finish reason maps to Finished with null reason`() {
        val result = mapper.map("msg-1", StreamFrame.End(null))

        assertIs<PromptApi.MessageStreamResult.Finished>(result)
        assertNull(result.reason)
    }

    @Test
    fun `End with max_tokens maps to Finished with Length reason`() {
        val result = mapper.map("msg-1", StreamFrame.End("max_tokens"))

        assertIs<PromptApi.MessageStreamResult.Finished>(result)
        assertEquals(MessageFinishReason.Length, result.reason)
    }

    @Test
    fun `ToolCall throws NotImplementedError`() {
        assertFailsWith<NotImplementedError> {
            mapper.map("msg-1", StreamFrame.ToolCall("tool-id", "calculator", "{}"))
        }
    }

}
