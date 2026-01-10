@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.data.repositories

import arrow.core.Either
import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.database.ModelProviderStore
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.remote.LMClientApiProvider
import dev.olek.lmclient.data.util.LMClientModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

private val ModelRefreshInterval = 1.days

interface ModelProviderRepository {
    fun observeProviders(): Flow<List<ModelProvider>>

    fun observeProvider(providerId: String): Flow<ModelProvider>

    suspend fun setActiveProvider(providerId: String)

    fun observeActiveModel(): Flow<Model?>

    suspend fun setActiveModel(model: Model)

    suspend fun getModels(useCache: Boolean = true): Either<LMClientError, List<Model>>

    suspend fun setProviderConfig(config: ModelProvider.ModelProviderConfig): Either<LMClientError, Unit>
}

fun ModelProviderRepository.observeActiveProvider() = observeProviders()
    .map { it.find(ModelProvider::isActive) }
    .distinctUntilChanged()

@Single(binds = [ModelProviderRepository::class])
internal class ModelProviderRepositoryImpl(
    private val modelProviderStore: ModelProviderStore,
    private val apiProvider: LMClientApiProvider,
) : ModelProviderRepository,
    KoinComponent {
    private val logger = Logger.withTag("ModelProviderRepositoryImpl")

    override fun observeProviders(): Flow<List<ModelProvider>> =
        modelProviderStore.observeProviders()
            .map { providers ->
                // FIXME add support for all providers
                providers.filter { provider ->
                    provider.id in listOf(
                        LMClientModelProvider.Google.id,
                        LMClientModelProvider.Claude.id,
                        LMClientModelProvider.OpenAI.id,
                        LMClientModelProvider.NexosAI.id,
                    )
                }
            }

    override fun observeProvider(providerId: String): Flow<ModelProvider> =
        modelProviderStore.observeModelProvider(providerId)

    override suspend fun setActiveProvider(providerId: String) {
        modelProviderStore.setActiveModelProvider(providerId)
    }

    override fun observeActiveModel(): Flow<Model?> =
        observeActiveProvider().flatMapLatest { provider ->
            if (provider != null) {
                modelProviderStore.observeActiveModel(provider.id)
            } else {
                flowOf(null)
            }
        }

    override suspend fun setActiveModel(model: Model) {
        modelProviderStore.setActiveModel(providerId = model.providerId, modelId = model.id)
    }

    override suspend fun getModels(useCache: Boolean): Either<LMClientError, List<Model>> {
        val provider = observeActiveProvider().firstOrNull() ?: error("No active provider")
        if (!useCache) {
            return apiProvider.getModelsApi(provider).models().onRight { models ->
                modelProviderStore.replaceModels(provider.id, models)
            }
        }

        val cachedModels = modelProviderStore.getModels(providerId = provider.id)
        val lastFetchTime = modelProviderStore.getLastModelUpdate(providerId = provider.id)

        if (cachedModels.isEmpty() || isCacheExpired(lastFetchTime, ModelRefreshInterval)) {
            return apiProvider.getModelsApi(provider).models().onRight { models ->
                modelProviderStore.replaceModels(provider.id, models)
            }
        }

        return Either.Right(cachedModels)
    }

    override suspend fun setProviderConfig(config: ModelProvider.ModelProviderConfig): Either<LMClientError, Unit> {
        logger.d { "setProviderConfig: $config" }
        val modelProvider = observeProvider(config.providerId).first()
        val updatedProvider = modelProvider.copy(config = config)

        // Check if model API will fetch models to verify the new configuration
        return apiProvider
            .createTestApi(updatedProvider)
            .models()
            .onRight { models ->
                logger.d { "Loaded models for provider ${config.providerId}: $models" }
                modelProviderStore.replaceModels(config.providerId, models)
                // Models were loaded without an error, save the new config
                logger.d { "Setting provider config: $config" }
                modelProviderStore.setProviderConfig(config)
            }.onLeft {
                logger.e { "Error while saving new provider config: $it" }
            }.map {}
    }

    private fun isCacheExpired(lastFetchTime: Long?, refreshInterval: Duration): Boolean {
        if (lastFetchTime == null) return true
        val now = Clock.System.now().toEpochMilliseconds()
        return now - lastFetchTime > refreshInterval.inWholeMilliseconds
    }
}
