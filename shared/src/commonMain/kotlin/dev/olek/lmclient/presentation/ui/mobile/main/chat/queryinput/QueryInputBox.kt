@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.olek.lmclient.data.models.AttachmentContentReference
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.presentation.components.main.QueryInputComponent
import dev.olek.lmclient.presentation.components.main.QueryInputComponent.State.AttachmentsState
import dev.olek.lmclient.presentation.components.main.QueryInputComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.PreviewWrapper
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.attachment_pick_file_desc
import lm_client.shared.generated.resources.attachment_pick_image_desc
import lm_client.shared.generated.resources.ic_arrow_up
import lm_client.shared.generated.resources.ic_attachment
import lm_client.shared.generated.resources.ic_camera
import lm_client.shared.generated.resources.ic_stop
import lm_client.shared.generated.resources.query_input_placeholder
import lm_client.shared.generated.resources.query_input_stop_desc
import lm_client.shared.generated.resources.query_input_submit_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QueryInputBox(
    component: QueryInputComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()
    val cameraPickerLauncher = rememberCameraPickerLauncher { file ->
        file?.let(component::onAddAttachment)
    }
    val filePickerLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = state.attachmentsState.supportedExtensions),
    ) { files ->
        files?.forEach(component::onAddAttachment)
    }

    AnimatedVisibility(
        visible = state.isEnabled,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Surface(
            modifier = modifier
                .clip(QueryInputBoxShape)
                .background(AppTheme.colors.backgroundSecondary)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            color = Color.Transparent,
            shape = QueryInputBoxShape,
            content = {
                QueryInputBoxContent(
                    query = state.query,
                    attachmentsState = state.attachmentsState,
                    isLoading = state.isLoading,
                    onQueryChange = component::onQueryChange,
                    onAddPhotoClick = { cameraPickerLauncher?.launch() },
                    onAddFileClick = { filePickerLauncher.launch() },
                    onRemoveAttachmentClick = component::onRemoveAttachment,
                    onSubmit = component::onQuerySubmit,
                    onCancel = component::onQueryCancel,
                )
            },
        )
    }
}

@Composable
private fun QueryInputBoxContent(
    query: String,
    attachmentsState: AttachmentsState,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    onAddFileClick: () -> Unit,
    onRemoveAttachmentClick: (MessageAttachment) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    Column {
        AttachmentsRow(
            modifier = Modifier.padding(top = 16.dp),
            attachments = attachmentsState.attachments,
            onRemoveAttachmentClick = onRemoveAttachmentClick,
        )
        InputField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 8.dp),
            query = query,
            onQueryChange = onQueryChange,
        )
        ButtonsContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            leadingButton = {
                Row {
                    if (attachmentsState.canAttachImages) {
                        IconButton(onClick = onAddPhotoClick) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_camera),
                                contentDescription = stringResource(Res.string.attachment_pick_image_desc),
                                tint = AppTheme.colors.icon,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    if (attachmentsState.canAttachDocuments) {
                        IconButton(onClick = onAddFileClick) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_attachment),
                                contentDescription = stringResource(Res.string.attachment_pick_file_desc),
                                tint = AppTheme.colors.icon,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            },
            trailingButton = {
                RoundedAnimatedButton(
                    onClick = if (isLoading) onCancel else onSubmit,
                    isVisible = query.isNotBlank() ||
                            isLoading || attachmentsState.attachments.isNotEmpty(),
                    icon = {
                        if (isLoading) {
                            CancelIcon(Modifier.padding(8.dp))
                        } else {
                            SubmitIcon(Modifier.padding(8.dp))
                        }
                    },
                )
            },
        )
    }
}

@Composable
private fun InputField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        modifier = modifier,
        value = query,
        onValueChange = onQueryChange,
        maxLines = 7,
        placeholder = {
            Text(
                text = stringResource(Res.string.query_input_placeholder),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text,
            )
        },
        textStyle = AppTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.text,
            unfocusedTextColor = AppTheme.colors.text,
            cursorColor = AppTheme.colors.primary,
            errorContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun ButtonsContainer(
    modifier: Modifier = Modifier,
    leadingButton: @Composable () -> Unit = {},
    trailingButton: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .animateContentSize(),
    ) {
        leadingButton()
        Spacer(modifier = Modifier.weight(1f))
        trailingButton()
    }
}

@Composable
private fun RoundedAnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    color: Color = AppTheme.colors.primary,
    icon: @Composable () -> Unit = {},
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
    }
}

@Composable
private fun CancelIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(Res.drawable.ic_stop),
        tint = AppTheme.colors.onPrimary,
        contentDescription = stringResource(Res.string.query_input_stop_desc),
    )
}

@Composable
private fun SubmitIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(Res.drawable.ic_arrow_up),
        tint = AppTheme.colors.onPrimary,
        contentDescription = stringResource(Res.string.query_input_submit_desc),
    )
}

@Composable
@Preview
private fun QueryInputBoxPreview() = PreviewWrapper {
    QueryInputBox(
        component = QueryInputComponentPreview(),
    )
}

@Composable
@Preview
private fun QueryInputBoxLoadingPreview() = PreviewWrapper {
    QueryInputBox(
        component = QueryInputComponentPreview(
            QueryInputComponent.State(isEnabled = true, isLoading = true)
        ),
    )
}

@Composable
@Preview
private fun QueryInputBoxTypingPreview() = PreviewWrapper {
    QueryInputBox(
        component = QueryInputComponentPreview(
            QueryInputComponent.State(isEnabled = true, query = "Tell me about me")
        ),
    )
}

@Composable
@Preview
private fun QueryInputBoxWithAttachmentsPreview() = PreviewWrapper {
    QueryInputBox(
        component = QueryInputComponentPreview(
            QueryInputComponent.State(
                isEnabled = true,
                query = "Tell me about me",
                attachmentsState = AttachmentsState(
                    canAttachImages = true,
                    canAttachDocuments = true,
                    attachments = listOf(
                        MessageAttachment(
                            content = AttachmentContentReference
                                .LocalFile("test/path".encodeToByteArray()),
                            format = "pdf",
                            fileName = "file",
                            mimeType = "application/pdf",
                        ),
                        MessageAttachment(
                            content = AttachmentContentReference
                                .LocalFile("test/path".encodeToByteArray()),
                            format = "png",
                            fileName = "image",
                            mimeType = "image/png",
                        )
                    )
                ),
            )
        ),
    )
}

@Composable
@Preview
private fun QueryInputBoxWithCapabilitiesPreview() = PreviewWrapper {
    QueryInputBox(
        component = QueryInputComponentPreview(
            QueryInputComponent.State(
                isEnabled = true,
                attachmentsState = AttachmentsState(
                    canAttachImages = true,
                    canAttachDocuments = true,
                ),
            )
        ),
    )
}

internal val QueryInputBoxShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp,
)
