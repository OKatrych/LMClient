package dev.olek.lmclient.presentation.components.main

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.presentation.components.main.ChatHistoryComponent.State
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChatHistoryComponent {
    val state: StateFlow<State>

    fun onSearchQueryChange(query: String)

    fun navigateToChatRoom(chatRoom: ChatRoom)

    fun navigateToSettings()

    data class State(
        val chatRooms: List<ChatRoom> = emptyList(),
        val searchQuery: String = "",
    )
}

class ChatHistoryComponentImpl(
    context: ComponentContext,
    private val onChatRoomClicked: () -> Unit,
    private val onSettingsClicked: () -> Unit,
) : ChatHistoryComponent, KoinComponent, ComponentContext by context {

    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)
    private val logger = Logger.withTag("ChatHistoryComponent")
    private val chatRoomRepository: ChatRoomRepository by inject()

    private val searchQuery = MutableStateFlow("")

    override val state: StateFlow<State> = combine(
        chatRoomRepository.observeChatRooms(),
        searchQuery,
    ) { chatRooms, searchQuery ->
        State(
            chatRooms = chatRooms.filterBySearchQuery(searchQuery),
            searchQuery = searchQuery,
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = State(),
    )

    override fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    override fun navigateToChatRoom(chatRoom: ChatRoom) {
        coroutineScope.launch {
            logger.d { "onChatRoomSelected: ${chatRoom.id}" }
            chatRoomRepository.setActiveChatRoom(chatRoom.id)
            onChatRoomClicked()
        }
    }

    override fun navigateToSettings() {
        logger.d { "navigateToSettings" }
        onSettingsClicked()
    }

    private fun List<ChatRoom>.filterBySearchQuery(searchQuery: String) = filter { chatRoom ->
        if (searchQuery.isEmpty()) return@filter true
        chatRoom.name.contains(other = searchQuery, ignoreCase = true)
    }
}

data class ChatHistoryComponentPreview(
    private val customState: State,
) : ChatHistoryComponent {
    override val state: StateFlow<State> = MutableStateFlow(customState)

    override fun onSearchQueryChange(query: String) = Unit

    override fun navigateToChatRoom(chatRoom: ChatRoom) = Unit

    override fun navigateToSettings() = Unit
}
