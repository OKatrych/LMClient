@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.repositories

import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.MessageFinishReason
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.remote.messages.PromptApi
import dev.olek.lmclient.data.repositories.fakes.FakeLMClientApiProvider
import dev.olek.lmclient.data.repositories.fakes.FakeMessagesStore
import dev.olek.lmclient.data.repositories.fakes.FakeModelProviderStore
import dev.olek.lmclient.data.repositories.fakes.FakePromptApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalCoroutinesApi::class)
class ChatMessagesRepositoryTest {

    private val messagesStore = FakeMessagesStore()
    private val modelProviderStore = FakeModelProviderStore()
    private val fakePromptApi = FakePromptApi()
    private val apiProvider = FakeLMClientApiProvider(fakePromptApi)

    private fun TestScope.createRepository(): ChatMessagesRepositoryImpl {
        setupDefaultProvider()
        return ChatMessagesRepositoryImpl(
            messagesStore = messagesStore,
            modelProviderStore = modelProviderStore,
            apiProvider = apiProvider,
            coroutineScope = this,
        )
    }

    private fun setupDefaultProvider() {
        modelProviderStore.setProvider(TEST_PROVIDER)
        modelProviderStore.setModels(
            TEST_PROVIDER.id,
            listOf(TEST_MODEL),
        )
    }

    @Test
    fun `generateMessage inserts user message into store`() = runTest {
        val repository = createRepository()
        fakePromptApi.createMessageStreamResult = flowOf()

        repository.generateMessage(TEST_USER_MESSAGE, TEST_CHAT_ROOM)
        advanceUntilIdle()

        assertTrue(messagesStore.insertedMessages.any { (roomId, msg) ->
            roomId == TEST_CHAT_ROOM.id && msg.id == TEST_USER_MESSAGE.id
        })
    }

    @Test
    fun `generateMessage calls stream API for Text content`() = runTest {
        val repository = createRepository()
        fakePromptApi.createMessageStreamResult = flowOf()

        repository.generateMessage(TEST_USER_MESSAGE, TEST_CHAT_ROOM)
        advanceUntilIdle()

        assertEquals(1, fakePromptApi.createMessageStreamCalls.size)
    }

    @Test
    fun `generateMessage processes stream chunks`() = runTest {
        val repository = createRepository()
        fakePromptApi.createMessageStreamResult = flowOf(
            PromptApi.MessageStreamResult.Chunk("msg-1", "Hello "),
            PromptApi.MessageStreamResult.Chunk("msg-1", "World"),
            PromptApi.MessageStreamResult.Finished("msg-1", MessageFinishReason.Stop),
        )

        repository.generateMessage(TEST_USER_MESSAGE, TEST_CHAT_ROOM)
        advanceUntilIdle()

        val chunks = messagesStore.streamMessages
        assertEquals(3, chunks.size)
        assertEquals("Hello ", chunks[0].contentChunk)
        assertEquals("World", chunks[1].contentChunk)
        assertEquals("", chunks[2].contentChunk)
        assertEquals(MessageFinishReason.Stop, chunks[2].finishReason)
    }

    @Test
    fun `generateMessage handles stream error with existing chunkId`() = runTest {
        val repository = createRepository()
        fakePromptApi.createMessageStreamResult = flowOf(
            PromptApi.MessageStreamResult.Chunk("msg-1", "Partial"),
            PromptApi.MessageStreamResult.Error(LMClientError.Timeout),
        )

        repository.generateMessage(TEST_USER_MESSAGE, TEST_CHAT_ROOM)
        advanceUntilIdle()

        val errorRecord = messagesStore.streamMessages.last()
        assertEquals("msg-1", errorRecord.messageId)
        assertEquals(LMClientError.Timeout, errorRecord.error)
    }

    @Test
    fun `regenerateMessage deletes from assistant message onward and re-generates`() = runTest {
        val repository = createRepository()
        fakePromptApi.createMessageStreamResult = flowOf()

        messagesStore.setMessages(
            TEST_CHAT_ROOM.id,
            listOf(
                TEST_USER_MESSAGE,
                TEST_ASSISTANT_MESSAGE,
            ),
        )

        repository.regenerateMessage(TEST_ASSISTANT_MESSAGE.id, TEST_CHAT_ROOM)
        advanceUntilIdle()

        assertTrue(messagesStore.deletedMessages.any { (roomId, msgId) ->
            roomId == TEST_CHAT_ROOM.id && msgId == TEST_ASSISTANT_MESSAGE.id
        })
        assertTrue(fakePromptApi.createMessageStreamCalls.isNotEmpty())
    }

    @Test
    fun `regenerateMessage does nothing when message not found`() = runTest {
        val repository = createRepository()
        messagesStore.setMessages(TEST_CHAT_ROOM.id, emptyList())

        repository.regenerateMessage("non-existent", TEST_CHAT_ROOM)
        advanceUntilIdle()

        assertTrue(messagesStore.deletedMessages.isEmpty())
        assertTrue(fakePromptApi.createMessageStreamCalls.isEmpty())
    }

    @Test
    fun `regenerateMessage does nothing when predecessor is not UserMessage`() = runTest {
        val repository = createRepository()
        val firstAssistant = Message.AssistantMessage(
            id = "assistant-first",
            content = MessageContent.Text("First"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )
        messagesStore.setMessages(
            TEST_CHAT_ROOM.id,
            listOf(firstAssistant, TEST_ASSISTANT_MESSAGE),
        )

        repository.regenerateMessage(TEST_ASSISTANT_MESSAGE.id, TEST_CHAT_ROOM)
        advanceUntilIdle()

        assertTrue(messagesStore.deletedMessages.isEmpty())
        assertTrue(fakePromptApi.createMessageStreamCalls.isEmpty())
    }

    @Test
    fun `cancelMessageGeneration updates status to false for specific room`() = runTest {
        val repository = createRepository()

        repository.cancelMessageGeneration(TEST_CHAT_ROOM.id)

        val status = repository.observeMessageGenerationStatus(TEST_CHAT_ROOM.id).first()
        assertFalse(status)
    }

    @Test
    fun `cancelMessageGeneration with null clears all rooms`() = runTest {
        val repository = createRepository()

        repository.cancelMessageGeneration(null)

        val status = repository.observeMessageGenerationStatus(TEST_CHAT_ROOM.id).first()
        assertFalse(status)
    }

    @Test
    fun `observeMessageGenerationStatus returns false for unknown room`() = runTest {
        val repository = createRepository()

        val status = repository.observeMessageGenerationStatus("unknown-room").first()
        assertFalse(status)
    }

    @Test
    fun `observeMessages delegates to messages store`() = runTest {
        val repository = createRepository()
        messagesStore.setMessages(TEST_CHAT_ROOM.id, listOf(TEST_USER_MESSAGE))

        val messages = repository.observeMessages(TEST_CHAT_ROOM.id).first()
        assertEquals(1, messages.size)
        assertIs<Message.UserMessage>(messages[0])
    }

    @Test
    fun `observeMessages returns empty list for unknown room`() = runTest {
        val repository = createRepository()

        val messages = repository.observeMessages("unknown-room").first()
        assertTrue(messages.isEmpty())
    }

    companion object {
        private val TEST_PROVIDER = ModelProvider(
            id = "openai",
            name = "OpenAI",
            config = ModelProvider.ModelProviderConfig.StandardConfig(
                providerId = "openai",
                apiUrl = "https://api.openai.com",
                apiKey = "test-key",
            ),
            isActive = true,
        )

        private val TEST_MODEL = Model(
            id = "gpt-4o",
            providerId = "openai",
            name = "GPT-4o",
            capabilities = listOf(Model.Capability.Completion),
            contextLength = 128000,
            maxOutputTokens = 4096,
        )

        private val TEST_CHAT_ROOM = ChatRoom(
            id = "room-1",
            name = "Test Room",
            modelProviderId = "openai",
            modelId = "gpt-4o",
        )

        private val TEST_USER_MESSAGE = Message.UserMessage(
            id = "user-1",
            content = MessageContent.Text("Hello"),
            attachments = emptyList(),
        )

        private val TEST_ASSISTANT_MESSAGE = Message.AssistantMessage(
            id = "assistant-1",
            content = MessageContent.Text("Hi there!"),
            attachments = emptyList(),
            finishReason = MessageFinishReason.Stop,
            error = null,
        )
    }
}
