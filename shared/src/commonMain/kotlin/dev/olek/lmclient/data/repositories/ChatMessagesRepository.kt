package dev.olek.lmclient.data.repositories

import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.local.MessagesStoreContract
import dev.olek.lmclient.data.local.ModelProviderStoreContract
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.remote.LMClientApiProvider
import dev.olek.lmclient.data.remote.messages.PromptApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private typealias ChatRoomId = String

/**
 * Repository for managing chat messages between user and AI assistants.
 */
interface ChatMessagesRepository {
    /**
     * Observes the current list of messages for a chat room.
     *
     * @param chatRoomId The unique identifier of the chat room
     * @return Flow emitting the current messages list
     */
    fun observeMessages(chatRoomId: ChatRoomId): Flow<List<Message>>

    /**
     * Observes error events that occur during chat operations.
     *
     * @param chatRoomId The unique identifier of the chat room
     * @return Flow emitting error events
     */
    fun observeErrorEvents(chatRoomId: ChatRoomId): Flow<LMClientError>

    /**
     * Observes current status of generation status.
     *
     * @param chatRoomId The unique identifier of the chat room
     * @return true when an AI response is being generated, false otherwise
     */
    fun observeMessageGenerationStatus(chatRoomId: ChatRoomId): Flow<Boolean>

    /**
     * Sends a user message to the AI system.
     *
     * @param prompt The user message prompt to send
     * @param chatRoom The chat room context
     */
    fun generateMessage(prompt: Message.UserMessage, chatRoom: ChatRoom)

    /**
     *
     */
    fun regenerateMessage(assistantMessageId: String, chatRoom: ChatRoom)

    /**
     * Cancels ongoing message generation process.
     *
     * @param chatRoomId The unique identifier of the chat room, in case of null all
     * ongoing message generation jobs will be canceled
     */
    fun cancelMessageGeneration(chatRoomId: ChatRoomId? = null)
}

@Single(binds = [ChatMessagesRepository::class])
internal class ChatMessagesRepositoryImpl(
    private val messagesStore: MessagesStoreContract,
    private val modelProviderStore: ModelProviderStoreContract,
    private val apiProvider: LMClientApiProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : ChatMessagesRepository {
    private val logger = Logger.withTag("ChatMessagesRepositoryImpl")

    private val messageGenerationJobs = mutableMapOf<ChatRoomId, Job>()
    private val messageGenerationStatus = MutableStateFlow<List<ChatRoomId>>(emptyList())
    private val messageGenerationError = MutableSharedFlow<Pair<ChatRoomId, LMClientError>>()

    override fun observeMessages(chatRoomId: String): Flow<List<Message>> {
        logger.d { "Observing messages for chat room: $chatRoomId" }
        return messagesStore.observeMessages(chatRoomId)
    }

    override fun observeErrorEvents(chatRoomId: String): Flow<LMClientError> {
        logger.d { "Observing error events for chat room: $chatRoomId" }
        return messageGenerationError
            .onEach { (_, error) -> logger.e { "Message generation error: $error" } }
            .mapNotNull { (roomId, error) -> if (roomId == chatRoomId) error else null }
            .onEach { logger.e { "Message generation error for chat room $chatRoomId: $it" } }
    }

    override fun observeMessageGenerationStatus(chatRoomId: String): Flow<Boolean> {
        logger.d { "Observing message generation status for chat room: $chatRoomId" }
        return messageGenerationStatus.map { chatRoomId in it }
    }

    override fun generateMessage(prompt: Message.UserMessage, chatRoom: ChatRoom) {
        logger.d { "Sending message: $prompt to chat room: ${chatRoom.id}" }
        messageGenerationJobs[chatRoom.id]?.cancel()
        messageGenerationJobs[chatRoom.id] = coroutineScope
            .launch {
                updateGenerationStatus(chatRoom.id, true)

                val chatHistory = messagesStore.observeMessages(chatRoom.id).first()
                messagesStore.insertFullMessage(chatRoomId = chatRoom.id, message = prompt)
                val model = modelProviderStore
                    .getModels(chatRoom.modelProviderId)
                    .find { it.id == chatRoom.modelId }
                    ?: error("Cannot find model for chat room: ${chatRoom.id}")
                val modelProvider = modelProviderStore
                    .observeModelProvider(chatRoom.modelProviderId)
                    .first()

                if (prompt.content is MessageContent.Text) {
                    pushStreamMessage(chatHistory + prompt, chatRoom, modelProvider, model)
                } else {
                    pushRegularMessage(chatHistory + prompt, chatRoom, modelProvider, model)
                }
            }.also {
                it.invokeOnCompletion {
                    updateGenerationStatus(chatRoom.id, false)
                }
            }
    }

    override fun regenerateMessage(assistantMessageId: String, chatRoom: ChatRoom) {
        logger.d { "Regenerating $assistantMessageId message" }
        messageGenerationJobs[chatRoom.id]?.cancel()
        coroutineScope.launch {
            val messages = messagesStore.observeMessages(chatRoom.id).first()

            val index = messages.indexOfLast { it.id == assistantMessageId }
            if (index == -1) {
                logger.e { "Cannot find message with id: $assistantMessageId" }
                return@launch
            }
            val prompt = messages.getOrNull(index - 1) as? Message.UserMessage
            if (prompt == null) {
                logger.e { "Cannot find UserMessage preceding AssistantMessage: $assistantMessageId" }
                return@launch
            }
            messagesStore.deleteMessageAndSubsequent(chatRoom.id, assistantMessageId)
            generateMessage(prompt, chatRoom)
        }
    }

    override fun cancelMessageGeneration(chatRoomId: ChatRoomId?) {
        if (chatRoomId != null) {
            logger.d { "Cancelling message generation for chat room: $chatRoomId" }
            messageGenerationJobs[chatRoomId]?.cancel()
            updateGenerationStatus(chatRoomId, false)
        } else {
            logger.d { "Cancelling all message generations" }
            messageGenerationJobs.forEach { (_, job) -> job.cancel() }
            updateGenerationStatus(null, false)
        }
    }

    private suspend fun pushStreamMessage(
        prompt: List<Message>,
        chatRoom: ChatRoom,
        modelProvider: ModelProvider,
        model: Model,
    ) {
        var chunkId: String? = null
        apiProvider
            .getPromptApi(modelProvider)
            .createMessageStream(
                prompt = prompt,
                model = model,
            ).collect { response ->
                when (response) {
                    is PromptApi.MessageStreamResult.Chunk -> {
                        logger.d { "Received chunk: $response" }
                        chunkId = response.id
                        messagesStore.insertStreamMessage(
                            messageId = response.id,
                            chatRoomId = chatRoom.id,
                            contentChunk = response.content,
                        )
                    }

                    is PromptApi.MessageStreamResult.Finished -> {
                        logger.d { "Received finish event: $response" }
                        chunkId = response.id
                        messagesStore.insertStreamMessage(
                            messageId = response.id,
                            chatRoomId = chatRoom.id,
                            contentChunk = "",
                            finishReason = response.reason,
                        )
                    }

                    is PromptApi.MessageStreamResult.Error -> {
                        logger.w { "Received error event: $response" }
                        chunkId?.let { chunkId ->
                            messagesStore.insertStreamMessage(
                                messageId = chunkId,
                                chatRoomId = chatRoom.id,
                                contentChunk = "",
                                error = response.error,
                            )
                        } ?: messageGenerationError.emit(chatRoom.id to response.error)
                    }
                }
            }
    }

    private suspend fun pushRegularMessage(
        chatHistory: List<Message>,
        chatRoom: ChatRoom,
        modelProvider: ModelProvider,
        model: Model,
    ) {
        apiProvider
            .getPromptApi(modelProvider)
            .createMessage(
                prompt = chatHistory,
                model = model,
            ).onLeft { error ->
                messageGenerationError.emit(chatRoom.id to error)
            }.onRight { systemMessage ->
                messagesStore.insertFullMessage(
                    chatRoomId = chatRoom.id,
                    message = systemMessage,
                )
            }
    }

    private fun updateGenerationStatus(chatRoomId: ChatRoomId?, isGenerating: Boolean) {
        messageGenerationStatus.update {
            if (chatRoomId != null) {
                if (isGenerating) it + chatRoomId else it - chatRoomId
            } else {
                emptyList()
            }
        }
    }
}
