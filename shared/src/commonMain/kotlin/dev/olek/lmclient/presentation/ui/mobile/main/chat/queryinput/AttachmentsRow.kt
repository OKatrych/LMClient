package dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput

import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.presentation.ui.mobile.common.PreviewWrapper

@Composable
fun AttachmentsRow(
    attachments: List<MessageAttachment>,
    onRemoveAttachmentClick: (MessageAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LookaheadScope {
        Box(modifier = Modifier.animateBounds(this)) {
            if (attachments.isNotEmpty()) {
                LazyRow(
                    modifier = modifier,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    itemsIndexed(
                        items = attachments,
                        key = { _, attachment -> attachment.content.key },
                    ) { index, attachment ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        AttachmentItem(
                            modifier = Modifier.animateItem(),
                            attachment = attachment,
                            onRemoveClick = { onRemoveAttachmentClick(attachment) },
                        )
                    }
                }
            }
        }
    }
}

private val AttachmentContentReference.key: String
    get() = when (this) {
        is AttachmentContentReference.LocalFile -> pathBytes.decodeToString()
        is AttachmentContentReference.RemoteFile -> url
    }

@Preview
@Composable
private fun AttachmentsRowPreview() = PreviewWrapper {
    AttachmentsRow(
        attachments = listOf(
            MessageAttachment(
                content = AttachmentContentReference.RemoteFile("test.com/image.png"),
                format = "png",
                mimeType = "image/png",
                fileName = "image"
            ),
            MessageAttachment(
                content = AttachmentContentReference.RemoteFile("test.com/image2.png"),
                format = "png",
                mimeType = "image/png",
                fileName = "image"
            ),

            MessageAttachment(
                content = AttachmentContentReference.RemoteFile("test.com/image3.png"),
                format = "png",
                mimeType = "image/png",
                fileName = "image"
            )
        ),
        onRemoveAttachmentClick = {},
    )
}
