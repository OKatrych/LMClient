package dev.olek.lmclient.data.repositories.fakes

import dev.olek.lmclient.data.local.MessagesStoreContract
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageFinishReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeMessagesStore : MessagesStoreContract {

    private val messagesState = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    val insertedMessages = mutableListOf<Pair<String, Message>>()
    val streamMessages = mutableListOf<StreamMessageRecord>()
    val deletedMessages = mutableListOf<Pair<String, String>>()

    data class StreamMessageRecord(
        val messageId: String,
        val chatRoomId: String,
        val contentChunk: String,
        val finishReason: MessageFinishReason?,
        val error: LMClientError?,
    )

    fun setMessages(chatRoomId: String, messages: List<Message>) {
        messagesState.value += (chatRoomId to messages)
    }

    override fun observeMessages(chatRoomId: String): Flow<List<Message>> =
        messagesState.map { it[chatRoomId] ?: emptyList() }

    override suspend fun insertFullMessage(chatRoomId: String, message: Message) {
        insertedMessages.add(chatRoomId to message)
        val current = messagesState.value[chatRoomId] ?: emptyList()
        messagesState.value += (chatRoomId to (current + message))
    }

    override suspend fun insertStreamMessage(
        messageId: String,
        chatRoomId: String,
        contentChunk: String,
        finishReason: MessageFinishReason?,
        error: LMClientError?,
    ) {
        streamMessages.add(
            StreamMessageRecord(messageId, chatRoomId, contentChunk, finishReason, error)
        )
    }

    override suspend fun getMessage(messageId: String): Message? =
        messagesState.value.values.flatten().find { it.id == messageId }

    override suspend fun deleteMessageAndSubsequent(chatRoomId: String, messageId: String) {
        deletedMessages.add(chatRoomId to messageId)
        val messages = messagesState.value[chatRoomId] ?: return
        val index = messages.indexOfFirst { it.id == messageId }
        if (index >= 0) {
            messagesState.value += (chatRoomId to messages.subList(0, index))
        }
    }
}
