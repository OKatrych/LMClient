package dev.olek.lmclient.data.local

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.shared.data.Database
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
internal class ChatRoomStore(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun insertChatRoom(chatRoom: ChatRoom) = withContext(dispatcher) {
        database.chatRoomsQueries.insertChatRoom(
            id = chatRoom.id,
            name = chatRoom.name,
            provider_id = chatRoom.modelProviderId,
            model_id = chatRoom.modelId,
            system_prompt = null, // TODO: Add system prompt to ChatRoom model
            temperature = null, // TODO: Add temperature to ChatRoom model
        )
    }

    suspend fun getChatRoom(id: String): ChatRoom? = withContext(dispatcher) {
        database.chatRoomsQueries
            .getChatRoom(id, mapper = ::chatRoomMapper)
            .awaitAsOneOrNull()
    }

    fun observeChatRooms(): Flow<List<ChatRoom>> = database.chatRoomsQueries
        .getAllChatRooms(mapper = ::chatRoomMapper)
        .asFlow()
        .mapToList(dispatcher)
        .flowOn(dispatcher)

    suspend fun removeChatRoom(id: String) = withContext(dispatcher) {
        database.chatRoomsQueries.deleteChatRoom(id)
    }

    fun observeChatRoom(id: String): Flow<ChatRoom?> = database.chatRoomsQueries
        .observeChatRoom(id, mapper = ::chatRoomMapper)
        .asFlow()
        .mapToOneOrNull(dispatcher)
        .distinctUntilChanged()
        .flowOn(dispatcher)

    suspend fun updateChatRoom(chatRoom: ChatRoom) = withContext(dispatcher) {
        database.chatRoomsQueries.updateChatRoom(
            name = chatRoom.name,
            system_prompt = null, // TODO: Add system prompt to ChatRoom model
            temperature = null, // TODO: Add temperature to ChatRoom model
            model_id = chatRoom.modelId,
            id = chatRoom.id,
        )
    }

    @Suppress("UnusedParameter")
    private fun chatRoomMapper(
        id: String,
        name: String,
        providerId: String,
        modelId: String?,
        systemPrompt: String?,
        temperature: Double?,
        createdAt: String,
        updatedAt: String,
    ): ChatRoom = ChatRoom(
        id = id,
        name = name,
        modelProviderId = providerId,
        modelId = modelId,
    )
}
