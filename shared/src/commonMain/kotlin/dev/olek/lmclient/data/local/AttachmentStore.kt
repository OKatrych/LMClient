package dev.olek.lmclient.data.local

class AttachmentStore {

    /**
     * @return URI of saved file
     */
    suspend fun saveAttachment(
        base64: String,
        mimeType: String,
    ): String {
        TODO()
    }

    /**
     * @return Base64 encoded file
     */
    suspend fun getAttachmentContent(
        uri: String,
    ): String {
        TODO()
    }

    suspend fun removeAttachment(uri: String) {
        TODO()
    }
}
