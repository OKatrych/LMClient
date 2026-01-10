@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.presentation.components.main

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.Message.MessageContent
import dev.olek.lmclient.data.repositories.ChatMessagesRepository
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface QueryInputComponent {
    val state: StateFlow<State>

    fun onQueryChange(query: String)

    fun onQuerySubmit()

    fun onQueryCancel()

    /**
     * @param query the query text.
     * @param attachments the list of attachments.
     * @param isLoading whether the system message is being generated.
     */
    data class State(
        val query: String = "",
        val attachments: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val isEnabled: Boolean = false,
    )
}

internal class QueryInputComponentImpl(context: ComponentContext) :
    QueryInputComponent,
    KoinComponent,
    ComponentContext by context {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    private val logger = Logger.withTag("QueryInputComponent")
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val chatMessagesRepository: ChatMessagesRepository by inject()
    private val modelProviderRepository: ModelProviderRepository by inject()

    private val queryState: MutableStateFlow<String> = MutableStateFlow("")

    private val chatRoomFlow: StateFlow<ChatRoom?> = chatRoomRepository
        .observeActiveChatRoom()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val messageGenerationFlow = chatRoomFlow.flatMapLatest { chatRoom ->
        if (chatRoom != null) {
            chatMessagesRepository.observeMessageGenerationStatus(chatRoom.id)
        } else {
            flowOf(false)
        }
    }

    private val modelProviderFlow = modelProviderRepository
        .observeActiveProvider()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val activeModelFlow = modelProviderRepository
        .observeActiveModel()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    override val state: StateFlow<QueryInputComponent.State> = combine(
        queryState,
        modelProviderFlow,
        activeModelFlow,
        messageGenerationFlow,
        chatRoomFlow,
    ) { query, activeProvider, activeModel, isMessageGenerating, activeChatRoom ->
        val hasSameProvider = activeChatRoom == null ||
            activeProvider?.id == activeChatRoom.modelProviderId
        QueryInputComponent.State(
            query = query,
            isLoading = isMessageGenerating,
            isEnabled = activeProvider != null && activeModel != null && hasSameProvider,
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = QueryInputComponent.State(),
    )

    override fun onQueryChange(query: String) {
        queryState.update { query }
    }

    override fun onQuerySubmit() {
        coroutineScope.launch {
            logger.d { "onQuerySubmit" }
            val userMessage = Message.UserMessage(
                content = MessageContent.Text(state.value.query),
                attachments = emptyList(),
            )
            val chatRoom = getOrCreateChatRoom(userMessage)

            chatMessagesRepository.generateMessage(
                prompt = userMessage,
                chatRoom = chatRoom,
            )
            queryState.update { "" }
        }
    }

    override fun onQueryCancel() {
        logger.d { "onQueryCancel" }
        chatMessagesRepository.cancelMessageGeneration(
            chatRoomId = chatRoomFlow.value?.id ?: error("Chat room should not be null"),
        )
    }

    private suspend fun getOrCreateChatRoom(userMessage: Message.UserMessage): ChatRoom = chatRoomFlow.value ?: run {
        val name = when (userMessage.content) {
            is MessageContent.Audio -> "Audio conversation"
            is MessageContent.Text -> userMessage.content.text.take(15)
        }
        val providerId = modelProviderFlow.value?.id
            ?: error("Model provider should not be null")
        val modelId = activeModelFlow.value?.id
            ?: error("Model should not be null")
        chatRoomRepository
            .createChatRoom(
                modelProviderId = providerId,
                name = name,
                modelId = modelId,
            ).also {
                chatRoomRepository.setActiveChatRoom(it.id)
            }
    }
}

object QueryInputComponentPreview : QueryInputComponent {
    override val state: StateFlow<QueryInputComponent.State> =
        MutableStateFlow(QueryInputComponent.State())

    override fun onQueryChange(query: String) = Unit

    override fun onQuerySubmit() = Unit

    override fun onQueryCancel() = Unit
}
