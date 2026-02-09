package dev.olek.lmclient.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import org.koin.compose.koinInject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun rememberAttachmentImageModel(
    content: AttachmentContentReference,
): Any? {
    val repository = koinInject<AttachmentsRepository>()
    return produceState<Any?>(initialValue = null, content) {
        value = when (content) {
            is AttachmentContentReference.RemoteFile -> content.url
            is AttachmentContentReference.LocalFile -> {
                repository.getAttachmentContent(content)
                    ?.base64
                    ?.let(Base64::decode)
            }
        }
    }.value
}
