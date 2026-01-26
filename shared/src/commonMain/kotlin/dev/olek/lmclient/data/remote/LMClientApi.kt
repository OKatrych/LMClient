package dev.olek.lmclient.data.remote

import ai.koog.prompt.executor.model.PromptExecutor
import dev.olek.lmclient.data.remote.mappers.KoogMessageMapper
import dev.olek.lmclient.data.remote.mappers.KoogStreamFrameMapper
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.messages.koog.KoogPromptApi
import dev.olek.lmclient.data.remote.models.ModelsApi

interface LMClientApi : PromptApi, ModelsApi

internal class KoogApi(
    promptExecutor: PromptExecutor,
    modelsApi: ModelsApi,
    messageMapper: KoogMessageMapper,
    streamFrameMapper: KoogStreamFrameMapper,
) : LMClientApi,
    PromptApi by KoogPromptApi(promptExecutor, messageMapper, streamFrameMapper),
    ModelsApi by modelsApi
