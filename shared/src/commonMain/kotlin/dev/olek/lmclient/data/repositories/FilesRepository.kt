package dev.olek.lmclient.data.repositories

import arrow.core.Either
import dev.olek.lmclient.data.models.AttachmentContent
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.ModelProvider

/**
 * Repository for handling file attachments for chat messages.
 *
 * Handles two storage strategies based on provider capabilities:
 * - **Files API**: For providers that support file uploads (e.g., OpenAI, Claude, Google),
 *   files are uploaded to the provider's storage and a URL is returned.
 * - **Local Base64**: For providers without Files API support (e.g., OpenRouter),
 *   files are stored locally and referenced by their local URI.
 */
interface FilesRepository {

    /**
     * Processes user file for attachment to a message.
     *
     * Based on the provider's capabilities, this will either:
     * - Upload the file to the provider's Files API and return a [AttachmentContent.RemoteFile]
     * - Store the file locally as base64 and return a [AttachmentContent.LocalFile]
     */
    suspend fun processFile(
        fileUri: String,
        mimeType: String,
        provider: ModelProvider,
    ): Either<LMClientError, AttachmentContent>

    /**
     * Process assistant file for attachment to a message.
     *
     * Stores the file locally as Base64 and return a [AttachmentContent.LocalFile]
     *
     */
    suspend fun processFile(
        base64: String,
        mimeType: String,
    ): Either<LMClientError, AttachmentContent.LocalFile>

    /**
     * Deletes a previously uploaded or stored file.
     *
     * For remote files, this will call the provider's delete API.
     * For local files, this will remove the file from local storage.
     *
     */
    suspend fun deleteFile(
        content: AttachmentContent,
        provider: ModelProvider,
    ): Either<LMClientError, Unit>

    /**
     * Retrieves the raw bytes of a file from its attachment content.
     *
     * Useful for re-uploading files when switching providers or for display purposes.
     *
     */
    suspend fun getFileBytes(content: AttachmentContent): Either<LMClientError, ByteArray>
}
