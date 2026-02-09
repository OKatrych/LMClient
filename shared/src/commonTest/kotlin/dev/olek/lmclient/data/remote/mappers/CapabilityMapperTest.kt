package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.llm.LLMCapability
import dev.olek.lmclient.data.models.Model
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CapabilityMapperTest {

    @Test
    fun `Audio maps to domain Audio`() {
        assertEquals(Model.Capability.Audio, LLMCapability.Audio.toDomainModel())
    }

    @Test
    fun `Completion maps to domain Completion`() {
        assertEquals(Model.Capability.Completion, LLMCapability.Completion.toDomainModel())
    }

    @Test
    fun `Document maps to domain Document`() {
        assertEquals(Model.Capability.Document(), LLMCapability.Document.toDomainModel())
    }

    @Test
    fun `MultipleChoices maps to domain MultipleChoices`() {
        assertEquals(Model.Capability.MultipleChoices, LLMCapability.MultipleChoices.toDomainModel())
    }

    @Test
    fun `Schema JSON Basic maps to domain Schema JSON Basic`() {
        assertEquals(Model.Capability.Schema.JSON.Basic, LLMCapability.Schema.JSON.Basic.toDomainModel())
    }

    @Test
    fun `Schema JSON Standard maps to domain Schema JSON Standard`() {
        assertEquals(Model.Capability.Schema.JSON.Standard, LLMCapability.Schema.JSON.Standard.toDomainModel())
    }

    @Test
    fun `Temperature maps to domain Temperature`() {
        assertEquals(Model.Capability.Temperature, LLMCapability.Temperature.toDomainModel())
    }

    @Test
    fun `ToolChoice maps to domain ToolChoice`() {
        assertEquals(Model.Capability.ToolChoice, LLMCapability.ToolChoice.toDomainModel())
    }

    @Test
    fun `Tools maps to domain Tools`() {
        assertEquals(Model.Capability.Tools, LLMCapability.Tools.toDomainModel())
    }

    @Test
    fun `Vision Image maps to domain Vision Image`() {
        assertEquals(Model.Capability.Vision.Image(), LLMCapability.Vision.Image.toDomainModel())
    }

    @Test
    fun `Vision Video maps to domain Vision Video`() {
        assertEquals(Model.Capability.Vision.Video, LLMCapability.Vision.Video.toDomainModel())
    }

    @Test
    fun `Speculation returns null`() {
        assertNull(LLMCapability.Speculation.toDomainModel())
    }

    @Test
    fun `Moderation returns null`() {
        assertNull(LLMCapability.Moderation.toDomainModel())
    }

    @Test
    fun `Embed returns null`() {
        assertNull(LLMCapability.Embed.toDomainModel())
    }

    @Test
    fun `PromptCaching returns null`() {
        assertNull(LLMCapability.PromptCaching.toDomainModel())
    }

    @Test
    fun `OpenAIEndpoint Completions returns null`() {
        assertNull(LLMCapability.OpenAIEndpoint.Completions.toDomainModel())
    }

    @Test
    fun `OpenAIEndpoint Responses returns null`() {
        assertNull(LLMCapability.OpenAIEndpoint.Responses.toDomainModel())
    }

    @Test
    fun `all mappable capabilities survive roundtrip`() {
        val mappableCapabilities = listOf(
            LLMCapability.Audio,
            LLMCapability.Completion,
            LLMCapability.Document,
            LLMCapability.MultipleChoices,
            LLMCapability.Schema.JSON.Basic,
            LLMCapability.Schema.JSON.Standard,
            LLMCapability.Temperature,
            LLMCapability.ToolChoice,
            LLMCapability.Tools,
            LLMCapability.Vision.Image,
            LLMCapability.Vision.Video,
        )

        for (capability in mappableCapabilities) {
            val domain = capability.toDomainModel()!!
            val roundtripped = domain.toKoogModel()
            assertEquals(capability, roundtripped, "Roundtrip failed for $capability")
        }
    }
}
