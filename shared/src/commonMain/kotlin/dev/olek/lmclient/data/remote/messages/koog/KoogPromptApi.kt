@file:OptIn(ExperimentalUuidApi::class)

package dev.olek.lmclient.data.remote.messages.koog

import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import arrow.core.Either
import co.touchlab.kermit.Logger
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.MessageFinishReason
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.remote.mappers.KoogMessageMapper
import dev.olek.lmclient.data.remote.mappers.KoogStreamFrameMapper
import dev.olek.lmclient.data.remote.mappers.toDomainError
import dev.olek.lmclient.data.remote.mappers.toKoogModel
import dev.olek.lmclient.data.remote.messages.PromptApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class KoogPromptApi(
    private val promptExecutor: PromptExecutor,
    private val messageMapper: KoogMessageMapper,
    private val streamFrameMapper: KoogStreamFrameMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PromptApi {
    private val logger = Logger.withTag("KoogPromptApi")

    override suspend fun createMessage(
        prompt: List<Message>,
        model: Model,
    ): Either<LMClientError, Message.AssistantMessage> = withContext(dispatcher) {
        logger.d { "createMessage(prompt=${prompt.map { it.id }}, modelId=$model)" }
        return@withContext Either
            .catch {
                promptExecutor
                    .execute(
                        prompt = prompt.toKoogPrompt(),
                        model = model.toKoogModel(),
                    ).first()
                    .let { messageMapper.map(it) } as Message.AssistantMessage
            }.mapLeft {
                it.toDomainError()
            }
    }

    override suspend fun createMessageStream(
        prompt: List<Message>,
        model: Model,
    ): Flow<PromptApi.MessageStreamResult> {
        logger.d { "createMessageStream(prompt=${prompt.map { it.id }}, modelId=$model)" }
        require(prompt.last().content is MessageContent.Text) {
            "Streaming supports only text messages"
        }

        val messageId = Uuid.random().toHexDashString()
        return promptExecutor
            .executeStreaming(
                prompt = prompt.toKoogPrompt(),
                model = model.toKoogModel(),
            ).map { frame ->
                streamFrameMapper.map(messageId, frame)
            }.catch { error ->
                if (error is IllegalStateException &&
                    error.message == "StandaloneCoroutine was cancelled"
                ) {
                    logger.d { "Streaming was cancelled" }
                    emit(
                        PromptApi.MessageStreamResult.Finished(
                            id = messageId,
                            reason = MessageFinishReason.Stop,
                        ),
                    )
                } else {
                    logger.e(error) { "Error while streaming message" }
                    emit(
                        PromptApi.MessageStreamResult.Error(error.toDomainError()),
                    )
                }
            }
    }

    private suspend fun List<Message>.toKoogPrompt(): Prompt {
        val mapped = map { messageMapper.map(it) }
        return prompt(Uuid.random().toHexDashString()) {
            messages(mapped)
        }
    }
}
