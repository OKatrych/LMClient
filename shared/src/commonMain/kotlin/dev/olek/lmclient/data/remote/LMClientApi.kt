package dev.olek.lmclient.data.remote

import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.remote.messages.koog.KoogPromptApi
import dev.olek.lmclient.data.remote.models.ModelsApi

interface LMClientApi : PromptApi, ModelsApi

internal class KoogApi(
    promptApi: KoogPromptApi,
    modelsApi: ModelsApi,
) : LMClientApi,
    PromptApi by promptApi,
    ModelsApi by modelsApi
