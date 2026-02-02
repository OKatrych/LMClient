@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.presentation.components.main.chatitems

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.repositories.ChatMessagesRepository
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponent.Event
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChatItemsListComponent {
    val state: StateFlow<ChatItemsListState>
    val event: Flow<Event>

    fun retry(messageId: String)

    fun copy(chatItem: ChatItem.AssistantItem)

    sealed interface Event {
        data class CopyText(val text: String) : Event
        data class Error(val error: LMClientError) : Event
    }
}

internal class ChatItemsListComponentImpl(context: ComponentContext) :
    ChatItemsListComponent,
    KoinComponent,
    ComponentContext by context {
    private val logger = Logger.withTag("MessagesListComponentImpl")
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    private val messagesRepository: ChatMessagesRepository by inject()
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val modelProviderRepository: ModelProviderRepository by inject()

    override val state: StateFlow<ChatItemsListState> =
        combine(
            chatRoomRepository.observeActiveChatRoom(),
            modelProviderRepository.observeActiveProvider(),
        ) { chatRoom, provider ->
            chatRoom to provider
        }.flatMapLatest { (chatRoom, provider) ->
            if (chatRoom != null) {
                combine(
                    messagesRepository.observeMessages(chatRoom.id),
                    messagesRepository.observeMessageGenerationStatus(chatRoom.id),
                ) { messages, isGeneratingResponse ->
                    ChatItemsListState(
                        chatItems = messages.map { it.toChatItem() }.reversed(),
                        chatRoom = chatRoom,
                        isGeneratingResponse = isGeneratingResponse,
                        isProviderConfigured = provider != null,
                    )
                }
            } else {
                flowOf(
                    ChatItemsListState(
                        isProviderConfigured = provider != null,
                    )
                )
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
            initialValue = ChatItemsListState(),
        )

    private val mutableEvent: Channel<Event> = Channel(Channel.BUFFERED)
    override val event: Flow<Event> = mutableEvent.receiveAsFlow()

    init {
        chatRoomRepository
            .observeActiveChatRoom()
            .flatMapLatest { chatRoom ->
                if (chatRoom != null) {
                    messagesRepository.observeErrorEvents(chatRoom.id)
                } else {
                    emptyFlow()
                }
            }.onEach { error ->
                logger.e { "Error event received: $error" }
                mutableEvent.send(Event.Error(error))
            }.launchIn(coroutineScope)
    }

    override fun retry(messageId: String) {
        logger.d { "Re-generating message: $messageId" }
        val chatRoom = state.value.chatRoom ?: error("Chat room is null")
        messagesRepository.regenerateMessage(messageId, chatRoom)
    }

    override fun copy(chatItem: ChatItem.AssistantItem) {
        val text = when (val content = chatItem.content) {
            is ChatItem.ChatItemContent.MarkdownContent -> content.markdownState.content
            is ChatItem.ChatItemContent.TextContent -> content.text
            else -> error("Only text chat items can be copied!")
        }
        mutableEvent.trySend(Event.CopyText(text))
    }

    private suspend fun Message.toChatItem(): ChatItem = when (this) {
        is Message.AssistantMessage -> {
            when (content) {
                is MessageContent.Text -> {
                    // Try to parse markdown
                    val parsingResult = parseMarkdownFlow(content = content.text)
                        .first { it is State.Success || it is State.Error }
                    when (parsingResult) {
                        is State.Success -> {
                            ChatItem.AssistantItem(
                                id = id,
                                content = ChatItem.ChatItemContent.MarkdownContent(
                                    markdownState = parsingResult,
                                ),
                                finishReason = finishReason,
                                error = error,
                            )
                        }

                        is State.Error -> {
                            logger.w(parsingResult.result) { "Failed to parse markdown" }
                            // Fallback to text content
                            ChatItem.AssistantItem(
                                id = id,
                                content = ChatItem.ChatItemContent.TextContent(
                                    text = content.text,
                                ),
                                finishReason = finishReason,
                                error = error,
                            )
                        }

                        is State.Loading -> error("Loading state should not be reached here")
                    }
                }

                is MessageContent.Audio -> TODO()
            }
        }

        is Message.UserMessage -> {
            when (content) {
                is MessageContent.Text -> {
                    ChatItem.UserItem(
                        id = id,
                        content = ChatItem.ChatItemContent.TextContent(
                            text = content.text,
                        ),
                    )
                }

                is MessageContent.Audio -> TODO()
            }
        }
    }
}

data class ChatItemsListComponentPreview(
    private val customState: ChatItemsListState = ChatItemsListState(),
) : ChatItemsListComponent {
    override val state: StateFlow<ChatItemsListState> = MutableStateFlow(
        customState,
    )
    override val event: Flow<Event> = emptyFlow()

    override fun retry(messageId: String) = Unit
    override fun copy(chatItem: ChatItem.AssistantItem) = Unit
}
