package dev.olek.lmclient.data.local

import io.github.vinceglb.filekit.BookmarkData
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
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

@Single
class AttachmentStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val attachmentsDir: PlatformFile by lazy {
        (FileKit.filesDir / "attachments").also {
            if (!it.exists()) it.createDirectories()
        }
    }

    /**
     * @return [BookmarkData] of saved file
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun saveAttachment(
        base64: String,
        mimeType: String,
    ): BookmarkData = withContext(Dispatchers.IO) {
        val fileName = "${Uuid.random()}.json"
        val file = attachmentsDir / fileName
        val stored = StoredAttachment(mimeType, base64)
        file.writeString(json.encodeToString(stored))
        file.bookmarkData()
    }

    /**
     * @return Base64 encoded file
     */
    suspend fun getAttachmentContent(
        bookmarkData: BookmarkData,
    ): String = withContext(Dispatchers.IO) {
        val file = PlatformFile.fromBookmarkData(bookmarkData)
        val stored = json.decodeFromString<StoredAttachment>(file.readString())
        stored.content
    }

    suspend fun removeAttachment(
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
