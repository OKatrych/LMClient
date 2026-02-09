package dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput

import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.olek.lmclient.data.models.MessageAttachment

@Composable
fun AttachmentsRow(
    attachments: List<MessageAttachment>,
    onRemoveAttachmentClick: (MessageAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LookaheadScope {
        Box(modifier = Modifier.animateBounds(this)) {
            if (attachments.isNotEmpty()) {
                FlowRow(
                    modifier = modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    attachments.fastForEach { attachment ->
                        AttachmentChip(
                            attachment = attachment,
                            onRemoveClick = { onRemoveAttachmentClick(attachment) },
                        )
                    }
                }
            }
        }
    }
}
