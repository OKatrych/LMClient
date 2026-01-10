package dev.olek.lmclient.data.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.olek.lmclient.shared.data.Database
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.models.ModelProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class ModelProviderStore(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun observeProviders(): Flow<List<ModelProvider>> = database.modelProvidersQueries
        .selectAllModelProviders(mapper = ::providerMapper)
        .asFlow()
        .mapToList(dispatcher)
        .distinctUntilChanged()
        .flowOn(dispatcher)

    fun observeModelProvider(id: String): Flow<ModelProvider> = database.modelProvidersQueries
        .selectModelProvider(id = id, mapper = ::providerMapper)
        .asFlow()
        .mapToOne(dispatcher)
        .distinctUntilChanged()
        .flowOn(dispatcher)

    suspend fun setActiveModelProvider(id: String) = withContext(dispatcher) {
        database.modelProvidersQueries
            .setActiveModelProvider(id = id, provider_id = id)
            .awaitAsOneOrNull()
    }

    suspend fun setProviderConfig(config: ModelProvider.ModelProviderConfig) {
        withContext(dispatcher) {
            when (config) {
                is ModelProvider.ModelProviderConfig.StandardConfig -> {
                    database.modelProvidersQueries.updateProviderConfig(
                        provider_id = config.providerId,
                        api_key = config.apiKey,
                        api_url = config.apiUrl,
                    )
                }
                is ModelProvider.ModelProviderConfig.LocalConfig -> {
                    database.modelProvidersQueries.updateProviderConfig(
                        provider_id = config.providerId,
                        api_url = config.apiUrl,
                        api_key = null, // No API key for local config
                    )
                }
            }
        }
    }

    fun observeActiveModel(providerId: String): Flow<Model?> = database.modelsQueries
        .getActiveModel(providerId, mapper = ::modelMapper)
        .asFlow()
        .mapToOneOrNull(dispatcher)
        .distinctUntilChanged()
        .flowOn(dispatcher)

    suspend fun getModels(providerId: String): List<Model> = withContext(dispatcher) {
        database.modelsQueries
            .getModels(provider_id = providerId, mapper = ::modelMapper)
            .awaitAsList()
    }

    suspend fun setActiveModel(providerId: String, modelId: String) = withContext(dispatcher) {
        database.modelsQueries
            .setActiveModel(provider_id = providerId, id = modelId)
            .awaitAsOneOrNull()
    }

    suspend fun replaceModels(providerId: String, models: List<Model>) = withContext(dispatcher) {
        database.transaction {
            // Insert new models
            models.forEach { model ->
                database.modelsQueries.upsertModel(
                    id = model.id,
                    provider_id = providerId,
                    name = model.name,
                    capabilities = Json.encodeToString(model.capabilities),
                    context_length = model.contextLength,
                    max_output_tokens = model.maxOutputTokens,
                )
            }
        }
    }

    suspend fun getLastModelUpdate(providerId: String): Long? = withContext(dispatcher) {
        database.modelsQueries
            .getLastModelUpdate(provider_id = providerId)
            .awaitAsOneOrNull()
            ?.MAX
    }

    @Suppress("UnusedParameter")
    private fun providerMapper(
        id: String,
        name: String,
        isActive: Long,
        configType: String,
        apiKey: String?,
        apiUrl: String?,
    ): ModelProvider {
        val config = when (configType) {
            "standard" -> {
                ModelProvider.ModelProviderConfig.StandardConfig(
                    providerId = id,
                    apiKey = apiKey,
                    apiUrl = apiUrl ?: error("API URL is required for standard config"),
                )
            }
            "local" -> {
                ModelProvider.ModelProviderConfig.LocalConfig(
                    providerId = id,
                    apiUrl = apiUrl ?: error("API URL is required for local config"),
                )
            }

            else -> error("Unsupported config type")
        }

        return ModelProvider(
            id = id,
            name = name,
            config = config,
            isActive = isActive == 1L,
        )
    }

    @Suppress("UnusedParameter")
    private fun modelMapper(
        id: String,
        providerId: String,
        name: String,
        capabilities: String?,
        contextLength: Long?,
        maxOutputTokens: Long?,
        createdAt: String,
        updatedAt: String,
        isActive: Long,
    ): Model = Model(
        id = id,
        providerId = providerId,
        name = name,
        capabilities = capabilities?.let { Json.decodeFromString<List<Model.Capability>>(it) }
            ?: emptyList(),
        contextLength = contextLength ?: 0,
        maxOutputTokens = maxOutputTokens ?: 0,
        isActive = isActive == 1L,
    )
}
