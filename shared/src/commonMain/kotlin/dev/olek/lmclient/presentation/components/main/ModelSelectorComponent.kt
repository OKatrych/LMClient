@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.presentation.components.main

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ModelSelectorComponent {
    val state: StateFlow<State>
    val onDismissRequest: () -> Unit

    fun onModelSelected(model: Model)

    data class State(
        val isLoading: Boolean = true,
        val selectedModel: Model? = null,
        val models: List<Model> = emptyList(),
        val error: LMClientError? = null,
    )
}

class ModelSelectorComponentImpl(
    context: ComponentContext,
    override val onDismissRequest: () -> Unit
) :
    ModelSelectorComponent,
    KoinComponent,
    ComponentContext by context {
    private val modelProviderRepository: ModelProviderRepository by inject()
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val logger = Logger.withTag("ModelSelectorComponent")

    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    override val state: StateFlow<ModelSelectorComponent.State> =
        combine(
            modelProviderRepository.observeActiveProvider(),
            modelProviderRepository.observeActiveModel(),
        ) { provider, activeModel ->
            if (provider != null) {
                modelProviderRepository.getModels().fold(
                    ifLeft = { error ->
                        ModelSelectorComponent.State(
                            isLoading = false,
                            selectedModel = null,
                            models = emptyList(),
                            error = error,
                        )
                    },
                    ifRight = { models ->
                        ModelSelectorComponent.State(
                            isLoading = false,
                            selectedModel = activeModel?.let {
                                models.find { activeModel.id == it.id }
                            },
                            models = models,
                        )
                    },
                )
            } else {
                logger.e { "Provider is null, this should not happen" }
                Sentry.captureException(Exception("Provider is null, this should not happen"))
                ModelSelectorComponent.State()
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
            initialValue = ModelSelectorComponent.State(),
        )

    override fun onModelSelected(model: Model) {
        coroutineScope.launch {
            runCatching {
                val chatRoom = chatRoomRepository.observeActiveChatRoom().first()
                if (chatRoom != null) {
                    chatRoomRepository.updateChatRoom(chatRoom.copy(modelId = model.id))
                }
                modelProviderRepository.setActiveModel(model)
                onDismissRequest()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                logger.e(error) { "Failed to update model: $model" }
            }
        }
    }
}

data class ModelSelectorComponentPreview(
    override val state: StateFlow<ModelSelectorComponent.State> = MutableStateFlow(
        value = ModelSelectorComponent.State(
            selectedModel = Model(
                id = "1",
                providerId = "google",
                name = "Gemini 2.5 Pro 03-25",
                capabilities = emptyList(),
                contextLength = 0,
            ),
            models = listOf(
                Model(
                    id = "1",
                    providerId = "google",
                    name = "Gemini 2.5 Pro 03-25",
                    capabilities = emptyList(),
                    contextLength = 0,
                ),
                Model(
                    id = "2",
                    providerId = "google",
                    name = "Gemini 2.0 Flash",
                    capabilities = emptyList(),
                    contextLength = 0,
                ),
                Model(
                    id = "3",
                    providerId = "google",
                    name = "Gemini 2.5 Flash 04-17",
                    capabilities = emptyList(),
                    contextLength = 0,
                ),
            ),
            isLoading = false,
        ),
    )
) : ModelSelectorComponent {
    override val onDismissRequest: () -> Unit = {}
    override fun onModelSelected(model: Model) = Unit
}
