package dev.olek.lmclient.presentation.components.settings

import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.ModelProvider.ModelProviderConfig
import dev.olek.lmclient.data.repositories.ChatMessagesRepository
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent.Event
import dev.olek.lmclient.presentation.components.settings.ModelProviderConfigurationComponent.State
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ModelProviderConfigurationComponent {
    val state: StateFlow<State>
    val event: Flow<Event>

    fun onConfigChange(config: ModelProviderConfig)

    fun saveAndActivate()

    fun navigateBack()

    data class State(
        val providerName: String = "",
        val config: ModelProviderConfig? = null,
        val isProviderActive: Boolean = false,
        val isConfigModified: Boolean = false,
        val isConfigSaveInProgress: Boolean = false,
    )

    sealed interface Event {
        data object ConfigSaveSuccess : Event

        data class ConfigSaveError(val error: LMClientError) : Event
    }
}

internal class ModelProviderConfigurationComponentImpl(
    context: ComponentContext,
    providerId: String,
    private val onBackPressed: () -> Unit,
) : ModelProviderConfigurationComponent,
    KoinComponent,
    ComponentContext by context {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val modelProviderRepository: ModelProviderRepository by inject()
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val messagesRepository: ChatMessagesRepository by inject()

    private val modifiedConfigState = MutableStateFlow(
        value = stateKeeper.consume(
            key = "modified_config",
            strategy = ModelProviderConfig.serializer(),
        ),
    )
    private val savingState = MutableStateFlow(false)

    init {
        stateKeeper.register(
            "modified_config",
            ModelProviderConfig.serializer(),
        ) { modifiedConfigState.value }
    }

    override val state: StateFlow<State> = combine(
        modelProviderRepository.observeProvider(providerId),
        modifiedConfigState,
        savingState,
    ) { provider, modifiedConfig, isConfigSaveInProgress ->
        if (modifiedConfig == null) {
            modifiedConfigState.emit(provider.config)
        }
        State(
            providerName = provider.name,
            config = modifiedConfig ?: provider.config,
            isProviderActive = provider.isActive,
            isConfigModified = modifiedConfig != null && modifiedConfig != provider.config,
            isConfigSaveInProgress = isConfigSaveInProgress,
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = State(),
    )

    private val mutableEvent: Channel<Event> = Channel(Channel.BUFFERED)
    override val event: Flow<Event> = mutableEvent.receiveAsFlow()

    override fun onConfigChange(config: ModelProviderConfig) {
        coroutineScope.launch {
            modifiedConfigState.update { config }
        }
    }

    override fun saveAndActivate() {
        coroutineScope.launch {
            savingState.update { true }
            val config = state.value.config ?: error("Config cannot be null")

            modelProviderRepository
                .setProviderConfig(config)
                .onLeft {
                    mutableEvent.send(Event.ConfigSaveError(it))
                }.onRight {
                    modelProviderRepository.setActiveProvider(config.providerId)
                    chatRoomRepository.setActiveChatRoom(null)
                    messagesRepository.cancelMessageGeneration()

                    mutableEvent.send(Event.ConfigSaveSuccess)
                }
            savingState.update { false }
        }
    }

    override fun navigateBack() {
        onBackPressed()
    }
}

class ModelProviderConfigurationComponentPreview(customState: State) : ModelProviderConfigurationComponent {
    override val state: StateFlow<State> = MutableStateFlow(customState)
    override val event: Flow<Event> = emptyFlow()

    override fun onConfigChange(config: ModelProviderConfig) = Unit

    override fun saveAndActivate() = Unit

    override fun navigateBack() = Unit
}
