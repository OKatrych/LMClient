package dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.util.rememberAttachmentImageModel
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.attachment_remove_desc
import lm_client.shared.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AttachmentItem(
    attachment: MessageAttachment,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImage = attachment.mimeType.startsWith("image/")

    Box(modifier = modifier) {
        if (isImage) {
            ImageAttachmentItem(
                attachment = attachment,
            )
        } else {
            FileAttachmentItem(
                attachment = attachment,
            )
        }

        RemoveButton(
            onClick = onRemoveClick,
            showShadow = isImage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp),
        )
    }
}

@Composable
private fun ImageAttachmentItem(
    attachment: MessageAttachment,
    modifier: Modifier = Modifier,
) {
    val model = rememberAttachmentImageModel(attachment.content)
    AsyncImage(
        modifier = modifier
            .size(76.dp)
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.background),
        onError = {
            Logger.withTag("AttachmentChip")
                .e(it.result.throwable) { it.result.throwable.message ?: "" }
        },
        contentScale = ContentScale.Crop,
        model = model,
        contentDescription = null,
    )
}

@Composable
private fun FileAttachmentItem(
    attachment: MessageAttachment,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(76.dp)
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.background),
    ) {
        Text(
            text = attachment.fileName ?: "File",
            style = AppTheme.typography.footnote,
            color = AppTheme.colors.text,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            modifier = Modifier
                .padding(start = 8.dp, end = 24.dp, top = 8.dp, bottom = 4.dp)
                .weight(1f),
        )
        Box(
            modifier = Modifier
                .padding(start = 4.dp, bottom = 4.dp)
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

@Composable
private fun RemoveButton(
    onClick: () -> Unit,
    showShadow: Boolean,
    modifier: Modifier = Modifier,
) {
    val shadowModifier = if (showShadow) {
        val radiusPx = with(LocalDensity.current) { 4.dp.toPx() }
        val shadowColor = AppTheme.colors.onPrimary.copy(alpha = 0.5f)
        Modifier.dropShadow(CircleShape) {
            radius = radiusPx
            color = shadowColor
            offset = Offset(x = 0f, y = radiusPx)
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .then(shadowModifier)
            .background(AppTheme.colors.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_close),
            contentDescription = stringResource(Res.string.attachment_remove_desc),
            tint = AppTheme.colors.onPrimary,
            modifier = Modifier.size(18.dp),
        )
    }
}
