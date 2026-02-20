package dev.olek.lmclient.data.repositories.fakes

import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.remote.LMClientApi
import dev.olek.lmclient.data.remote.LMClientApiProvider
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.models.ModelsApi

internal class FakeLMClientApiProvider(
    private val promptApi: FakePromptApi = FakePromptApi(),
) : LMClientApiProvider {

    override suspend fun getModelsApi(modelProvider: ModelProvider): ModelsApi {
        error("Not needed in ChatMessagesRepository tests")
    }

    override suspend fun getPromptApi(modelProvider: ModelProvider): PromptApi = promptApi

    override fun createTestApi(modelProvider: ModelProvider): LMClientApi {
        error("Not needed in ChatMessagesRepository tests")
    }
}
