package dev.olek.lmclient.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.components.main.ChatScreenComponent.ChatScreenState
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponent
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponentImpl
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponentPreview
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChatScreenComponent {
    val chatTopBarComponent: ChatTopBarComponent
    val chatItemsListComponent: ChatItemsListComponent
    val queryInputComponent: QueryInputComponent

    val state: StateFlow<ChatScreenState>

    fun navigateToProviderList()

    data class ChatScreenState(val isProviderConfigured: Boolean = true)
}

internal class ChatScreenComponentImpl(
    context: ComponentContext,
    onDrawerClick: () -> Unit,
    private val onConfigureProviderClick: () -> Unit,
) : ChatScreenComponent,
    KoinComponent,
    ComponentContext by context {

    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    private val modelProviderRepository: ModelProviderRepository by inject()

    override val state: StateFlow<ChatScreenState> = modelProviderRepository.observeActiveProvider()
        .map { ChatScreenState(isProviderConfigured = it != null) }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
            initialValue = ChatScreenState(),
        )

    override fun navigateToProviderList() {
        onConfigureProviderClick()
    }

    override val chatTopBarComponent = ChatTopBarComponentImpl(
        context = childContext("ChatTopBarComponent"),
        onChatHistoryClicked = onDrawerClick,
    )
    override val chatItemsListComponent = ChatItemsListComponentImpl(
        context = childContext("MessagesListComponent"),
    )
    override val queryInputComponent = QueryInputComponentImpl(
        context = childContext("QueryInputComponent"),
    )
}

data class ChatScreenComponentPreview(
    override val chatTopBarComponent: ChatTopBarComponent = ChatTopBarComponentPreview(),
    override val chatItemsListComponent: ChatItemsListComponent = ChatItemsListComponentPreview(),
    override val queryInputComponent: QueryInputComponent = QueryInputComponentPreview,
    override val state: StateFlow<ChatScreenState> = MutableStateFlow(ChatScreenState())
) : ChatScreenComponent {
    override fun navigateToProviderList() = Unit
}
