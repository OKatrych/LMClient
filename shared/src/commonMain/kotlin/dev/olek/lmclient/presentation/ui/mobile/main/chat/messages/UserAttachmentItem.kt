package dev.olek.lmclient.presentation.ui.mobile.main.chat.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.models.id
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.util.rememberAttachmentImageModel

@Composable
internal fun UserAttachmentsRow(
    attachments: List<MessageAttachment>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End),
    ) {
        items(
            items = attachments,
            key = { it.content.id },
        ) { attachment ->
            UserAttachmentItem(attachment = attachment)
        }
    }
}

@Composable
private fun UserAttachmentItem(
    attachment: MessageAttachment,
    modifier: Modifier = Modifier,
) {
    if (attachment.mimeType.startsWith("image/")) {
        UserImageAttachmentItem(modifier = modifier, attachment = attachment)
    } else {
        UserFileAttachmentItem(modifier = modifier, attachment = attachment)
    }
}

@Composable
private fun UserImageAttachmentItem(
    attachment: MessageAttachment,
    modifier: Modifier = Modifier,
) {
    val model = rememberAttachmentImageModel(attachment.content)
    AsyncImage(
        modifier = modifier
            .size(84.dp)
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.background),
        onError = {
            Logger.withTag("UserAttachmentItem")
                .e(it.result.throwable) { it.result.throwable.message ?: "" }
        },
        contentScale = ContentScale.Crop,
        model = model,
        contentDescription = null,
    )
}

@Composable
private fun UserFileAttachmentItem(
    attachment: MessageAttachment,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(84.dp)
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.background),
    ) {
        Text(
            text = attachment.fileName ?: "File",
            style = AppTheme.typography.footnoteMedium,
            color = AppTheme.colors.textSecondary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                .weight(1f),
        )
        Box(
            modifier = Modifier
                .padding(start = 8.dp, bottom = 8.dp)
                .background(AppTheme.colors.primary, shape = AppTheme.shapes.button),
        ) {
            Text(
                text = attachment.format.uppercase(),
                style = AppTheme.typography.labelSemiBold,
                color = AppTheme.colors.onPrimary,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
            )
        }
    }
}
