package dev.olek.lmclient.data.repositories.fakes

import dev.olek.lmclient.data.local.ModelProviderStoreContract
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.models.ModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeModelProviderStore : ModelProviderStoreContract {

    private val providers = MutableStateFlow<Map<String, ModelProvider>>(emptyMap())
    private val models = mutableMapOf<String, List<Model>>()

    fun setProvider(provider: ModelProvider) {
        providers.value += (provider.id to provider)
    }

    fun setModels(providerId: String, modelList: List<Model>) {
        models[providerId] = modelList
    }

    override fun observeModelProvider(id: String): Flow<ModelProvider> =
        MutableStateFlow(providers.value[id] ?: error("Provider not found: $id"))

    override suspend fun getModels(providerId: String): List<Model> =
        models[providerId] ?: emptyList()
}
