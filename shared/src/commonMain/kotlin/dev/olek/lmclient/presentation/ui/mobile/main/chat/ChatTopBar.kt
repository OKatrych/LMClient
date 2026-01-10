@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.main.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.olek.lmclient.presentation.components.main.ChatTopBarComponent
import dev.olek.lmclient.presentation.components.main.ChatTopBarComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.PreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.animatedBorder
import dev.olek.lmclient.presentation.ui.mobile.common.topbar.CenteredTopBar
import dev.olek.lmclient.presentation.ui.mobile.main.chat.modelselector.ModelSelectorPopup
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.chat_history_button_content_desc
import lm_client.shared.generated.resources.ic_chevron_down
import lm_client.shared.generated.resources.ic_list_selection
import lm_client.shared.generated.resources.ic_new_chat
import lm_client.shared.generated.resources.new_chat_button_content_desc
import lm_client.shared.generated.resources.selected_model_button_no_model
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun ChatTopBar(component: ChatTopBarComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateMultiplatform()
    val modelSelectorComponent by component.modelSelectorComponent.subscribeAsState()

    CenteredTopBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(
                onClick = component::navigateToChatHistory,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_list_selection),
                    tint = AppTheme.colors.icon,
                    contentDescription = stringResource(Res.string.chat_history_button_content_desc),
                )
            }
        },
        centerContent = {
            if (state.isVisible) {
                SelectedModelButton(
                    selectedModel = state.selectedModel,
                    onSelectModelClick = component::onModelSelectorClick,
                )
            }
        },
        actionIcon = {
            IconButton(
                onClick = component::navigateToNewChat,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_new_chat),
                    tint = AppTheme.colors.icon,
                    contentDescription = stringResource(Res.string.new_chat_button_content_desc),
                )
            }
        },
    )

    modelSelectorComponent.child?.instance?.let {
        ModelSelectorPopup(
            component = it,
        )
    }
}

@Composable
private fun SelectedModelButton(
    selectedModel: String?,
    onSelectModelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .animatedBorder(
                borderColors = AppTheme.colors.strokeGradient,
                shape = AppTheme.shapes.button,
            ).clip(AppTheme.shapes.button)
            .clickable(onClick = onSelectModelClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val textColor = AppTheme.colors.text
        BasicText(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 4.dp),
            text = selectedModel ?: stringResource(Res.string.selected_model_button_no_model),
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = AppTheme.typography.bodyLarge,
            color = { textColor },
            autoSize = TextAutoSize.StepBased(
                maxFontSize = AppTheme.typography.bodyLarge.fontSize,
            ),
        )
        Icon(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 12.dp),
            painter = painterResource(Res.drawable.ic_chevron_down),
            tint = AppTheme.colors.icon,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun ChatTopBarPreview() = PreviewWrapper {
    ChatTopBar(
        component = ChatTopBarComponentPreview(
            customState = ChatTopBarComponent.ChatTopBarState(
                selectedModel = "gpt-5",
                isVisible = true,
            ),
        ),
    )
}
