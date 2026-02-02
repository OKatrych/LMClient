package dev.olek.lmclient.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.olek.lmclient.data.local.mapper.MessageAttachmentMapper
import dev.olek.lmclient.shared.data.Database
import dev.olek.lmclient.data.local.mapper.MessageMapper
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageFinishReason
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
internal class MessagesStore(
    private val database: Database,
    private val messageMapper: MessageMapper,
    private val messageAttachmentMapper: MessageAttachmentMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun observeMessages(chatRoomId: String): Flow<List<Message>> = database.messagesQueries
        .getMessages(chatRoomId)
        .asFlow()
        .mapToList(dispatcher)
        .map { messages ->
            messages.map { messageDb ->
                val attachmentsDb = database.messagesQueries
                    .getMessageAttachments(messageDb.id)
                    .executeAsList()
                val attachments = attachmentsDb.map(messageAttachmentMapper::mapFromDbModel)
                messageMapper.mapFromDbModel(messageDb, attachments)
            }
        }
        .distinctUntilChanged()
        .flowOn(dispatcher)

    suspend fun insertFullMessage(chatRoomId: String, message: Message) = withContext(dispatcher) {
        database.transaction {
            val dbModel = messageMapper.mapToDbModel(message, chatRoomId)

            database.messagesQueries.insertMessage(
                id = dbModel.id,
                chat_room_id = dbModel.chat_room_id,
                role = dbModel.role,
                content_text = dbModel.content_text,
                content_audio = dbModel.content_audio,
                content_type = dbModel.content_type,
                finish_reason_type = dbModel.finish_reason_type,
                finish_reason_message = dbModel.finish_reason_message,
                error_type = dbModel.error_type,
                error_message = dbModel.error_message,
                token_count = dbModel.token_count,
            )

            message.attachments.forEach { attachment ->
                val attachmentDb = messageAttachmentMapper.mapToDbModel(
                    attachment = attachment,
                    messageId = message.id
                )
                database.messagesQueries.insertAttachment(
                    id = attachmentDb.id,
                    message_id = attachmentDb.message_id,
                    content_reference_type = attachmentDb.content_reference_type,
                    content_reference = attachmentDb.content_reference,
                    file_type = attachmentDb.file_type,
                    mime_type = attachmentDb.mime_type,
                    file_name = attachmentDb.file_name,
                )
            }
        }
    }

    suspend fun insertStreamMessage(
        messageId: String,
        chatRoomId: String,
        contentChunk: String,
        finishReason: MessageFinishReason? = null,
        error: LMClientError? = null,
    ) = withContext(dispatcher) {
        val (finishReasonType, finishReasonMessage) = messageMapper.getFinishReasonDbFormat(
            finishReason
        )
        val (errorType, errorMessage) = messageMapper.getErrorDbFormat(error)

        database.messagesQueries.upsertStreamingMessage(
            id = messageId,
            chat_room_id = chatRoomId,
            content_text = contentChunk,
            content_audio = null,
            finish_reason_type = finishReasonType,
            finish_reason_message = finishReasonMessage,
            error_type = errorType,
            error_message = errorMessage,
            token_count = null,
        )
    }

    suspend fun getMessage(messageId: String): Message? = withContext(dispatcher) {
        database.messagesQueries
            .getMessageById(messageId)
            .executeAsOneOrNull()
            ?.let { messageDb ->
                val attachmentsDb = database.messagesQueries
                    .getMessageAttachments(messageId)
                    .executeAsList()
                val attachments = attachmentsDb.map(messageAttachmentMapper::mapFromDbModel)
                messageMapper.mapFromDbModel(messageDb, attachments)
            }
    }

    suspend fun deleteMessageAndSubsequent(
        chatRoomId: String,
        messageId: String
    ) = withContext(dispatcher) {
        database.messagesQueries.deleteMessageAndSubsequent(chatRoomId, messageId)
    }
}
