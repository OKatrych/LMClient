package dev.olek.lmclient.data.remote.mappers

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.utils.NexosAI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelMapperTest {

    @Test
    fun `toKoogModel preserves id`() {
        val model = createModel(providerId = "openai", id = "gpt-4o")
        val result = model.toKoogModel()
        assertEquals("gpt-4o", result.id)
    }

    @Test
    fun `toKoogModel preserves contextLength`() {
        val model = createModel(providerId = "openai", contextLength = 128000)
        val result = model.toKoogModel()
        assertEquals(128000, result.contextLength)
    }

    @Test
    fun `toKoogModel preserves maxOutputTokens`() {
        val model = createModel(providerId = "openai", maxOutputTokens = 4096)
        val result = model.toKoogModel()
        assertEquals(4096, result.maxOutputTokens)
    }

    @Test
    fun `toKoogModel resolves OpenAI provider`() {
        val model = createModel(providerId = "openai")
        val result = model.toKoogModel()
        assertEquals(LLMProvider.OpenAI, result.provider)
    }

    @Test
    fun `toKoogModel resolves Google provider`() {
        val model = createModel(providerId = "google")
        val result = model.toKoogModel()
        assertEquals(LLMProvider.Google, result.provider)
    }

    @Test
    fun `toKoogModel resolves Claude provider`() {
        val model = createModel(providerId = "claude")
        val result = model.toKoogModel()
        assertEquals(LLMProvider.Anthropic, result.provider)
    }

    @Test
    fun `toKoogModel resolves OpenRouter provider`() {
        val model = createModel(providerId = "openrouter")
        val result = model.toKoogModel()
        assertEquals(LLMProvider.OpenRouter, result.provider)
    }

    @Test
    fun `toKoogModel resolves NexosAI provider`() {
        val model = createModel(providerId = "nexos_ai")
        val result = model.toKoogModel()
        assertEquals(LLMProvider.NexosAI, result.provider)
    }

    @Test
    fun `toKoogModel maps capabilities`() {
        val model = createModel(
            providerId = "google",
            capabilities = listOf(Model.Capability.Temperature, Model.Capability.Tools),
        )
        val result = model.toKoogModel()
        assertTrue(result.capabilities.contains(LLMCapability.Temperature))
        assertTrue(result.capabilities.contains(LLMCapability.Tools))
    }

    @Test
    fun `OpenAI with Completion adds Completions and Responses`() {
        val model = createModel(
            providerId = "openai",
            capabilities = listOf(Model.Capability.Completion),
        )
        val result = model.toKoogModel()
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `OpenAI with Completion and Audio adds only Completions`() {
        val model = createModel(
            providerId = "openai",
            capabilities = listOf(Model.Capability.Completion, Model.Capability.Audio),
        )
        val result = model.toKoogModel()
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `NexosAI with Completion adds Completions and Responses`() {
        val model = createModel(
            providerId = "nexos_ai",
            capabilities = listOf(Model.Capability.Completion),
        )
        val result = model.toKoogModel()
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `OpenRouter with Completion adds Completions and Responses`() {
        val model = createModel(
            providerId = "openrouter",
            capabilities = listOf(Model.Capability.Completion),
        )
        val result = model.toKoogModel()
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertTrue(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `Google with Completion does not add OpenAI capabilities`() {
        val model = createModel(
            providerId = "google",
            capabilities = listOf(Model.Capability.Completion),
        )
        val result = model.toKoogModel()
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `Claude with Completion does not add OpenAI capabilities`() {
        val model = createModel(
            providerId = "claude",
            capabilities = listOf(Model.Capability.Completion),
        )
        val result = model.toKoogModel()
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `OpenAI without Completion does not add OpenAI endpoint capabilities`() {
        val model = createModel(
            providerId = "openai",
            capabilities = listOf(Model.Capability.Temperature, Model.Capability.Tools),
        )
        val result = model.toKoogModel()
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Completions))
        assertFalse(result.capabilities.contains(LLMCapability.OpenAIEndpoint.Responses))
    }

    @Test
    fun `toDomainModel preserves id`() {
        val llModel = createLLModel(id = "gpt-4o")
        val result = llModel.toDomainModel()
        assertEquals("gpt-4o", result.id)
    }

    @Test
    fun `toDomainModel sets name equal to id`() {
        val llModel = createLLModel(id = "gpt-4o")
        val result = llModel.toDomainModel()
        assertEquals("gpt-4o", result.name)
    }

    @Test
    fun `toDomainModel preserves contextLength`() {
        val llModel = createLLModel(contextLength = 128000)
        val result = llModel.toDomainModel()
        assertEquals(128000, result.contextLength)
    }

    @Test
    fun `toDomainModel preserves maxOutputTokens`() {
        val llModel = createLLModel(maxOutputTokens = 4096)
        val result = llModel.toDomainModel()
        assertEquals(4096, result.maxOutputTokens)
    }

    @Test
    fun `toDomainModel sets providerId from provider id`() {
        val llModel = createLLModel(provider = LLMProvider.OpenAI)
        val result = llModel.toDomainModel()
        assertEquals("openai", result.providerId)
    }

    @Test
    fun `toDomainModel filters out null capabilities`() {
        val llModel = LLModel(
            provider = LLMProvider.OpenAI,
            id = "model",
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Speculation,
                LLMCapability.Tools,
            ),
            contextLength = 4096,
            maxOutputTokens = 1024,
        )
        val result = llModel.toDomainModel()
        assertEquals(2, result.capabilities.size)
        assertTrue(result.capabilities.contains(Model.Capability.Temperature))
        assertTrue(result.capabilities.contains(Model.Capability.Tools))
    }

    @Test
    fun `roundtrip preserves essential data`() {
        val original = createModel(
            providerId = "google",
            id = "gemini-pro",
            capabilities = listOf(Model.Capability.Temperature, Model.Capability.Vision.Image()),
            contextLength = 32000,
            maxOutputTokens = 8192,
        )
        val koogModel = original.toKoogModel()
        val restored = koogModel.toDomainModel()

        assertEquals(original.id, restored.id)
        assertEquals(original.contextLength, restored.contextLength)
        assertEquals(original.maxOutputTokens, restored.maxOutputTokens)
        assertEquals(original.capabilities, restored.capabilities)
    }

    private fun createModel(
        providerId: String = "openai",
        id: String = "test-model",
        capabilities: List<Model.Capability> = emptyList(),
        contextLength: Long = 4096,
        maxOutputTokens: Long? = null,
    ) = Model(
        id = id,
        providerId = providerId,
        name = id,
        capabilities = capabilities,
        contextLength = contextLength,
        maxOutputTokens = maxOutputTokens,
    )

    private fun createLLModel(
        provider: LLMProvider = LLMProvider.OpenAI,
        id: String = "test-model",
        capabilities: List<LLMCapability> = emptyList(),
        contextLength: Long = 4096,
        maxOutputTokens: Long? = null,
    ) = LLModel(
        provider = provider,
        id = id,
        capabilities = capabilities,
        contextLength = contextLength,
        maxOutputTokens = maxOutputTokens,
    )
}
