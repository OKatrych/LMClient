package dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.olek.lmclient.data.models.MessageAttachment

@Composable
fun AttachmentsRow(
    attachments: List<MessageAttachment>,
    onRemoveAttachmentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow {
        // TODO
    }
}
