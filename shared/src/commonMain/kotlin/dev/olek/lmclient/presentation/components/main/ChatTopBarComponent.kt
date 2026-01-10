@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.presentation.components.main

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.components.main.ChatTopBarComponent.ChatTopBarState
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChatTopBarComponent {
    /**
     * Model selector is not always visible, to not keep [ModelSelectorComponent] in memory and
     * waste resources, use [ChildSlot] to manage its lifecycle.
     */
    val modelSelectorComponent: Value<ChildSlot<Unit, ModelSelectorComponent>>

    val state: StateFlow<ChatTopBarState>

    fun navigateToChatHistory()

    fun navigateToNewChat()

    fun onModelSelectorClick()

    data class ChatTopBarState(val isVisible: Boolean = false, val selectedModel: String? = null)
}

internal class ChatTopBarComponentImpl(
    context: ComponentContext,
    private val onChatHistoryClicked: () -> Unit,
) : ChatTopBarComponent,
    KoinComponent,
    ComponentContext by context {
    private val modelSelectorNavigation = SlotNavigation<Unit>()
    override val modelSelectorComponent: Value<ChildSlot<Unit, ModelSelectorComponent>> = childSlot(
        source = modelSelectorNavigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { _, context ->
            ModelSelectorComponentImpl(
                context = context,
                onDismissRequest = modelSelectorNavigation::dismiss,
            )
        },
    )

    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val modelProviderRepository: ModelProviderRepository by inject()

    override val state: StateFlow<ChatTopBarState> = combine(
        modelProviderRepository.observeActiveProvider(),
        modelProviderRepository.observeActiveModel(),
    ) { provider, model ->
        if (provider != null) {
            ChatTopBarState(
                isVisible = true,
                selectedModel = model?.name,
            )
        } else {
            ChatTopBarState(isVisible = false)
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = ChatTopBarState(),
    )

    override fun onModelSelectorClick() {
        modelSelectorNavigation.activate(Unit)
    }

    override fun navigateToChatHistory() {
        onChatHistoryClicked()
    }

    override fun navigateToNewChat() {
        coroutineScope.launch {
            chatRoomRepository.setActiveChatRoom(null)
        }
    }
}

data class ChatTopBarComponentPreview(private val customState: ChatTopBarState = ChatTopBarState()) :
    ChatTopBarComponent {
    override val state: StateFlow<ChatTopBarState> = MutableStateFlow(customState)
    override val modelSelectorComponent: Value<ChildSlot<Unit, ModelSelectorComponent>> =
        MutableValue(
            ChildSlot(
                child = Child.Created(
                    configuration = Unit,
                    instance = ModelSelectorComponentPreview(),
                ),
            ),
        )

    override fun navigateToChatHistory() = Unit

    override fun navigateToNewChat() = Unit

    override fun onModelSelectorClick() = Unit
}
