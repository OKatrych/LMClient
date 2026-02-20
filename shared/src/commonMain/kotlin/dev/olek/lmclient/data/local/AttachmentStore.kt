package dev.olek.lmclient.data.local

import io.github.vinceglb.filekit.BookmarkData
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.fromBookmarkData
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Interface for storing and retrieving attachment content.
 */
interface AttachmentStore {
    /**
     * Saves attachment content to storage.
     * @param base64 Base64-encoded content
     * @param mimeType MIME type of the content
     * @return [BookmarkData] reference to the saved file
     */
    suspend fun saveAttachment(base64: String, mimeType: String): BookmarkData

    /**
     * Retrieves attachment content from storage.
     * @param bookmarkData Reference to the stored attachment
     * @return Base64-encoded content
     */
    suspend fun getAttachmentContent(bookmarkData: BookmarkData): String?

    /**
     * Removes an attachment from storage.
     * @param bookmarkData Reference to the attachment to remove
     */
    suspend fun removeAttachment(bookmarkData: BookmarkData)
}

@Single(binds = [AttachmentStore::class])
internal class AttachmentStoreImpl : AttachmentStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val attachmentsDir: PlatformFile by lazy {
        (FileKit.cacheDir / "attachments").also {
            if (!it.exists()) it.createDirectories()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveAttachment(
        base64: String,
        mimeType: String,
    ): BookmarkData = withContext(Dispatchers.IO) {
        val fileName = "${Uuid.random()}.json"
        val file = attachmentsDir / fileName
        val stored = StoredAttachment(mimeType, base64)
        file.writeString(json.encodeToString(stored))
        file.bookmarkData()
    }

    override suspend fun getAttachmentContent(
        bookmarkData: BookmarkData,
    ): String? = withContext(Dispatchers.IO) {
        val file = PlatformFile.fromBookmarkData(bookmarkData)
        if (file.exists()) {
            val stored = json.decodeFromString<StoredAttachment>(file.readString())
            stored.content
        } else {
            null
        }
    }

    override suspend fun removeAttachment(
        bookmarkData: BookmarkData,
    ) = withContext(Dispatchers.IO) {
        PlatformFile.fromBookmarkData(bookmarkData).delete(mustExist = false)
    }
}

@Serializable
data class StoredAttachment(
    val mimeType: String,
    val content: String,
)
