package dev.olek.lmclient.data.remote.messages

import arrow.core.Either
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageFinishReason
import dev.olek.lmclient.data.models.Model
import kotlinx.coroutines.flow.Flow

interface PromptApi {
    suspend fun createMessage(
        prompt: List<Message>,
        model: Model
    ): Either<LMClientError, Message.AssistantMessage>

    suspend fun createMessageStream(
        prompt: List<Message>,
        model: Model,
    ): Flow<MessageStreamResult>

    sealed interface MessageStreamResult {

        data class Chunk(
            val id: String,
            val content: String,
        ) : MessageStreamResult

        data class Finished(
            val id: String,
            val reason: MessageFinishReason?,
        ) : MessageStreamResult

        data class Error(val error: LMClientError) : MessageStreamResult
    }
}
