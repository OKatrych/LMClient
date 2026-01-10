@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package dev.olek.lmclient.data.repositories

import dev.olek.lmclient.data.database.ChatRoomStore
import dev.olek.lmclient.data.models.ChatRoom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ChatRoomRepository {
    suspend fun createChatRoom(modelProviderId: String, modelId: String, name: String): ChatRoom

    fun observeChatRooms(): Flow<List<ChatRoom>>

    suspend fun removeChatRoom(roomId: String)

    suspend fun updateChatRoom(room: ChatRoom)

    suspend fun setActiveChatRoom(roomId: String?)

    fun observeActiveChatRoom(): Flow<ChatRoom?>
}

@Single(binds = [ChatRoomRepository::class])
internal class ChatRoomRepositoryImpl(private val chatRoomStore: ChatRoomStore) : ChatRoomRepository {
    private val activeChatRoomId = MutableStateFlow<String?>(null)

    override suspend fun createChatRoom(modelProviderId: String, modelId: String, name: String): ChatRoom {
        val chatRoom = ChatRoom(
            id = Uuid.random().toString(),
            name = name,
            modelProviderId = modelProviderId,
            modelId = modelId,
        )

        chatRoomStore.insertChatRoom(chatRoom)
        return chatRoom
    }

    override fun observeChatRooms(): Flow<List<ChatRoom>> = chatRoomStore.observeChatRooms()

    override fun observeActiveChatRoom(): Flow<ChatRoom?> = activeChatRoomId.flatMapLatest { roomId ->
        roomId?.let { chatRoomStore.observeChatRoom(it) } ?: flowOf(null)
    }

    override suspend fun setActiveChatRoom(roomId: String?) {
        // Active chat room doesn't needs to be persisted,
        // we want to start a new one on the next app launch
        activeChatRoomId.update { roomId }
    }

    override suspend fun removeChatRoom(roomId: String) {
        chatRoomStore.removeChatRoom(roomId)
    }

    override suspend fun updateChatRoom(room: ChatRoom) {
        chatRoomStore.updateChatRoom(room)
    }
}
