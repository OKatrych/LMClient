package dev.olek.lmclient.data.remote

import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterClientSettings
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import dev.olek.lmclient.app.KoinApp
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.models.ModelProvider.ModelProviderConfig
import dev.olek.lmclient.data.remote.mappers.KoogMessageMapper
import dev.olek.lmclient.data.remote.mappers.KoogStreamFrameMapper
import dev.olek.lmclient.data.remote.mappers.toKoogProvider
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.messages.anthropic.CustomKoogAnthropicClient
import dev.olek.lmclient.data.remote.models.ModelsApi
import dev.olek.lmclient.data.remote.models.anthropic.ClaudeModelsApi
import dev.olek.lmclient.data.remote.models.koog.KoogModelsApi
import dev.olek.lmclient.data.remote.models.nexosai.NexosAiModelsApi
import dev.olek.lmclient.data.remote.models.openai.OpenAiModelsApi
import dev.olek.lmclient.data.remote.utils.claudeHttpClient
import dev.olek.lmclient.data.remote.utils.nexosAiHttpClient
import dev.olek.lmclient.data.util.LMClientModelProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

/**
 * Provider for creating and caching [LMClientApi] instances based on different [ModelProvider] configurations.
 *
 * This class serves as a factory for creating API instances that support various AI model providers including
 * OpenAI, Anthropic, Google, OpenRouter, and Ollama. It implements caching to avoid unnecessary recreation
 * of API instances when the same model provider is requested multiple times.
 *
 * @see ModelProvider
 * @see LMClientApi
 */
internal interface LMClientApiProvider {
    /**
     * Retrieves a cached [ModelsApi] instance for the specified [ModelProvider].
     *
     * @param modelProvider The model provider configuration
     * @return A [ModelsApi] instance for managing available models
     */
    suspend fun getModelsApi(modelProvider: ModelProvider): ModelsApi

    /**
     * Retrieves a cached [PromptApi] instance for the specified [ModelProvider].
     *
     * @param modelProvider The model provider configuration
     * @return A [PromptApi] instance for sending prompts and receiving responses
     */
    suspend fun getPromptApi(modelProvider: ModelProvider): PromptApi

    /**
     * Creates a new, uncached [LMClientApi] instance for testing configuration validity.
     *
     * This method bypasses the caching mechanism and always creates a fresh API instance,
     * making it ideal for testing whether a model provider configuration is working
     * without affecting the cached instance.
     *
     * @param modelProvider The model provider configuration to test
     * @return A new [LMClientApi] instance
     */
    fun createTestApi(modelProvider: ModelProvider): LMClientApi
}

@Single(binds = [LMClientApiProvider::class])
internal class LMClientApiProviderImpl(
    private val messageMapper: KoogMessageMapper,
    private val streamFrameMapper: KoogStreamFrameMapper,
    @Property(KoinApp.PROPERTY_IS_DEBUG) private val isDebug: Boolean,
) : LMClientApiProvider {
    private val mutex = Mutex()
    private var mutableApi: LMClientApi? = null
    private var mutableModelProvider: ModelProvider? = null

    override suspend fun getModelsApi(modelProvider: ModelProvider): ModelsApi {
        return getCachedApi(modelProvider)
    }

    override suspend fun getPromptApi(modelProvider: ModelProvider): PromptApi {
        return getCachedApi(modelProvider)
    }

    override fun createTestApi(modelProvider: ModelProvider): LMClientApi {
        return createApi(modelProvider)
    }

    private suspend fun getCachedApi(
        modelProvider: ModelProvider,
    ): LMClientApi = mutex.withLock {
        return@withLock if (this.mutableModelProvider == modelProvider) {
            return mutableApi ?: error("LMClientApi not initialized")
        } else {
            createApi(modelProvider).also {
                mutableModelProvider = modelProvider
                mutableApi = it
            }
        }
    }

    private fun createApi(modelProvider: ModelProvider): LMClientApi = KoogApi(
        promptExecutor = createPromptExecutor(modelProvider),
        modelsApi = createModelsApi(modelProvider),
        messageMapper = messageMapper,
        streamFrameMapper = streamFrameMapper,
    )

    private fun createModelsApi(modelProvider: ModelProvider): ModelsApi {
        val config = modelProvider.config as ModelProviderConfig.StandardConfig
        requireNotNull(config.apiKey)

        return when (LMClientModelProvider.fromId(modelProvider.id)) {
            LMClientModelProvider.Claude -> {
                ClaudeModelsApi(
                    httpClient = claudeHttpClient(
                        apiKey = config.apiKey,
                        baseUrl = config.apiUrl,
                        isDebug = isDebug,
                    ),
                )
            }

            LMClientModelProvider.OpenAI -> {
                OpenAiModelsApi(
                    openAI = OpenAI(
                        token = config.apiKey,
                        host = OpenAIHost(baseUrl = config.apiUrl),
                        logging = LoggingConfig(
                            logLevel = LogLevel.All,
                        ),
                        timeout = Timeout(
                            request = 10.seconds,
                            connect = 10.seconds,
                            socket = 30.seconds,
                        ),
                        retry = RetryStrategy(maxRetries = 0),
                    ),
                )
            }

            LMClientModelProvider.NexosAI -> NexosAiModelsApi(
                httpClient = nexosAiHttpClient(
                    apiKey = config.apiKey,
                    baseUrl = config.apiUrl,
                    isDebug = isDebug,
                ),
            )

            LMClientModelProvider.DeepSeek,
            LMClientModelProvider.Google,
            LMClientModelProvider.Ollama,
            LMClientModelProvider.OpenRouter,
                -> KoogModelsApi(
                provider = LMClientModelProvider
                    .fromId(
                        modelProvider.id,
                    ).toKoogProvider(),
            )
        }
    }

    @Suppress("LongMethod")
    private fun createPromptExecutor(modelProvider: ModelProvider): PromptExecutor =
        when (LMClientModelProvider.fromId(modelProvider.id)) {
            LMClientModelProvider.Claude -> {
                val config = modelProvider.config as ModelProviderConfig.StandardConfig
                requireNotNull(config.apiKey)
                SingleLLMPromptExecutor(
                    CustomKoogAnthropicClient(
                        apiKey = config.apiKey,
                        settings = AnthropicClientSettings(baseUrl = config.apiUrl),
                    ),
                )
            }

            LMClientModelProvider.OpenAI -> {
                val config = modelProvider.config as ModelProviderConfig.StandardConfig
                requireNotNull(config.apiKey)
                SingleLLMPromptExecutor(
                    OpenAILLMClient(
                        apiKey = config.apiKey,
                        settings = OpenAIClientSettings(
                            baseUrl = config.apiUrl,
                            chatCompletionsPath = "chat/completions",
                            responsesAPIPath = "responses",
                            embeddingsPath = "embeddings",
                            moderationsPath = "moderations",
                        ),
                    ),
                )
            }

            LMClientModelProvider.OpenRouter -> {
                val config = modelProvider.config as ModelProviderConfig.StandardConfig
                requireNotNull(config.apiKey)
                SingleLLMPromptExecutor(
                    OpenRouterLLMClient(
                        apiKey = config.apiKey,
                        settings = OpenRouterClientSettings(baseUrl = config.apiUrl),
                    ),
                )
            }

            LMClientModelProvider.Google -> {
                val config = modelProvider.config as ModelProviderConfig.StandardConfig
                requireNotNull(config.apiKey)
                SingleLLMPromptExecutor(
                    GoogleLLMClient(
                        apiKey = config.apiKey,
                        settings = GoogleClientSettings(baseUrl = config.apiUrl),
                    ),
                )
            }

            LMClientModelProvider.Ollama -> {
                val config = modelProvider.config as ModelProviderConfig.LocalConfig
                SingleLLMPromptExecutor(OllamaClient(baseUrl = config.apiUrl))
            }

            LMClientModelProvider.NexosAI -> {
                val config = modelProvider.config as ModelProviderConfig.StandardConfig
                requireNotNull(config.apiKey)
                SingleLLMPromptExecutor(
                    OpenAILLMClient(
                        apiKey = config.apiKey,
                        settings = OpenAIClientSettings(
                            baseUrl = config.apiUrl,
                            chatCompletionsPath = "chat/completions",
                            responsesAPIPath = "responses",
                            embeddingsPath = "embeddings",
                            moderationsPath = "moderations",
                        ),
                    ),
                )
            }

            LMClientModelProvider.DeepSeek -> TODO()
        }
}
