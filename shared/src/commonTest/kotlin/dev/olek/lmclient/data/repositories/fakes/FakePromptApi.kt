package dev.olek.lmclient.data.repositories.fakes

import arrow.core.Either
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.messages.PromptApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakePromptApi : PromptApi {

    var createMessageResult: Either<LMClientError, Message.AssistantMessage> =
        Either.Left(LMClientError.UnknownError("Not configured"))
    var createMessageStreamResult: Flow<PromptApi.MessageStreamResult> = emptyFlow()

    val createMessageCalls = mutableListOf<List<Message>>()
    val createMessageStreamCalls = mutableListOf<List<Message>>()

    override suspend fun createMessage(
        prompt: List<Message>,
        model: Model,
    ): Either<LMClientError, Message.AssistantMessage> {
        createMessageCalls.add(prompt)
        return createMessageResult
    }

    override suspend fun createMessageStream(
        prompt: List<Message>,
        model: Model,
    ): Flow<PromptApi.MessageStreamResult> {
        createMessageStreamCalls.add(prompt)
        return createMessageStreamResult
    }
}
